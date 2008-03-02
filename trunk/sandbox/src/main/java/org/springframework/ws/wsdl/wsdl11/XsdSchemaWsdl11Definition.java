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

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;
import org.xml.sax.SAXException;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xml.sax.SaxUtils;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class XsdSchemaWsdl11Definition extends DomWsdl11Definition {

    public static final String XSD_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";

    public static final String XSD_NAMESPACE_PREFIX = "xsd";

    private Resource[] schemaResources;

    private Element[] schemas;

    // keys are String namespaces; values are List of Elements
    private Map namespaces = new LinkedHashMap();

    public void setSchemas(Resource[] schemas) {
        this.schemaResources = schemas;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notEmpty(schemaResources, "'schemas' is required");
        DocumentBuilder documentBuilder = createDocumentBuilder();
        schemas = new Element[schemaResources.length];
        for (int i = 0; i < schemaResources.length; i++) {
            schemas[i] = parseSchema(documentBuilder, schemaResources[i]);
        }
        super.afterPropertiesSet();
    }

    protected void addTypes(Document document, Element definitions) {
        Element types = createWsdlElement(document, "types");
        definitions.appendChild(types);
        for (int i = 0; i < schemas.length; i++) {
            Element importedSchema = (Element) document.importNode(schemas[i], true);
            types.appendChild(importedSchema);
        }
    }
    /*
    protected void declareNamespaces(Element definitions) {
        super.declareNamespaces(definitions);
        declareNamespace(definitions, XSD_NAMESPACE_PREFIX, XSD_NAMESPACE_URI);
        int i = 0;
        for (Iterator iterator = namespaces.keySet().iterator(); iterator.hasNext();) {
            String namespace = (String) iterator.next();
            declareNamespace(definitions, "s" + i, namespace);
            i++;
        }
    }

    protected void addTypes(Document document, Element definitions) {
        Element types = createWsdlElement(document, "types");
        definitions.appendChild(types);
        for (Iterator iterator = namespaces.keySet().iterator(); iterator.hasNext();) {
            String namespace = (String) iterator.next();
            List schemaElements = (List) namespaces.get(namespace);
            addSchema(document, types, namespace, schemaElements);
        }
    }

    private void addSchema(Document document, Element types, String targetNamespace, List schemaElements) {
        Element schema = createElement(document, XSD_NAMESPACE_PREFIX, XSD_NAMESPACE_URI, "schema");
        types.appendChild(schema);
        schema.setAttribute("elementFormDefault", "qualified");
        schema.setAttribute("targetNamespace", targetNamespace);
        for (Iterator iterator = schemaElements.iterator(); iterator.hasNext();) {
            Element toBeImported = (Element) iterator.next();
            NodeList children = toBeImported.getChildNodes();
            for (int i = 0; i < children.getLength();i++) {
                Node importedNode = document.importNode(children.item(i), true);
                schema.appendChild(importedNode);
            }
        }
    }
    */

    /*
        protected void addTypes(Document document, Element definitions) {
            Element types = createWsdlElement(document, "types");
            definitions.appendChild(types);
            for (int i = 0; i < schemas.length; i++) {
                addSchema(document, types, schemas[i]);
            }
        }

        private void addSchema(Document document, Element types, Resource xsdSchema) {
            try {
                DocumentBuilder documentBuilder = createDocumentBuilder();
                Element schema = parseSchema(documentBuilder, xsdSchema, null);
                Element importedSchema = (Element) document.importNode(schema, true);
                types.appendChild(importedSchema);
            }
            catch (ParserConfigurationException ex) {
                throw new WsdlDefinitionException("Could not create DocumentBuilder", ex);
            }
        }


        private Element parseSchema(DocumentBuilder documentBuilder,
                                    Resource schemaResource,
                                    String expectedTargetNamespace) {
            try {
                Document schemaDocument = documentBuilder.parse(SaxUtils.createInputSource(schemaResource));
                Element schema = schemaDocument.getDocumentElement();
                checkSchemaElement(schemaResource, expectedTargetNamespace, schema);
                inlineIncludes(documentBuilder, schemaResource, schema);
                return schema;
            }
            catch (Exception ex) {
                throw new WsdlDefinitionException("Could parse schema " + schemaResource, ex);
            }
        }

        private void checkSchemaElement(Resource schemaResource, String expectedTargetNamespace, Element schema) {
            Assert.isTrue("schema".equals(schema.getLocalName()),
                        schemaResource + " does not have 'schema' as root element local name");
            Assert.isTrue(XSD_NAMESPACE_URI.equals(schema.getNamespaceURI()),
                        schemaResource + " does not have '" + XSD_NAMESPACE_URI + "' as root element namespace");
            if (StringUtils.hasText(expectedTargetNamespace)) {
                String targetNamespace = schema.getAttribute("targetNamespace");
                Assert.isTrue(!StringUtils.hasText(targetNamespace) ||
                        expectedTargetNamespace.equals(targetNamespace), schemaResource +
                        " has invalid targetNamespace [" + targetNamespace + "]. Expected [" + expectedTargetNamespace +
                        "]");
            }
            // check for elementFormDefault
        }

        private void inlineIncludes(DocumentBuilder documentBuilder, Resource schemaResource, Element schema)
                throws IOException {
            List includes = getChildElements(schema, XSD_NAMESPACE_URI, "include");
            for (Iterator iterator = includes.iterator(); iterator.hasNext();) {
                Element include = (Element) iterator.next();
                String schemaLocation = include.getAttribute("schemaLocation");
                Assert.hasText(schemaLocation, schemaResource + " <include/> has no schemaLocation attribute");
                Resource includedResource = schemaResource.createRelative(schemaLocation);
                Assert.isTrue(includedResource.exists(), includedResource + " does not exist");
                String targetNamespace = schema.getAttribute("targetNamespace");
                Element includedSchemaElement = parseSchema(documentBuilder, includedResource, targetNamespace);
                NodeList children = includedSchemaElement.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node importedChild = schema.getOwnerDocument().importNode(children.item(i), true);
                    schema.appendChild(importedChild);
                }
                schema.removeChild(include);
            }
        }

        private void findImports(Resource schemaResource, Element schema) throws IOException {
            List imports = getChildElements(schema, XSD_NAMESPACE_URI, "import");
            for (Iterator iterator = imports.iterator(); iterator.hasNext();) {
                Element importEl = (Element) iterator.next();
                String schemaLocation = importEl.getAttribute("schemaLocation");
                Assert.hasText(schemaLocation, schemaResource + " <include/> has no schemaLocation attribute");
                Resource includedResource = schemaResource.createRelative(schemaLocation);

            }
        }

    private Element findNamespaces(DocumentBuilder documentBuilder,
                                   Resource schemaResource,
                                   Map namespaces,
                                   String namespace) throws IOException, SAXException {
        Document schemaDocument = documentBuilder.parse(SaxUtils.createInputSource(schemaResource));
        Element schema = schemaDocument.getDocumentElement();
        if (!StringUtils.hasText(namespace)) {
            namespace = schema.getAttribute("targetNamespace");
        }
        List elements = (List) namespaces.get(namespace);
        if (elements == null) {
            elements = new LinkedList<Element>();
            namespaces.put(namespace, elements);
        }
        NodeList children = schema.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (Node.ELEMENT_NODE == children.item(i).getNodeType() &&
                    XSD_NAMESPACE_URI.equals(children.item(i).getNamespaceURI())) {
                Element element = (Element) children.item(i);
                if ("include".equals(element.getLocalName())) {
                    String schemaLocation = element.getAttribute("schemaLocation");
                    Assert.hasText(schemaLocation, schemaResource + " <include/> has no schemaLocation attribute");
                    Resource includedResource = schemaResource.createRelative(schemaLocation);
                    Assert.isTrue(includedResource.exists(),
                            includedResource + " (included from " + schemaResource + ") does not exist");
                    Element includedSchema = findNamespaces(documentBuilder, includedResource, namespaces, namespace);
                    NodeList nodeList = includedSchema.getChildNodes();
                    for (int j = 0; j < nodeList.getLength(); j++) {
                        Node importedNode = schema.getOwnerDocument().importNode(nodeList.item(j), true);
                        schema.appendChild(importedNode);
                    }
                    schema.removeChild(element);
                }
                else if ("import".equals(element.getLocalName())) {
                    String schemaLocation = element.getAttribute("schemaLocation");
                    Assert.hasText(schemaLocation, schemaResource + " <import/> has no schemaLocation attribute");
                    String importNamespace = element.getAttribute("namespace");
                    Assert.hasText(importNamespace, schemaResource + " <import/> has no namespace attribute");
                    Resource importedResource = schemaResource.createRelative(schemaLocation);
                    Assert.isTrue(importedResource.exists(),
                            importedResource + " (imported from " + schemaResource + ") does not exist");
                    findNamespaces(documentBuilder, importedResource, namespaces, importNamespace);
                    element.removeAttribute("schemaLocation");
                    elements.add(schema);
                }
            }
        }
        return schema;
    }
    */


    private Element parseSchema(DocumentBuilder documentBuilder,
                               Resource schemaResource) throws IOException, SAXException {
        Document schemaDocument = documentBuilder.parse(SaxUtils.createInputSource(schemaResource));
        Element schema = schemaDocument.getDocumentElement();
        inlineIncludes(documentBuilder, schemaResource, schema);
        return schema;
    }

    private void inlineIncludes(DocumentBuilder documentBuilder, Resource schemaResource, Element schema)
            throws IOException, SAXException {
        List includes = getChildElements(schema, XSD_NAMESPACE_URI, "include");
        for (Iterator iterator = includes.iterator(); iterator.hasNext();) {
            Element include = (Element) iterator.next();
            String schemaLocation = include.getAttribute("schemaLocation");
            Assert.hasText(schemaLocation, schemaResource + " <include/> has no schemaLocation attribute");
            Resource includedResource = schemaResource.createRelative(schemaLocation);
            Assert.isTrue(includedResource.exists(), includedResource + " does not exist");
            String targetNamespace = schema.getAttribute("targetNamespace");
            Element includedSchemaElement = parseSchema(documentBuilder, includedResource);
            NodeList children = includedSchemaElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node importedChild = schema.getOwnerDocument().importNode(children.item(i), true);
                schema.appendChild(importedChild);
            }
            NamedNodeMap attributes = includedSchemaElement.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                if ("http://www.w3.org/2000/xmlns/".equals(attribute.getNamespaceURI()) &&
                    "xmlns".equals(attribute.getPrefix())) {
                    Attr importedAttr = (Attr) schema.getOwnerDocument().importNode(attribute, true);
                    schema.setAttributeNode(importedAttr);
                }
            }
            schema.removeChild(include);
        }
    }


}
