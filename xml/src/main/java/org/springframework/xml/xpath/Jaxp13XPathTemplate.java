/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.xml.xpath;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.springframework.xml.transform.TraxUtils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Implementation of {@link XPathOperations} that uses JAXP 1.3. JAXP 1.3 is part of Java SE since 1.5.
 * <p/>
 * Namespaces can be set using the <code>namespaces</code> property.
 *
 * @author Arjen Poutsma
 * @see #setNamespaces(java.util.Properties)
 * @since 1.0.0
 */
public class Jaxp13XPathTemplate extends AbstractXPathTemplate {

    private XPathFactory xpathFactory;

    public Jaxp13XPathTemplate() {
        this(XPathFactory.DEFAULT_OBJECT_MODEL_URI);
    }

    public Jaxp13XPathTemplate(String xpathFactoryUri) {
        try {
            xpathFactory = XPathFactory.newInstance(xpathFactoryUri);
        }
        catch (XPathFactoryConfigurationException ex) {
            throw new XPathException("Could not create XPathFactory", ex);
        }
    }

    public boolean evaluateAsBoolean(String expression, Source context) throws XPathException {
        Boolean result = (Boolean) evaluate(expression, context, XPathConstants.BOOLEAN);
        return result != null && result;
    }

    public Node evaluateAsNode(String expression, Source context) throws XPathException {
        return (Node) evaluate(expression, context, XPathConstants.NODE);
    }

    public List<Node> evaluateAsNodeList(String expression, Source context) throws XPathException {
        NodeList result = (NodeList) evaluate(expression, context, XPathConstants.NODESET);
        List<Node> nodes = new ArrayList<Node>(result.getLength());
        for (int i = 0; i < result.getLength(); i++) {
            nodes.add(result.item(i));
        }
        return nodes;
    }

    public double evaluateAsDouble(String expression, Source context) throws XPathException {
        Double result = (Double) evaluate(expression, context, XPathConstants.NUMBER);
        return result != null ? result : Double.NaN;
    }

    public String evaluateAsString(String expression, Source context) throws XPathException {
        return (String) evaluate(expression, context, XPathConstants.STRING);
    }

    public <T> T evaluateAsObject(String expression, Source context, NodeMapper<T> nodeMapper) throws XPathException {
        Node node = evaluateAsNode(expression, context);
        if (node != null) {
            try {
                return nodeMapper.mapNode(node, 0);
            }
            catch (DOMException ex) {
                throw new XPathException("Mapping resulted in DOMException", ex);
            }
        }
        else {
            return null;
        }
    }

    public <T> List<T> evaluate(String expression, Source context, NodeMapper<T> nodeMapper) throws XPathException {
        NodeList nodes = (NodeList) evaluate(expression, context, XPathConstants.NODESET);
        List<T> results = new ArrayList<T>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            try {
                results.add(nodeMapper.mapNode(nodes.item(i), i));
            }
            catch (DOMException ex) {
                throw new XPathException("Mapping resulted in DOMException", ex);
            }
        }
        return results;
    }

    private Object evaluate(String expression, Source context, QName returnType) throws XPathException {
        XPath xpath = xpathFactory.newXPath();
        if (getNamespaces() != null && !getNamespaces().isEmpty()) {
            SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
            namespaceContext.setBindings(getNamespaces());
            xpath.setNamespaceContext(namespaceContext);
        }
        try {
            if (TraxUtils.isStaxSource(context)) {
                Element element = getRootElement(context);
                return xpath.evaluate(expression, element, returnType);
            }
            else if (context instanceof SAXSource) {
                SAXSource saxSource = (SAXSource) context;
                return xpath.evaluate(expression, saxSource.getInputSource(), returnType);
            }
            else if (context instanceof DOMSource) {
                DOMSource domSource = (DOMSource) context;
                return xpath.evaluate(expression, domSource.getNode(), returnType);
            }
            else if (context instanceof StreamSource) {
                StreamSource streamSource = (StreamSource) context;
                InputSource inputSource;
                if (streamSource.getInputStream() != null) {
                    inputSource = new InputSource(streamSource.getInputStream());
                }
                else if (streamSource.getReader() != null) {
                    inputSource = new InputSource(streamSource.getReader());
                }
                else {
                    throw new IllegalArgumentException("StreamSource contains neither InputStream nor Reader");
                }
                return xpath.evaluate(expression, inputSource, returnType);
            }
            else {
                throw new IllegalArgumentException("context type unknown");
            }
        }
        catch (javax.xml.xpath.XPathException ex) {
            throw new XPathException("Could not evaluate XPath expression [" + expression + "]", ex);
        }
        catch (TransformerException ex) {
            throw new XPathException("Could not transform context to DOM Node", ex);
        }
    }

}
