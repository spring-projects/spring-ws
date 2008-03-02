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

package org.springframework.xml.xsd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.sax.SaxUtils;

/**
 * The default {@link XsdSchema} implementation.
 * <p/>
 * Allows a XSD to be set by the {@link #setXsd(Resource)}, or directly in the {@link #SimpleXsdSchema(Resource)
 * constructor}.
 *
 * @author Mark LaFond
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class SimpleXsdSchema implements XsdSchema, InitializingBean {

    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    private static final QName SCHEMA_NAME = QNameUtils.createQName(SCHEMA_NAMESPACE, "schema", "xsd");

    private static final QName ELEMENT_NAME = QNameUtils.createQName(SCHEMA_NAMESPACE, "element", "xsd");

    private Resource xsdResource;

    private Element schemaElement;

    static {
        documentBuilderFactory.setNamespaceAware(true);
    }

    /**
     * Create a new instance of the {@link SimpleXsdSchema} class.
     * <p/>
     * A subsequent call to the {@link #setXsd(Resource)} method is required.
     */
    public SimpleXsdSchema() {
    }

    /**
     * Create a new instance of the  {@link SimpleXsdSchema} class with the specified resource.
     *
     * @param xsdResource the XSD resource; must not be <code>null</code>
     * @throws IllegalArgumentException if the supplied <code>xsdResource</code> is <code>null</code>
     */
    public SimpleXsdSchema(Resource xsdResource) {
        Assert.notNull(xsdResource, "xsdResource must not be null");
        this.xsdResource = xsdResource;
    }

    /**
     * Set the XSD resource to be exposed by calls to this instances' {@link #getSource()} method.
     *
     * @param xsdResource the XSD resource
     */
    public void setXsd(Resource xsdResource) {
        this.xsdResource = xsdResource;
    }

    public String getTargetNamespace() {
        return schemaElement.getAttribute("targetNamespace");
    }

    public Source getSource() {
        return new DOMSource(schemaElement);
    }

    public QName[] getElementNames() {
        NodeList children = schemaElement.getChildNodes();
        List result = new ArrayList(children.getLength());
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) children.item(i);
                QName childName = QNameUtils.getQNameForNode(childElement);
                if (ELEMENT_NAME.equals(childName)) {
                    result.add(getElementName(childElement));
                }
            }
        }
        return (QName[]) result.toArray(new QName[result.size()]);
    }

    private QName getElementName(Element element) {
        String attributeValue = element.getAttribute("name");
        return StringUtils.hasLength(attributeValue) ? new QName(getTargetNamespace(), attributeValue) : null;
    }

    public void afterPropertiesSet() throws ParserConfigurationException, IOException, SAXException {
        Assert.notNull(xsdResource, "'xsd' is required");
        Assert.isTrue(this.xsdResource.exists(), "xsd '" + this.xsdResource + "' does not exit");
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        loadSchema(documentBuilder);
    }

    private void loadSchema(DocumentBuilder documentBuilder) throws SAXException, IOException {
        Document schemaDocument = documentBuilder.parse(SaxUtils.createInputSource(xsdResource));
        schemaElement = schemaDocument.getDocumentElement();
        Assert.isTrue(SCHEMA_NAME.getLocalPart().equals(schemaElement.getLocalName()),
                xsdResource + " has invalid root element : [" + schemaElement.getLocalName() + "] instead of [schema]");
        Assert.isTrue(SCHEMA_NAME.getNamespaceURI().equals(schemaElement.getNamespaceURI()), xsdResource +
                " has invalid root element: [" + schemaElement.getNamespaceURI() + "] instead of [" +
                SCHEMA_NAME.getNamespaceURI() + "]");
        Assert.hasText(getTargetNamespace(), xsdResource + " has no targetNamespace");
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("SimpleXsdSchema");
        buffer.append('{');
        buffer.append(getTargetNamespace());
        buffer.append('}');
        return buffer.toString();
    }

}