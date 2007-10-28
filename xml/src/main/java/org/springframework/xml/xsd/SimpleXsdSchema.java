/*
 * Copyright 2007 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.sax.SaxUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple implementation of the {@link XsdSchema} interface. Only wraps a single {@link XsdSchemaDocument}, and does not
 * follow <code>&lt;xsd:include/&gt;</code> nor <code>&lt;xsd:import/&gt;</code> elements.
 * <p/>
 * Allows an XSD {@link Resource resource} to be set by the {@link #setSchema(Resource) schema} property, or directly in
 * the {@link #SimpleXsdSchema(Resource) constructor}.
 *
 * @author Arjen Poutsma
 * @since 1.0.2
 */
public class SimpleXsdSchema implements XsdSchema, InitializingBean {

    private static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    /** The schema qualified name. */
    private static final QName SCHEMA_NAME = QNameUtils.createQName(SCHEMA_NAMESPACE, "schema", "xsd");

    /** The element declaration qualified name. */
    private static final QName ELEMENT_NAME = QNameUtils.createQName(SCHEMA_NAMESPACE, "element", "xsd");

    private final XsdSchemaDocument[] schemaDocuments = new XsdSchemaDocument[]{new SimpleXsdSchemaDocument()};

    private Resource schemaResource;

    private Element schemaElement;

    /**
     * Create a new instance of the {@link SimpleXsdSchema} class.
     * <p/>
     * A subsequent call to the {@link #setSchema(Resource)} method is required.
     */
    public SimpleXsdSchema() {
    }

    /**
     * Create a new instance of the {@link SimpleXsdSchema} class with the specified resource.
     *
     * @param schemaResource the XSD resource; must not be <code>null</code>
     * @throws IllegalArgumentException if the supplied <code>schemaResource</code> is <code>null</code>
     */
    public SimpleXsdSchema(Resource schemaResource) {
        setSchema(schemaResource);
    }

    /** Set the XSD resource to be used. */
    public void setSchema(Resource schemaResource) {
        this.schemaResource = schemaResource;
    }

    public XsdSchemaDocument[] getSchemaDocuments() {
        return schemaDocuments;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(schemaResource, "'schema' is required");
        Assert.isTrue(schemaResource.exists(), "schema '" + schemaResource + "' does not exit");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(SaxUtils.createInputSource(schemaResource));
        schemaElement = document.getDocumentElement();
        QName name = QNameUtils.getQNameForNode(schemaElement);
        Assert.isTrue(SCHEMA_NAME.equals(name),
                "schema document has invalid qualified name: " + name + ". " + "Epected " + SCHEMA_NAME);
        Assert.hasLength(schemaElement.getAttribute("targetNamespace"), "schema does not define targetNamespace");
    }

    /** Inner definition of {@link SimpleXsdSchemaDocument}. */
    private class SimpleXsdSchemaDocument implements XsdSchemaDocument {

        public String getFilename() {
            return schemaResource.getFilename();
        }

        public Source getSource() {
            return new DOMSource(schemaElement);
        }

        public XsdElementDeclaration[] getElementDeclarations() {
            NodeList children = schemaElement.getChildNodes();
            List declarations = new ArrayList(children.getLength());
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) children.item(i);
                    QName childName = QNameUtils.getQNameForNode(childElement);
                    if (ELEMENT_NAME.equals(childName)) {
                        declarations.add(new SimpleXsdElementDeclaration(childElement));
                    }
                }
            }
            return (XsdElementDeclaration[]) declarations.toArray(new XsdElementDeclaration[declarations.size()]);
        }

        public String getTargetNamespace() {
            return schemaElement.getAttribute("targetNamespace");
        }

        /** Inner definition of {@link SimpleXsdElementDeclaration}. */
        private class SimpleXsdElementDeclaration implements XsdElementDeclaration {

            private final Element element;

            private SimpleXsdElementDeclaration(Element element) {
                this.element = element;
            }

            public QName getName() {
                String attributeValue = element.getAttribute("name");
                return StringUtils.hasLength(attributeValue) ? new QName(getTargetNamespace(), attributeValue) : null;
            }
        }

    }

}