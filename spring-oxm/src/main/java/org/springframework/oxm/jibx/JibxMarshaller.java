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

package org.springframework.oxm.jibx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.runtime.impl.StAXReaderWrapper;
import org.jibx.runtime.impl.StAXWriter;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.AbstractMarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xml.stream.StaxEventContentHandler;
import org.springframework.xml.stream.XmlEventStreamReader;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * Implementation of the <code>Marshaller</code> and <code>Unmarshaller</code> interfaces for JiBX.
 * <p/>
 * The typical usage will be to set the <code>targetClass</code> and optionally the <code>bindingName</code> property on
 * this bean, and to refer to it.
 *
 * @author Arjen Poutsma
 * @see org.jibx.runtime.IMarshallingContext
 * @see org.jibx.runtime.IUnmarshallingContext
 */
public class JibxMarshaller extends AbstractMarshaller implements InitializingBean {

    private static final Log logger = LogFactory.getLog(JibxMarshaller.class);

    private Class targetClass;

    private String bindingName;

    private IBindingFactory bindingFactory;

    private TransformerFactory transfomerFactory;

    /**
     * Sets the optional binding name for this instance.
     */
    public void setBindingName(String bindingName) {
        this.bindingName = bindingName;
    }

    /**
     * Sets the target class for this instance. This property is required.
     */
    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(targetClass, "targetClass is required");
        if (logger.isInfoEnabled()) {
            if (StringUtils.hasLength(bindingName)) {
                logger.info("Configured for target class [" + targetClass + "] using binding [" + bindingName + "]");
            }
            else {
                logger.info("Configured for target class [" + targetClass + "]");
            }
        }
        try {
            if (StringUtils.hasLength(bindingName)) {
                bindingFactory = BindingDirectory.getFactory(bindingName, targetClass);
            }
            else {
                bindingFactory = BindingDirectory.getFactory(targetClass);
            }
        }
        catch (JiBXException ex) {
            throw new JibxSystemException(ex);
        }
        transfomerFactory = TransformerFactory.newInstance();
    }

    /**
     * Convert the given <code>JiBXException</code> to an appropriate exception from the
     * <code>org.springframework.oxm</code> hierarchy.
     * <p/>
     * The default implementation delegates to <code>JibxUtils</code>. Can be overridden in subclasses.
     * <p/>
     * A boolean flag is used to indicate whether this exception occurs during marshalling or unmarshalling, since JiBX
     * itself does not make this distinction in its exception hierarchy.
     *
     * @param ex          <code>JiBXException</code> that occured
     * @param marshalling indicates whether the exception occurs during marshalling (<code>true</code>), or
     *                    unmarshalling (<code>false</code>)
     * @return the corresponding <code>XmlMappingException</code> instance
     * @see JibxUtils#convertJibxException(org.jibx.runtime.JiBXException, boolean)
     */
    public XmlMappingException convertJibxException(JiBXException ex, boolean marshalling) {
        return JibxUtils.convertJibxException(ex, marshalling);
    }

    protected void marshalDomNode(Object graph, Node node) throws XmlMappingException {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            marshalOutputStream(graph, os);
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            Transformer transformer = transfomerFactory.newTransformer();
            transformer.transform(new StreamSource(is), new DOMResult(node));
        }
        catch (IOException ex) {
            throw new JibxSystemException(ex);
        }
        catch (TransformerException ex) {
            throw new JibxSystemException(ex);
        }
    }

    protected void marshalOutputStream(Object graph, OutputStream outputStream)
            throws XmlMappingException, IOException {
        try {
            IMarshallingContext marshallingContext = bindingFactory.createMarshallingContext();
            marshallingContext.marshalDocument(graph, null, null, outputStream);
        }
        catch (JiBXException ex) {
            throw convertJibxException(ex, true);
        }
    }

    protected void marshalSaxHandlers(Object graph, ContentHandler contentHandler, LexicalHandler lexicalHandler)
            throws XmlMappingException {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            marshalOutputStream(graph, os);
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            Transformer transformer = transfomerFactory.newTransformer();
            transformer.transform(new StreamSource(is), new SAXResult(contentHandler));
        }
        catch (IOException ex) {
            throw new JibxSystemException(ex);
        }
        catch (TransformerException ex) {
            throw new JibxSystemException(ex);
        }
    }

    protected void marshalWriter(Object graph, Writer writer) throws XmlMappingException, IOException {
        try {
            IMarshallingContext marshallingContext = bindingFactory.createMarshallingContext();
            marshallingContext.marshalDocument(graph, null, null, writer);
        }
        catch (JiBXException ex) {
            throw convertJibxException(ex, true);
        }
    }

    protected void marshalXmlEventWriter(Object graph, XMLEventWriter eventWriter) {

        ContentHandler contentHandler = new StaxEventContentHandler(eventWriter);
        marshalSaxHandlers(graph, contentHandler, null);
    }

    protected void marshalXmlStreamWriter(Object graph, XMLStreamWriter streamWriter) throws XmlMappingException {
        try {
            MarshallingContext marshallingContext = (MarshallingContext) bindingFactory.createMarshallingContext();
            IXMLWriter xmlWriter = new StAXWriter(marshallingContext.getNamespaces(), streamWriter);
            marshallingContext.setXmlWriter(xmlWriter);
            marshallingContext.marshalDocument(graph);
        }
        catch (JiBXException ex) {
            throw convertJibxException(ex, false);
        }
    }

    protected Object unmarshalDomNode(Node node) throws XmlMappingException {
        try {
            Transformer transformer = transfomerFactory.newTransformer();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(node), new StreamResult(os));
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            return unmarshalInputStream(is);
        }
        catch (IOException ex) {
            throw new JibxSystemException(ex);
        }
        catch (TransformerException ex) {
            throw new JibxSystemException(ex);
        }
    }

    protected Object unmarshalInputStream(InputStream inputStream) throws XmlMappingException, IOException {
        try {
            IUnmarshallingContext unmarshallingContext = bindingFactory.createUnmarshallingContext();
            return unmarshallingContext.unmarshalDocument(inputStream, null);
        }
        catch (JiBXException ex) {
            throw convertJibxException(ex, false);
        }
    }

    protected Object unmarshalReader(Reader reader) throws XmlMappingException, IOException {
        try {
            IUnmarshallingContext unmarshallingContext = bindingFactory.createUnmarshallingContext();
            return unmarshallingContext.unmarshalDocument(reader);
        }
        catch (JiBXException ex) {
            throw convertJibxException(ex, false);
        }
    }

    protected Object unmarshalSaxReader(XMLReader xmlReader, InputSource inputSource)
            throws XmlMappingException, IOException {
        try {
            Transformer transformer = transfomerFactory.newTransformer();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            transformer.transform(new SAXSource(xmlReader, inputSource), new StreamResult(os));
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            return unmarshalInputStream(is);
        }
        catch (IOException ex) {
            throw new JibxSystemException(ex);
        }
        catch (TransformerException ex) {
            throw new JibxSystemException(ex);
        }
    }

    protected Object unmarshalXmlEventReader(XMLEventReader eventReader) {
        try {
            XMLStreamReader streamReader = new XmlEventStreamReader(eventReader);
            return unmarshalXmlStreamReader(streamReader);
        }
        catch (XMLStreamException ex) {
            throw new JibxSystemException(ex);
        }
    }

    protected Object unmarshalXmlStreamReader(XMLStreamReader streamReader) {
        try {
            UnmarshallingContext unmarshallingContext =
                    (UnmarshallingContext) bindingFactory.createUnmarshallingContext();
            IXMLReader xmlReader = new StAXReaderWrapper(streamReader, null, true);
            unmarshallingContext.setDocument(xmlReader);
            return unmarshallingContext.unmarshalElement();
        }
        catch (JiBXException ex) {
            throw convertJibxException(ex, false);
        }
    }
}
