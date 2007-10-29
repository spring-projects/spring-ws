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
import java.util.List;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
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
import org.springframework.ws.wsdl.wsdl11.DynamicWsdl11Definition;
import org.springframework.xml.namespace.QNameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Builds a <code>WsdlDefinition</code> with a SOAP 1.1 binding based on an XSD schema. This builder iterates over all
 * <code>element</code>s found in the schema, and creates a <code>message</code> for those elements that end with the
 * request or response suffix. It combines these messages into <code>operation</code>s, and builds a
 * <code>portType</code> based on the operations.
 * <p/>
 * By default, the schema file is inlined in a <code>types</code> block. However, if the <code>schemaLocation</code>
 * property is set, an XSD <code>import</code> is used instead. As such, the imported schema file can contain further
 * imports, which will be resolved correctly in accordance with the schema location.
 * <p/>
 * Typically used within a {@link DynamicWsdl11Definition}, like so:
 * <pre>
 * &lt;bean id=&quot;airline&quot; class=&quot;org.springframework.ws.wsdl.wsdl11.DynamicWsdl11Definition&quot;&gt;
 *   &lt;property name=&quot;builder&quot;&gt;
 *     &lt;bean class=&quot;org.springframework.ws.wsdl.wsdl11.builder.XsdBasedSoap11Wsdl4jDefinitionBuilder&quot;&gt;
 *     &lt;property name=&quot;schema&quot; value=&quot;/WEB-INF/airline.xsd&quot;/&gt;
 *     &lt;property name=&quot;portTypeName&quot; value=&quot;Airline&quot;/&gt;
 *     &lt;property name=&quot;locationUri&quot; value=&quot;http://localhost:8080/airline/services&quot;/&gt;
 *     &lt;/bean&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * <p/>
 * Requires the <code>schema</code> and <code>portTypeName</code> properties to be set.
 *
 * @author Arjen Poutsma
 * @see #setSchema(org.springframework.core.io.Resource)
 * @see #setPortTypeName(String)
 * @see #setRequestSuffix(String)
 * @see #setResponseSuffix(String)
 * @since 1.0.0
 */
public class XsdBasedSoap11Wsdl4jDefinitionBuilder extends AbstractSoap11Wsdl4jDefinitionBuilder
        implements InitializingBean {

    /** The default suffix used to detect request elements in the schema. */
    public static final String DEFAULT_REQUEST_SUFFIX = "Request";

    /** The default suffix used to detect response elements in the schema. */
    public static final String DEFAULT_RESPONSE_SUFFIX = "Response";

    /** The default suffix used to detect fault elements in the schema. */
    public static final String DEFAULT_FAULT_SUFFIX = "Fault";

    /** The default prefix used to register the schema namespace in the WSDL. */
    public static final String DEFAULT_SCHEMA_PREFIX = "schema";

    /** The default prefix used to register the target namespace in the WSDL. */
    public static final String DEFAULT_PREFIX = "tns";

    /** The suffix used to create a service name from a port type name. */
    public static final String SERVICE_SUFFIX = "Service";

    private Resource schemaResource;

    private XsdSchemaHelper schemaHelper;

    private String schemaLocation;

    private String targetNamespace;

    private String portTypeName;

    private String schemaPrefix = DEFAULT_SCHEMA_PREFIX;

    private String prefix = DEFAULT_PREFIX;

    private String requestSuffix = DEFAULT_REQUEST_SUFFIX;

    private String responseSuffix = DEFAULT_RESPONSE_SUFFIX;

    private String faultSuffix = DEFAULT_FAULT_SUFFIX;

    private boolean followIncludeImport = false;

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

    /**
     * Sets the suffix used to detect fault elements in the schema.
     *
     * @see #DEFAULT_FAULT_SUFFIX
     */
    public void setFaultSuffix(String faultSuffix) {
        this.faultSuffix = faultSuffix;
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
    public void setSchema(Resource schemaResource) {
        Assert.notNull(schemaResource, "'schema' must not be null");
        Assert.isTrue(schemaResource.exists(), "schema \"" + schemaResource + "\" does not exit");
        this.schemaResource = schemaResource;
    }

    /**
     * Sets the location of the schema to import. If this property is set, the <code>schema</code> element in the
     * generated WSDL will only contain an <code>import</code>, referring to the value of this property.
     */
    public void setSchemaLocation(String schemaLocation) {
        Assert.hasLength(schemaLocation, "'schemaLocation' must not be empty");
        this.schemaLocation = schemaLocation;
    }

    /**
     * Indicates whether schema <code>&lt;xsd:include/&gt;</code> and <code>&lt;xsd:import/&gt;</code> should be
     * followed.
     */
    public void setFollowIncludeImport(boolean followIncludeImport) {
        this.followIncludeImport = followIncludeImport;
    }

    public final void afterPropertiesSet() throws IOException, ParserConfigurationException, SAXException {
        Assert.notNull(schemaResource, "'schema' is required");
        Assert.notNull(portTypeName, "'portTypeName' is required");
        schemaHelper = new XsdSchemaHelper(schemaResource);
        if (!StringUtils.hasLength(targetNamespace)) {
            targetNamespace = schemaHelper.getTargetNamespace();
        }
    }

    /** Adds the target namespace and schema namespace to the definition. */
    protected void populateDefinition(Definition definition) throws WSDLException {
        super.populateDefinition(definition);
        definition.setTargetNamespace(targetNamespace);
        definition.addNamespace(schemaPrefix, schemaHelper.getTargetNamespace());
        if (!targetNamespace.equals(schemaHelper.getTargetNamespace())) {
            definition.addNamespace(prefix, targetNamespace);
        }
    }

    /** Does nothing. */
    protected void buildImports(Definition definition) throws WSDLException {
    }

    /**
     * Creates a {@link Types} object containing a {@link Schema}. By default, the schema set by the <code>schema</code>
     * property will be inlined into this <code>type</code>. If the <code>schemaLocation</code> is set, </code>object
     * that is populated with the types found in the schema.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     */
    protected void buildTypes(Definition definition) throws WSDLException {
        Types types = definition.createTypes();
        Schema schema = (Schema) createExtension(Types.class, XsdSchemaHelper.SCHEMA_NAME);
        if (!StringUtils.hasLength(schemaLocation)) {
            schema.setElement(schemaHelper.getSchemaElement());
        }
        else {
            Document document;
            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                document = documentBuilder.newDocument();
            }
            catch (ParserConfigurationException ex) {
                throw new WSDLException(WSDLException.PARSER_ERROR, "Could not create DocumentBuilder", ex);
            }
            Element importingSchemaElement = document.createElementNS(XsdSchemaHelper.SCHEMA_NAME.getNamespaceURI(),
                    QNameUtils.toQualifiedName(XsdSchemaHelper.SCHEMA_NAME));
            schema.setElement(importingSchemaElement);
            Element importElement = document.createElementNS(XsdSchemaHelper.IMPORT_NAME.getNamespaceURI(),
                    QNameUtils.toQualifiedName(XsdSchemaHelper.IMPORT_NAME));
            importingSchemaElement.appendChild(importElement);
            importElement.setAttribute("namespace", schemaHelper.getTargetNamespace());
            importElement.setAttribute("schemaLocation", schemaLocation);
        }
        types.addExtensibilityElement(schema);
        definition.setTypes(types);
    }

    /**
     * Creates messages for each element found in the schema for which <code>isRequestMessage()</code>,
     * <code>isResponseMessage()</code>, or <code>isFaultMessage()</code> is <code>true</code>.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     * @see #isRequestMessage(javax.xml.namespace.QName)
     * @see #isResponseMessage(javax.xml.namespace.QName)
     * @see #isFaultMessage(javax.xml.namespace.QName)
     */
    protected void buildMessages(Definition definition) throws WSDLException {
        List elementDeclarations = schemaHelper.getElementDeclarations(followIncludeImport);
        for (Iterator iterator = elementDeclarations.iterator(); iterator.hasNext();) {
            QName elementName = (QName) iterator.next();
            if (elementName != null &&
                    (isRequestMessage(elementName) || isResponseMessage(elementName) || isFaultMessage(elementName))) {
                if (!StringUtils.hasLength(definition.getPrefix(elementName.getNamespaceURI()))) {
                    int i = 0;
                    while (true) {
                        String prefix = schemaPrefix + Integer.toString(i);
                        if (!StringUtils.hasLength(definition.getNamespace(prefix))) {
                            definition.addNamespace(prefix, elementName.getNamespaceURI());
                            break;
                        }
                    }
                }
                Message message = definition.createMessage();
                populateMessage(message, elementName);
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
     * Indicates whether the given name should be included as <code>Message</code> in the definition. Default
     * implementation checks whether the local part ends with the fault suffix.
     *
     * @param name the name of the element elligable for being a message
     * @return <code>true</code> if to be included as message; <code>false</code> otherwise
     * @see #setFaultSuffix(String)
     */
    protected boolean isFaultMessage(QName name) {
        return name.getLocalPart().endsWith(faultSuffix);
    }

    /**
     * Called after the <code>Message</code> has been created.
     * <p/>
     * Default implementation sets the name of the message to the element name.
     *
     * @param message     the WSDL4J <code>Message</code>
     * @param elementName the element name
     * @throws WSDLException in case of errors
     */
    protected void populateMessage(Message message, QName elementName) throws WSDLException {
        message.setQName(new QName(targetNamespace, elementName.getLocalPart()));
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
    protected void populatePart(Part part, QName elementName) throws WSDLException {
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
                    Message requestMessage = message;
                    Message responseMessage = definition.getMessage(getResponseMessageName(requestMessage.getQName()));
                    Message faultMessage = definition.getMessage(getFaultMessageName(requestMessage.getQName()));
                    Operation operation = definition.createOperation();
                    populateOperation(operation, requestMessage, responseMessage);
                    if (requestMessage != null) {
                        Input input = definition.createInput();
                        input.setMessage(requestMessage);
                        input.setName(requestMessage.getQName().getLocalPart());
                        operation.setInput(input);
                    }
                    if (responseMessage != null) {
                        Output output = definition.createOutput();
                        output.setMessage(responseMessage);
                        output.setName(responseMessage.getQName().getLocalPart());
                        operation.setOutput(output);
                    }
                    if (faultMessage != null) {
                        Fault fault = definition.createFault();
                        fault.setMessage(faultMessage);
                        fault.setName(faultMessage.getQName().getLocalPart());
                        operation.addFault(fault);
                    }
                    if (requestMessage != null && responseMessage != null) {
                        operation.setStyle(OperationType.REQUEST_RESPONSE);
                    }
                    else if (requestMessage != null && responseMessage == null) {
                        operation.setStyle(OperationType.ONE_WAY);
                    }
                    else if (requestMessage == null && responseMessage != null) {
                        operation.setStyle(OperationType.NOTIFICATION);
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
     * Given an request message name, return the corresponding fault message name.
     * <p/>
     * Default implementation removes the request suffix, and appends the fault suffix.
     *
     * @param requestMessageName the name of the request message
     * @return the name of the corresponding response message, or null
     */
    protected QName getFaultMessageName(QName requestMessageName) {
        String localPart = requestMessageName.getLocalPart();
        if (localPart.endsWith(requestSuffix)) {
            String prefix = localPart.substring(0, localPart.length() - requestSuffix.length());
            return new QName(requestMessageName.getNamespaceURI(), prefix + faultSuffix);
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
            }
        }
        operation.setName(operationName);
    }

    /** Sets the name of the service to the name of the port type, with "Service" appended to it. */
    protected void populateService(Service service) throws WSDLException {
        service.setQName(new QName(targetNamespace, portTypeName + SERVICE_SUFFIX));
    }
}
