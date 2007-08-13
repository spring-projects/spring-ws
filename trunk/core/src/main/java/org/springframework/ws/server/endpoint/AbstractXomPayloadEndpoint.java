/*
 * Copyright 2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.converters.DOMConverter;
import org.springframework.util.ClassUtils;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Abstract base class for endpoints that handle the message payload as XOM elements. Offers the message payload as a
 * XOM <code>Element</code>, and allows subclasses to create a response by returning an <code>Element</code>.
 * <p/>
 * An <code>AbstractXomPayloadEndpoint</code> only accept one payload element. Multiple payload elements are not in
 * accordance with WS-I.
 * <p/>
 * This class tries to use Java reflection to access some of the non-public classes of XOM
 * (<code>nu.xom.xslt.XOMResult</code> and <code>nu.xom.xslt.XOMSource</code>). If these classes cannot be accessed
 * because of security restrictions, a slower approach is used. You can specify whether you want to use the faster, but
 * non-public reflection-based approach by calling {@link #AbstractXomPayloadEndpoint(boolean)}.
 *
 * @author Arjen Poutsma
 * @see Element
 */
public abstract class AbstractXomPayloadEndpoint extends TransformerObjectSupport implements PayloadEndpoint {

    private Constructor xomResultConstructor;

    private Method xomResultGetResultMethod;

    private Constructor xomSourceConstructor;

    private boolean useReflection = true;

    private DocumentBuilderFactory documentBuilderFactory;

    /**
     * Creates a new instance of <code>AbstractXomPayloadEndpoint</code> using reflection to access faster, but
     * non-public XOM classes.
     */
    protected AbstractXomPayloadEndpoint() {
        this(true);
    }

    /**
     * Creates a new instance of <code>AbstractXomPayloadEndpoint</code>.
     *
     * @param useReflection specifies whether to use faster, but non-public XOM classes (<code>true</code>); or to use a
     *                      converting approach (<code>false</code>)
     */
    protected AbstractXomPayloadEndpoint(boolean useReflection) {
        this.useReflection = useReflection;
        if (useReflection) {
            try {
                Class xomResultClass = ClassUtils.forName("nu.xom.xslt.XOMResult");
                xomResultConstructor = xomResultClass.getDeclaredConstructor(new Class[]{NodeFactory.class});
                xomResultConstructor.setAccessible(true);
                xomResultGetResultMethod = xomResultClass.getDeclaredMethod("getResult", new Class[0]);
                xomResultGetResultMethod.setAccessible(true);
                Class xomSourceClass = ClassUtils.forName("nu.xom.xslt.XOMSource");
                xomSourceConstructor = xomSourceClass.getDeclaredConstructor(new Class[]{Nodes.class});
                xomSourceConstructor.setAccessible(true);
            }
            catch (Exception e) {
                this.useReflection = false;
                createDocumentBuilderFactory();
            }
        }
    }

    public final Source invoke(Source request) throws Exception {
        if (useReflection) {
            return invokeUsingReflection(request);
        }
        else {
            return invokeUsingTransformation(request);
        }
    }

    private Source invokeUsingReflection(Source request) throws Exception {
        try {
            Element requestElement = null;
            if (request != null) {
                Result xomResult = createXomResult();
                transform(request, xomResult);
                requestElement = getRequestElement(xomResult);
            }
            Element responseElement = invokeInternal(requestElement);
            return responseElement != null ? createXomSource(responseElement) : null;
        }
        catch (IllegalAccessException ex) {
            useReflection = false;
            throw ex;
        }
        catch (InvocationTargetException ex) {
            useReflection = false;
            throw ex;
        }
        catch (InstantiationException ex) {
            useReflection = false;
            throw ex;
        }
    }

    private Source invokeUsingTransformation(Source request) throws Exception {
        Element requestElement = null;
        if (request != null) {
            if (request instanceof DOMSource) {
                requestElement = handleDomSource(request);
            }
            else if (request instanceof SAXSource) {
                requestElement = handleSaxSource(request);
            }
            else if (request instanceof StreamSource) {
                requestElement = handleStreamSource(request);
            }
            else {
                throw new IllegalArgumentException("Source [" + request.getClass().getName() +
                        "] is neither SAXSource, DOMSource, nor StreamSource");
            }
        }
        Element responseElement = invokeInternal(requestElement);
        if (responseElement != null) {
            if (documentBuilderFactory == null) {
                createDocumentBuilderFactory();
            }
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document responseDocument = new Document(responseElement);
            org.w3c.dom.Document w3cDocument =
                    DOMConverter.convert(responseDocument, documentBuilder.getDOMImplementation());
            return new DOMSource(w3cDocument);
        }
        else {
            return null;
        }

    }

    private Result createXomResult() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return (Result) xomResultConstructor.newInstance(new Object[]{new NodeFactory()});
    }

    private Element getRequestElement(Result xomResult) throws IllegalAccessException, InvocationTargetException {
        Nodes result = (Nodes) xomResultGetResultMethod.invoke(xomResult, new Object[0]);
        if (result.size() == 0) {
            return null;
        }
        else {
            return (Element) result.get(0);
        }
    }

    private Source createXomSource(Element responseElement)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Nodes nodes = new Nodes(responseElement);
        return (Source) xomSourceConstructor.newInstance(new Object[]{nodes});
    }

    private Element handleStreamSource(Source request) throws ParsingException, IOException {
        StreamSource streamSource = (StreamSource) request;
        Builder builder = new Builder();
        Document document = null;
        if (streamSource.getInputStream() != null) {
            document = builder.build(streamSource.getInputStream());
        }
        else if (streamSource.getReader() != null) {
            document = builder.build(streamSource.getReader());
        }
        else {
            throw new IllegalArgumentException("StreamSource contains neither byte stream nor character stream");
        }
        return document.getRootElement();
    }

    private Element handleSaxSource(Source request) throws ParsingException, IOException {
        SAXSource saxSource = (SAXSource) request;
        Builder builder = new Builder(saxSource.getXMLReader());
        InputSource inputSource = saxSource.getInputSource();
        Document document = null;
        if (inputSource.getByteStream() != null) {
            document = builder.build(inputSource.getByteStream());
        }
        else if (inputSource.getCharacterStream() != null) {
            document = builder.build(inputSource.getCharacterStream());
        }
        else {
            throw new IllegalArgumentException(
                    "InputSource in SAXSource contains neither byte stream nor " + "character stream");
        }
        return document.getRootElement();
    }

    private Element handleDomSource(Source request) {
        Node w3cNode = ((DOMSource) request).getNode();
        org.w3c.dom.Element w3cElement = null;
        if (w3cNode.getNodeType() == Node.ELEMENT_NODE) {
            w3cElement = (org.w3c.dom.Element) w3cNode;
        }
        else if (w3cNode.getNodeType() == Node.DOCUMENT_NODE) {
            org.w3c.dom.Document w3cDocument = (org.w3c.dom.Document) w3cNode;
            w3cElement = w3cDocument.getDocumentElement();
        }
        return DOMConverter.convert(w3cElement);
    }

    private void createDocumentBuilderFactory() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    /**
     * Template method. Subclasses must implement this. Offers the request payload as a XOM <code>Element</code>, and
     * allows subclasses to return a response <code>Element</code>.
     *
     * @param requestElement the contents of the SOAP message as XOM element
     * @return the response element. Can be <code>null</code> to specify no response.
     */
    protected abstract Element invokeInternal(Element requestElement) throws Exception;


}
