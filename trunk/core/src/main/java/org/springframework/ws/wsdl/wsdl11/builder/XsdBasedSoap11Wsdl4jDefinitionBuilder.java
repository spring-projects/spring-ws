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

package org.springframework.ws.wsdl.wsdl11.builder;

import java.io.IOException;
import java.util.Iterator;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.sax.SaxUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Builds a <code>WsdlDefinition</code> with a SOAP 1.1 binding based on an XSD schema. This builder iterates over all
 * <code>element</code>s found in the schema, and creates a <code>message</code> for those elements that end with the
 * request or response suffix. It combines these messages into <code>operation</code>s, and builds a
 * <code>portType</code> based on the operations. The schema itself is inlined in a <code>types</code> block.
 * <p/>
 * Requires the <code>schema</code> and <code>portTypeName</code> properties to be set.
 *
 * @author Arjen Poutsma
 * @see #setSchema(org.springframework.core.io.Resource)
 * @see #setPortTypeName(String)
 * @see #setRequestSuffix(String)
 * @see #setResponseSuffix(String)
 */
public class XsdBasedSoap11Wsdl4jDefinitionBuilder extends AbstractSoap11Wsdl4jDefinitionBuilder
        implements InitializingBean {

    /** The schema namespace URI. */
    private static final String SCHEMA_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";

    /** The default suffix used to detect request elements in the schema. */
    public static final String DEFAULT_REQUEST_SUFFIX = "Request";

    /** The default suffix used to detect response elements in the schema. */
    public static final String DEFAULT_RESPONSE_SUFFIX = "Response";

    /** The default prefix used to register the schema namespace in the WSDL. */
    public static final String DEFAULT_SCHEMA_PREFIX = "schema";

    /** The default prefix used to register the target namespace in the WSDL. */
    public static final String DEFAULT_PREFIX = "tns";

    /** The suffix used to create a service name from a port type name. */
    public static final String SERVICE_SUFFIX = "Service";

    private Resource schema;

    private Element schemaElement;

    private String targetNamespace;

    private String portTypeName;

    private String schemaPrefix = DEFAULT_SCHEMA_PREFIX;

    private String prefix = DEFAULT_PREFIX;

    private String requestSuffix = DEFAULT_REQUEST_SUFFIX;

    private String responseSuffix = DEFAULT_RESPONSE_SUFFIX;

    /**
     * Sets the suffix used to detect request elements in the schema.
     *
     * @see #DEFAULT_REQUEST_SUFFIX
     */
    public void setRequestSuffix(String requestSuffix) {
        this.requestSuffix = requestSuffix;
    }

    /**
     * Sets the suffix used to detect response elements in the schema.
     *
     * @see #DEFAULT_RESPONSE_SUFFIX
     */
    public void setResponseSuffix(String responseSuffix) {
        this.responseSuffix = responseSuffix;
    }

    /** Sets the port type name used for this definition. Required. */
    public void setPortTypeName(String portTypeName) {
        this.portTypeName = portTypeName;
    }

    /** Sets the target namespace used for this definition. */
    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    /**
     * Sets the prefix used to declare the schema target namespace.
     *
     * @see #DEFAULT_SCHEMA_PREFIX
     */
    public void setSchemaPrefix(String schemaPrefix) {
        this.schemaPrefix = schemaPrefix;
    }

    /**
     * Sets the prefix used to declare the target namespace.
     *
     * @see #DEFAULT_PREFIX
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /** Sets the XSD schema to use for generating the WSDL. */
    public void setSchema(Resource schema) {
        Assert.notNull(schema, "schema must not be empty or null");
        Assert.isTrue(schema.exists(), "schema \"" + schema + "\" does not exit");
        this.schema = schema;
    }

    public final void afterPropertiesSet() throws IOException, ParserConfigurationException, SAXException {
        Assert.notNull(schema, "schema is required");
        Assert.notNull(portTypeName, "portTypeName is required");
        parseSchema();
    }

    private void parseSchema() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document schemaDocument = documentBuilder.parse(SaxUtils.createInputSource(schema));
        schemaElement = schemaDocument.getDocumentElement();
        Assert.isTrue("schema".equals(schemaElement.getLocalName()),
                "schema document root element has invalid local name : [" + schemaElement.getLocalName() +
                        "] instead of [schema]");
        Assert.isTrue(SCHEMA_NAMESPACE_URI.equals(schemaElement.getNamespaceURI()),
                "schema document root element has invalid namespace uri: [" + schemaElement.getNamespaceURI() +
                        "] instead of [" + SCHEMA_NAMESPACE_URI + "]");
        String schemaTargetNamespace = getSchemaTargetNamespace();
        Assert.hasLength(schemaTargetNamespace, "schema document has no targetNamespace");
        if (!StringUtils.hasLength(targetNamespace)) {
            targetNamespace = schemaTargetNamespace;
        }
    }

    private String getSchemaTargetNamespace() {
        return schemaElement.getAttribute("targetNamespace");
    }

    /** Adds the target namespace and schema namespace to the definition. */
    protected void populateDefinition(Definition definition) throws WSDLException {
        super.populateDefinition(definition);
        definition.setTargetNamespace(targetNamespace);
        definition.addNamespace(schemaPrefix, getSchemaTargetNamespace());
        if (!targetNamespace.equals(getSchemaTargetNamespace())) {
            definition.addNamespace(prefix, targetNamespace);
        }
    }

    /** Does nothing. */
    protected void buildImports(Definition definition) throws WSDLException {
    }

    /**
     * Creates a <code>Types</code> object that is populated with the types found in the schema.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     */
    protected void buildTypes(Definition definition) throws WSDLException {
        Types types = definition.createTypes();
        Schema schema = (Schema) createExtension(Types.class, QNameUtils.getQNameForNode(schemaElement));
        schema.setElement(schemaElement);
        types.addExtensibilityElement(schema);
        definition.setTypes(types);
    }

    /**
     * Creates messages for each element found in the schema for which <code>isRequestMessage()</code> or
     * <code>isResponseMessage()</code> is <code>true</code>.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     * @see #isRequestMessage(javax.xml.namespace.QName)
     * @see #isResponseMessage(javax.xml.namespace.QName)
     */
    protected void buildMessages(Definition definition) throws WSDLException {
        NodeList elements = schemaElement.getElementsByTagNameNS(SCHEMA_NAMESPACE_URI, "element");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            QName elementName = getSchemaElementName(element);
            if (elementName != null && (isRequestMessage(elementName) || isResponseMessage(elementName))) {
                Message message = definition.createMessage();
                populateMessage(message, element);
                Part part = definition.createPart();
                populatePart(part, elementName);
                message.addPart(part);
                message.setUndefined(false);
                definition.addMessage(message);
            }
        }
    }

    /**
     * Indicates whether the given name name should be included as request <code>Message</code> in the definition.
     * Default implementation checks whether the local part ends with the request suffix.
     *
     * @param name the name of the element elligable for being a message
     * @return <code>true</code> if to be included as message; <code>false</code> otherwise
     * @see #setRequestSuffix(String)
     */
    protected boolean isRequestMessage(QName name) {
        return name.getLocalPart().endsWith(requestSuffix);
    }

    /**
     * Indicates whether the given name should be included as <code>Message</code> in the definition. Default
     * implementation checks whether the local part ends with the response suffix.
     *
     * @param name the name of the element elligable for being a message
     * @return <code>true</code> if to be included as message; <code>false</code> otherwise
     * @see #setResponseSuffix(String)
     */
    protected boolean isResponseMessage(QName name) {
        return name.getLocalPart().endsWith(responseSuffix);
    }

    /**
     * Called after the <code>Message</code> has been created.
     * <p/>
     * Default implementation sets the name of the message to the element name.
     *
     * @param message the WSDL4J <code>Message</code>
     * @param element the element
     * @throws WSDLException in case of errors
     */
    protected void populateMessage(Message message, Element element) {
        message.setQName(new QName(targetNamespace, element.getAttribute("name")));
    }

    /**
     * Called after the <code>Part</code> has been created.
     * <p/>
     * Default implementation sets the element name of the part.
     *
     * @param part        the WSDL4J <code>Part</code>
     * @param elementName the elementName
     * @throws WSDLException in case of errors
     * @see Part#setElementName(javax.xml.namespace.QName)
     */
    protected void populatePart(Part part, QName elementName) {
        part.setElementName(elementName);
        part.setName(elementName.getLocalPart());
    }

    protected void buildPortTypes(Definition definition) throws WSDLException {
        PortType portType = definition.createPortType();
        populatePortType(portType);
        createOperations(definition, portType);
        portType.setUndefined(false);
        definition.addPortType(portType);
    }

    /**
     * Called after the <code>PortType</code> has been created.
     * <p/>
     * Default implementation sets the name of the port type to the defined value.
     *
     * @param portType the WSDL4J <code>PortType</code>
     * @throws WSDLException in case of errors
     * @see #setPortTypeName(String)
     */
    protected void populatePortType(PortType portType) throws WSDLException {
        portType.setQName(new QName(targetNamespace, portTypeName));
    }

    private void createOperations(Definition definition, PortType portType) throws WSDLException {
        for (Iterator messageIterator = definition.getMessages().values().iterator(); messageIterator.hasNext();) {
            Message message = (Message) messageIterator.next();
            for (Iterator partIterator = message.getParts().values().iterator(); partIterator.hasNext();) {
                Part part = (Part) partIterator.next();
                if (isRequestMessage(part.getElementName())) {
                    Message responseMessage = definition.getMessage(getResponseMessageName(message.getQName()));
                    Operation operation = definition.createOperation();
                    populateOperation(operation, message, responseMessage);
                    if (message != null) {
                        Input input = definition.createInput();
                        input.setMessage(message);
                        input.setName(message.getQName().getLocalPart());
                        operation.setInput(input);
                    }
                    if (responseMessage != null) {
                        Output output = definition.createOutput();
                        output.setMessage(responseMessage);
                        output.setName(responseMessage.getQName().getLocalPart());
                        operation.setOutput(output);
                    }
                    operation.setUndefined(false);
                    portType.addOperation(operation);
                }
            }
        }
    }

    /**
     * Given an request message name, return the corresponding response message name.
     * <p/>
     * Default implementation removes the request suffix, and appends the response suffix.
     *
     * @param requestMessageName the name of the request message
     * @return the name of the corresponding response message, or null
     */
    protected QName getResponseMessageName(QName requestMessageName) {
        String localPart = requestMessageName.getLocalPart();
        if (localPart.endsWith(requestSuffix)) {
            String prefix = localPart.substring(0, localPart.length() - requestSuffix.length());
            return new QName(requestMessageName.getNamespaceURI(), prefix + responseSuffix);
        }
        else {
            return null;
        }
    }

    /**
     * Called after the <code>Operation</code> has been created.
     * <p/>
     * Default implementation sets the name of the operation to name of the messages, without suffix.
     *
     * @param operation       the WSDL4J <code>Operation</code>
     * @param requestMessage  the WSDL4J request <code>Message</code>
     * @param responseMessage the WSDL4J response <code>Message</code>
     * @throws WSDLException in case of errors
     * @see #setPortTypeName(String)
     */
    protected void populateOperation(Operation operation, Message requestMessage, Message responseMessage)
            throws WSDLException {
        String localPart = requestMessage.getQName().getLocalPart();
        String operationName = null;
        if (localPart.endsWith(requestSuffix)) {
            operationName = localPart.substring(0, localPart.length() - requestSuffix.length());
        }
        else {
            localPart = responseMessage.getQName().getLocalPart();
            if (localPart.endsWith(responseSuffix)) {
                operationName = localPart.substring(0, localPart.length() - responseSuffix.length());
                operationName = localPart;
            }
        }
        operation.setName(operationName);
    }

    /** Sets the name of the service to the name of the port type, with "Service" appended to it. */
    protected void populateService(Service service) throws WSDLException {
        service.setQName(new QName(targetNamespace, portTypeName + SERVICE_SUFFIX));
    }

    /**
     * Returns the qualified name of the element. This is a combination of schema target namespace and the value of the
     * "name" attribute value.
     *
     * @param element an element
     * @return the value of the name attribute
     */
    private QName getSchemaElementName(Element element) {
        String attributeValue = element.getAttribute("name");
        if (StringUtils.hasLength(attributeValue)) {
            return new QName(getSchemaTargetNamespace(), attributeValue);
        }
        else {
            return null;
        }
    }
}
