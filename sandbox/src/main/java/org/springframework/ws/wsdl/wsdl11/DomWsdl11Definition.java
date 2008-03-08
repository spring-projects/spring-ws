/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.wsdl.wsdl11;

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class DomWsdl11Definition implements Wsdl11Definition, InitializingBean {

    public static final String WSDL_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/";

    public static final String WSDL_NAMESPACE_PREFIX = "wsdl";

    public static final String TARGET_NAMESPACE_PREFIX = "tns";

    private Document document;

    private String targetNamespace;

    public void setTargetNamespace(String targetNamespace) {
        Assert.notNull(targetNamespace, "'targetNamespace' must not be null");
        this.targetNamespace = targetNamespace;
    }

    public Source getSource() {
        return new DOMSource(document);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(targetNamespace, "'targetNamespace' is required");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        Element definitions = createDefinitions(document);
        document.appendChild(definitions);
    }

    public Element createDefinitions(Document document) {
        Element definitions = createWsdlElement(document, "definitions");
        declareNamespaces(definitions);
        definitions.setAttribute("targetNamespace", targetNamespace);
        addImports(document, definitions);
        addTypes(document, definitions);
        addMessages(document, definitions);
        addPortTypes(document, definitions);
        addBindings(document, definitions);
        addServices(document, definitions);

        return definitions;
    }

    protected void declareNamespaces(Element definitions) {
        declareNamespace(definitions, WSDL_NAMESPACE_PREFIX, WSDL_NAMESPACE_URI);
        declareNamespace(definitions, TARGET_NAMESPACE_PREFIX, targetNamespace);
    }

    protected void addImports(Document document, Element definitions) {
    }

    protected void addTypes(Document document, Element definitions) {
    }

    protected void addMessages(Document document, Element definitions) {
    }

    protected void addPortTypes(Document document, Element definitions) {
    }

    protected void addBindings(Document document, Element definitions) {
    }

    protected void addServices(Document document, Element definitions) {
    }

    protected void declareNamespace(Element element, String namespacePrefix, String namespaceUri) {
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + namespacePrefix, namespaceUri);
    }

    protected Element createWsdlElement(Document document, String localName) {
        return createElement(document, WSDL_NAMESPACE_PREFIX, WSDL_NAMESPACE_URI, localName);
    }

    protected Element createElement(Document document, String namespacePrefix, String namespaceUri, String localName) {
        Assert.hasLength(namespacePrefix, "No prefix given");
        Assert.hasLength(namespaceUri, "No namespace given");
        Assert.hasLength(localName, "No localName given");
        return document.createElementNS(namespaceUri, namespacePrefix + ":" + localName);
    }

    protected List getWsdlChildElements(Element element, String localName) {
        return getChildElements(element, WSDL_NAMESPACE_URI, localName);
    }

    protected Element getWsdlChildElement(Element element, String localName) {
        return getChildElement(element, WSDL_NAMESPACE_URI, localName);
    }

    protected List getChildElements(Element element, String namespaceUri, String localName) {
        Assert.hasLength(namespaceUri, "No namespace given");
        Assert.hasLength(localName, "No localName given");
        NodeList nodeList = element.getChildNodes();
        List result = new ArrayList();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && namespaceUri.equals(node.getNamespaceURI()) &&
                    localName.equals(node.getLocalName())) {
                result.add(node);
            }
        }
        return result;
    }

    protected Element getChildElement(Element element, String namespaceUri, String localName) {
        Assert.hasLength(namespaceUri, "No namespace given");
        Assert.hasLength(localName, "No localName given");
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && namespaceUri.equals(node.getNamespaceURI()) &&
                    localName.equals(node.getLocalName())) {
                return (Element) node;
            }
        }
        return null;
    }


}
