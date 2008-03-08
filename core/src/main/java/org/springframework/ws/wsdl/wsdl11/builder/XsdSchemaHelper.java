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

package org.springframework.ws.wsdl.wsdl11.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.sax.SaxUtils;

/**
 * Helper class for dealing with XSD schemas. Exposes the target namespace, and the list of qualified names declared in
 * a schema.
 *
 * @author Arjen Poutsma
 * @since 1.0.2
 * @deprecated as of Spring Web Services 1.5: superseded by {@link org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition}
 *             and the {@link org.springframework.ws.wsdl.wsdl11.provider} package
 */
class XsdSchemaHelper {

    private static final Log logger = LogFactory.getLog(XsdSchemaHelper.class);

    private static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    static final QName SCHEMA_NAME = QNameUtils.createQName(SCHEMA_NAMESPACE, "schema", "xsd");

    static final QName ELEMENT_NAME = QNameUtils.createQName(SCHEMA_NAMESPACE, "element", "xsd");

    static final QName INCLUDE_NAME = QNameUtils.createQName(SCHEMA_NAMESPACE, "include", "xsd");

    static final QName IMPORT_NAME = QNameUtils.createQName(SCHEMA_NAMESPACE, "import", "xsd");

    private final Resource schemaResource;

    private final Element schemaElement;

    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    static {
        documentBuilderFactory.setNamespaceAware(true);
    }

    public XsdSchemaHelper(Resource schemaResource) throws ParserConfigurationException, IOException, SAXException {
        Assert.notNull(schemaResource, "schema must not be empty or null");
        Assert.isTrue(schemaResource.exists(), "schema \"" + schemaResource + "\" does not exit");
        this.schemaResource = schemaResource;
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document schemaDocument = documentBuilder.parse(SaxUtils.createInputSource(schemaResource));
        schemaElement = schemaDocument.getDocumentElement();
        Assert.isTrue(SCHEMA_NAME.getLocalPart().equals(schemaElement.getLocalName()),
                "schema document root element has invalid local name : [" + schemaElement.getLocalName() +
                        "] instead of [schema]");
        Assert.isTrue(SCHEMA_NAME.getNamespaceURI().equals(schemaElement.getNamespaceURI()),
                "schema document root element has invalid namespace uri: [" + schemaElement.getNamespaceURI() +
                        "] instead of [" + SCHEMA_NAME.getNamespaceURI() + "]");
        Assert.hasLength(getTargetNamespace(), "schema [" + schemaResource + "] has no targetNamespace");
    }

    public String getTargetNamespace() {
        return schemaElement.getAttribute("targetNamespace");
    }

    public Element getSchemaElement() {
        return schemaElement;
    }

    public List getElementDeclarations(boolean followIncludeImport) {
        List declarations = new ArrayList();
        getElementDeclarationsInternal(declarations, followIncludeImport);
        return declarations;
    }

    private void getElementDeclarationsInternal(List declarations, boolean followIncludeImport) {
        NodeList children = schemaElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) children.item(i);
                QName childName = QNameUtils.getQNameForNode(childElement);
                if (ELEMENT_NAME.equals(childName)) {
                    declarations.add(getElementName(childElement));
                }
                else if (followIncludeImport) {
                    if (INCLUDE_NAME.equals(childName) || IMPORT_NAME.equals(childName)) {
                        String schemaLocation = childElement.getAttribute("schemaLocation");
                        if (StringUtils.hasLength(schemaLocation)) {
                            try {
                                Resource resource = schemaResource.createRelative(schemaLocation);
                                if (resource.exists()) {
                                    XsdSchemaHelper helper = new XsdSchemaHelper(resource);
                                    helper.getElementDeclarationsInternal(declarations, followIncludeImport);
                                }
                                else {
                                    logger.warn("Imported/Includes schema with location " + schemaLocation +
                                            " does not exist");
                                }
                            }
                            catch (Exception ex) {
                                logger.warn(ex);
                                // ignore
                            }
                        }
                    }
                }
            }
        }
    }

    private QName getElementName(Element element) {
        String attributeValue = element.getAttribute("name");
        return StringUtils.hasLength(attributeValue) ? new QName(getTargetNamespace(), attributeValue) : null;
    }


}
