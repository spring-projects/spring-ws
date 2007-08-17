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

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;

/**
 * Abstract base class for <code>Wsdl11DefinitionBuilder</code> implementations that use WSDL4J and contain a SOAP 1.1
 * binding. Requires the <code>locationUri</code> property to be set before use.
 *
 * @author Arjen Poutsma
 * @see #setLocationUri(String)
 * @since 1.0.0
 */
public abstract class AbstractSoap11Wsdl4jDefinitionBuilder extends AbstractBindingWsdl4jDefinitionBuilder {

    private static final String WSDL_SOAP_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/soap/";

    private static final String WSDL_SOAP_PREFIX = "soap";

    /** The default soap:binding transport attribute value. */
    public static final String DEFAULT_TRANSPORT_URI = "http://schemas.xmlsoap.org/soap/http";

    private String transportUri = DEFAULT_TRANSPORT_URI;

    private String locationUri;

    /**
     * Sets the value used for the soap:binding transport attribute value.
     *
     * @see javax.wsdl.extensions.soap.SOAPBinding#setTransportURI(String)
     * @see #DEFAULT_TRANSPORT_URI
     */
    public void setTransportUri(String transportUri) {
        this.transportUri = transportUri;
    }

    /** Sets the value used for the soap:address location attribute value. */
    public void setLocationUri(String locationUri) {
        this.locationUri = locationUri;
    }

    /** Adds the WSDL SOAP namespace to the definition. */
    protected void populateDefinition(Definition definition) throws WSDLException {
        definition.addNamespace(WSDL_SOAP_PREFIX, WSDL_SOAP_NAMESPACE_URI);
    }

    /**
     * Calls <code>populateBindingInternal()</code>, creates <code>SOAPBinding</code>, and calls
     * <code>populateSoapBinding()</code>.
     *
     * @param binding  the WSDL4J <code>Binding</code>
     * @param portType the corresponding <code>PortType</code>
     * @throws WSDLException in case of errors
     * @see javax.wsdl.extensions.soap.SOAPBinding
     * @see #populateSoapBinding(javax.wsdl.extensions.soap.SOAPBinding)
     */
    protected void populateBinding(Binding binding, PortType portType) throws WSDLException {
        super.populateBinding(binding, portType);
        SOAPBinding soapBinding = (SOAPBinding) createSoapExtension(Binding.class, "binding");
        populateSoapBinding(soapBinding);
        binding.addExtensibilityElement(soapBinding);
    }

    /**
     * Called after the <code>SOAPBinding</code> has been created. Default implementation sets the binding style to
     * <code>"document"</code>, and set the transport URI to the value set on this builder. Subclasses can override this
     * behavior.
     *
     * @param soapBinding the WSDL4J <code>SOAPBinding</code>
     * @throws WSDLException in case of errors
     * @see javax.wsdl.extensions.soap.SOAPBinding#setStyle(String)
     * @see javax.wsdl.extensions.soap.SOAPBinding#setTransportURI(String)
     * @see #setTransportUri(String)
     * @see #DEFAULT_TRANSPORT_URI
     */
    protected void populateSoapBinding(SOAPBinding soapBinding) throws WSDLException {
        soapBinding.setStyle("document");
        soapBinding.setTransportURI(transportUri);
    }

    /**
     * Calls <code>getBindingOperationName()</code>, creates a <code>SOAPOperation</code>, and calls
     * <code>populateSoapOperation()</code>.
     *
     * @param bindingOperation the WSDL4J <code>BindingOperation</code>
     * @throws WSDLException in case of errors
     * @see javax.wsdl.extensions.soap.SOAPOperation
     * @see #populateSoapOperation(javax.wsdl.extensions.soap.SOAPOperation)
     */
    protected void populateBindingOperation(BindingOperation bindingOperation, Operation operation)
            throws WSDLException {
        super.populateBindingOperation(bindingOperation, operation);
        SOAPOperation soapOperation = (SOAPOperation) createSoapExtension(BindingOperation.class, "operation");
        populateSoapOperation(soapOperation);
        bindingOperation.addExtensibilityElement(soapOperation);
    }

    /**
     * Called after the <code>SOAPOperation</code> has been created.
     * <p/>
     * Default implementation set the <code>SOAPAction</code> uri to an empty string.
     *
     * @param soapOperation the WSDL4J <code>SOAPOperation</code>
     * @throws WSDLException in case of errors
     * @see javax.wsdl.extensions.soap.SOAPOperation#setSoapActionURI(String)
     */
    protected void populateSoapOperation(SOAPOperation soapOperation) throws WSDLException {
        soapOperation.setSoapActionURI("");
    }

    /**
     * Creates a <code>SOAPBody</code>, and calls <code>populateSoapBody()</code>.
     *
     * @param bindingInput the WSDL4J <code>BindingInput</code>
     * @throws WSDLException in case of errors
     * @see javax.wsdl.extensions.soap.SOAPOperation
     * @see #populateSoapBody(javax.wsdl.extensions.soap.SOAPBody)
     */
    protected void populateBindingInput(BindingInput bindingInput, Input input) throws WSDLException {
        super.populateBindingInput(bindingInput, input);
        SOAPBody soapBody = (SOAPBody) createSoapExtension(BindingInput.class, "body");
        populateSoapBody(soapBody);
        bindingInput.addExtensibilityElement(soapBody);
    }

    /**
     * Creates a <code>SOAPBody</code>, and calls <code>populateSoapBody()</code>.
     *
     * @param bindingOutput the WSDL4J <code>BindingOutput</code>
     * @throws javax.wsdl.WSDLException in case of errors
     * @see javax.wsdl.extensions.soap.SOAPOperation
     * @see #populateSoapBody(javax.wsdl.extensions.soap.SOAPBody)
     */
    protected void populateBindingOutput(BindingOutput bindingOutput, Output output) throws WSDLException {
        super.populateBindingOutput(bindingOutput, output);
        SOAPBody soapBody = (SOAPBody) createSoapExtension(BindingOutput.class, "body");
        populateSoapBody(soapBody);
        bindingOutput.addExtensibilityElement(soapBody);
    }

    /**
     * Creates a <code>SOAPBody</code>, and calls <code>populateSoapBody()</code>.
     *
     * @param bindingFault the WSDL4J <code>BindingFault</code>
     * @throws javax.wsdl.WSDLException in case of errors
     * @see javax.wsdl.extensions.soap.SOAPOperation
     * @see #populateSoapBody(javax.wsdl.extensions.soap.SOAPBody)
     */
    protected void populateBindingFault(BindingFault bindingFault, Fault fault) throws WSDLException {
        super.populateBindingFault(bindingFault, fault);
        SOAPFault soapFault = (SOAPFault) createSoapExtension(BindingFault.class, "fault");
        populateSoapFault(bindingFault, soapFault);
        bindingFault.addExtensibilityElement(soapFault);
    }

    /**
     * Called after the <code>SOAPBody</code> has been created. Default implementation sets the use style to
     * <code>"literal"</code>. Subclasses can override this behavior.
     *
     * @param soapBody the WSDL4J <code>SOAPBody</code>
     * @throws WSDLException in case of errors
     * @see javax.wsdl.extensions.soap.SOAPBody#setUse(String)
     */
    protected void populateSoapBody(SOAPBody soapBody) throws WSDLException {
        soapBody.setUse("literal");
    }

    /**
     * Called after the <code>SOAPFault</code> has been created. Default implementation sets the use style to
     * <code>"literal"</code>, and sets the name equal to the binding fault. Subclasses can override this behavior.
     *
     * @param bindingFault the WSDL4J <code>BindingFault</code>
     * @param soapFault    the WSDL4J <code>SOAPFault</code>
     * @throws WSDLException in case of errors
     * @see javax.wsdl.extensions.soap.SOAPBody#setUse(String)
     */
    protected void populateSoapFault(BindingFault bindingFault, SOAPFault soapFault) throws WSDLException {
        soapFault.setName(bindingFault.getName());
        soapFault.setUse("literal");
    }

    /**
     * Creates a <code>SOAPAddress</code>, and calls <code>populateSoapAddress()</code>.
     *
     * @param port the WSDL4J <code>Port</code>
     * @throws javax.wsdl.WSDLException in case of errors
     * @see javax.wsdl.extensions.soap.SOAPAddress
     * @see #populateSoapBody(javax.wsdl.extensions.soap.SOAPBody)
     */
    protected void populatePort(Port port, Binding binding) throws WSDLException {
        super.populatePort(port, binding);
        SOAPAddress soapAddress = (SOAPAddress) createSoapExtension(Port.class, "address");
        populateSoapAddress(soapAddress);
        port.addExtensibilityElement(soapAddress);
    }

    /**
     * Called after the <code>SOAPAddress</code> has been created. Default implementation sets the location URI to the
     * value set on this builder. Subclasses can override this behavior.
     *
     * @param soapAddress the WSDL4J <code>SOAPAddress</code>
     * @throws WSDLException in case of errors
     * @see javax.wsdl.extensions.soap.SOAPAddress#setLocationURI(String)
     * @see #setLocationUri(String)
     */
    protected void populateSoapAddress(SOAPAddress soapAddress) throws WSDLException {
        soapAddress.setLocationURI(locationUri);
    }

    /**
     * Creates a SOAP extensibility element.
     *
     * @param parentType a class object indicating where in the WSDL definition this extension will exist
     * @param localName  the local name of the extensibility element
     * @return the extensibility element
     * @throws WSDLException in case of errors
     * @see javax.wsdl.extensions.ExtensionRegistry#createExtension(Class,javax.xml.namespace.QName)
     */
    protected ExtensibilityElement createSoapExtension(Class parentType, String localName) throws WSDLException {
        return createExtension(parentType, new QName(WSDL_SOAP_NAMESPACE_URI, localName));
    }

}
