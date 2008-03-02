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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class DomWsdl11Definition implements Wsdl11Definition, BeanNameAware, InitializingBean {

    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    public static final String WSDL_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/";

    public static final String WSDL_NAMESPACE_PREFIX = "wsdl";

    public static final String TARGET_NAMESPACE_PREFIX = "tns";

    private Document document;

    private String targetNamespace;

    private String beanName;

    private String name;

    static {
        documentBuilderFactory.setNamespaceAware(true);
    }

    public void setTargetNamespace(String targetNamespace) {
        Assert.notNull(targetNamespace, "'targetNamespace' must not be null");
        this.targetNamespace = targetNamespace;
    }

    public void setName(String name) {
        Assert.notNull(name, "'name' must not be null");
        this.name = name;
    }

    public Source getSource() {
        return new DOMSource(document);
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(targetNamespace, "'targetNamespace' is required");
        if (!StringUtils.hasLength(name)) {
            this.name = beanName;
        }
        DocumentBuilder documentBuilder = createDocumentBuilder();
        document = documentBuilder.newDocument();
        Element definitions = createDefinitions(document);
        document.appendChild(definitions);
    }

    public Element createDefinitions(Document document) {
        Element definitions = createWsdlElement(document, "definitions");
        if (StringUtils.hasLength(name)) {
            definitions.setAttribute("name", name);
        }
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

    protected DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        return documentBuilderFactory.newDocumentBuilder();
    }

    protected void declareNamespace(Element element, String namespacePrefix, String namespaceUri) {
        Assert.hasLength(namespacePrefix, "No prefix given");
        Assert.hasLength(namespaceUri, "No namespace given");
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
