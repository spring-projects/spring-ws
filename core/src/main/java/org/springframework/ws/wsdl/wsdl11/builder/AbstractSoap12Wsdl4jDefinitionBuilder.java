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
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Fault;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.xml.namespace.QName;

/**
 * Abstract base class for <code>Wsdl11DefinitionBuilder</code> implementations that use WSDL4J and contain a SOAP 1.2
 * binding. Requires the <code>locationUri</code> property to be set before use.
 *
 * @author Arjen Poutsma
 * @author Alex Marshall
 * @see #setLocationUri(String)
 * @since 1.1.0
 */
public abstract class AbstractSoap12Wsdl4jDefinitionBuilder extends AbstractBindingWsdl4jDefinitionBuilder {

    private static final String WSDL_SOAP_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/soap12/";

    private static final String WSDL_SOAP_PREFIX = "soap12";

    /** The default soap12:binding transport attribute value. */
    public static final String DEFAULT_TRANSPORT_URI = "http://schemas.xmlsoap.org/soap/http";

    private String transportUri = DEFAULT_TRANSPORT_URI;

    private String locationUri;

    /**
     * Sets the value used for the soap12:binding transport attribute value.
     *
     * @see SOAP12Binding#setTransportURI(String)
     * @see #DEFAULT_TRANSPORT_URI
     */
    public void setTransportUri(String transportUri) {
        this.transportUri = transportUri;
    }

    /** Sets the value used for the soap12:address location attribute value. */
    public void setLocationUri(String locationUri) {
        this.locationUri = locationUri;
    }

    /** Adds the WSDL SOAP namespace to the definition. */
    protected void populateDefinition(Definition definition) throws WSDLException {
        definition.addNamespace(WSDL_SOAP_PREFIX, WSDL_SOAP_NAMESPACE_URI);
    }

    /**
     * Calls {@link AbstractBindingWsdl4jDefinitionBuilder#populateBinding(Binding, PortType)}, creates {@link
     * SOAP12Binding}, and calls {@link #populateSoapBinding(SOAP12Binding)}.
     *
     * @param binding  the WSDL4J <code>Binding</code>
     * @param portType the corresponding <code>PortType</code>
     * @throws WSDLException in case of errors
     */
    protected void populateBinding(Binding binding, PortType portType) throws WSDLException {
        super.populateBinding(binding, portType);
        SOAP12Binding soapBinding = (SOAP12Binding) createSoapExtension(Binding.class, "binding");
        populateSoapBinding(soapBinding);
        binding.addExtensibilityElement(soapBinding);
    }

    /**
     * Called after the {@link SOAP12Binding} has been created. Default implementation sets the binding style to
     * <code>"document"</code>, and set the transport URI to the value set on this builder. Subclasses can override this
     * behavior.
     *
     * @param soapBinding the WSDL4J <code>SOAP12Binding</code>
     * @throws WSDLException in case of errors
     * @see SOAP12Binding#setStyle(String)
     * @see SOAP12Binding#setTransportURI(String)
     * @see #setTransportUri(String)
     * @see #DEFAULT_TRANSPORT_URI
     */
    protected void populateSoapBinding(SOAP12Binding soapBinding) throws WSDLException {
        soapBinding.setStyle("document");
        soapBinding.setTransportURI(transportUri);
    }

    /**
     * Calls {@link AbstractBindingWsdl4jDefinitionBuilder#populateBindingOperation(BindingOperation, Operation)},
     * creates a {@link SOAP12Operation}, and calls {@link #populateSoapOperation(SOAP12Operation)}.
     *
     * @param bindingOperation the WSDL4J <code>BindingOperation</code>
     * @throws WSDLException in case of errors
     */
    protected void populateBindingOperation(BindingOperation bindingOperation, Operation operation)
            throws WSDLException {
        super.populateBindingOperation(bindingOperation, operation);
        SOAP12Operation soapOperation = (SOAP12Operation) createSoapExtension(BindingOperation.class, "operation");
        populateSoapOperation(soapOperation);
        bindingOperation.addExtensibilityElement(soapOperation);
    }

    /**
     * Called after the {@link SOAP12Operation} has been created.
     * <p/>
     * Default implementation set the <code>SOAPAction</code> uri to an empty string.
     *
     * @param soapOperation the WSDL4J <code>SOAP12Operation</code>
     * @throws WSDLException in case of errors
     * @see SOAP12Operation#setSoapActionURI(String)
     */
    protected void populateSoapOperation(SOAP12Operation soapOperation) throws WSDLException {
        soapOperation.setSoapActionURI("");
    }

    /**
     * Creates a {@link SOAP12Body}, and calls {@link #populateSoapBody(SOAP12Body)}.
     *
     * @param bindingInput the WSDL4J <code>BindingInput</code>
     * @throws WSDLException in case of errors
     */
    protected void populateBindingInput(BindingInput bindingInput, Input input) throws WSDLException {
        super.populateBindingInput(bindingInput, input);
        SOAP12Body soapBody = (SOAP12Body) createSoapExtension(BindingInput.class, "body");
        populateSoapBody(soapBody);
        bindingInput.addExtensibilityElement(soapBody);
    }

    /**
     * Creates a {@link SOAP12Body}, and calls {@link #populateSoapBody(SOAP12Body)}.
     *
     * @param bindingOutput the WSDL4J <code>BindingOutput</code>
     * @throws WSDLException in case of errors
     */
    protected void populateBindingOutput(BindingOutput bindingOutput, Output output) throws WSDLException {
        super.populateBindingOutput(bindingOutput, output);
        SOAP12Body soapBody = (SOAP12Body) createSoapExtension(BindingOutput.class, "body");
        populateSoapBody(soapBody);
        bindingOutput.addExtensibilityElement(soapBody);
    }

    /**
     * Creates a {@link SOAP12Body}, and calls {@link #populateSoapBody(SOAP12Body)}.
     *
     * @param bindingFault the WSDL4J <code>BindingFault</code>
     * @throws WSDLException in case of errors
     */
    protected void populateBindingFault(BindingFault bindingFault, Fault fault) throws WSDLException {
        super.populateBindingFault(bindingFault, fault);
        SOAP12Fault soapFault = (SOAP12Fault) createSoapExtension(BindingFault.class, "fault");
        populateSoapFault(bindingFault, soapFault);
        bindingFault.addExtensibilityElement(soapFault);
    }

    /**
     * Called after the {@link SOAP12Body} has been created. Default implementation sets the use style to
     * <code>"literal"</code>. Subclasses can override this behavior.
     *
     * @param soapBody the WSDL4J <code>SOAP12Body</code>
     * @throws WSDLException in case of errors
     * @see SOAP12Body#setUse(String)
     */
    protected void populateSoapBody(SOAP12Body soapBody) throws WSDLException {
        soapBody.setUse("literal");
    }

    /**
     * Called after the {@link SOAP12Fault} has been created. Default implementation sets the use style to
     * <code>"literal"</code>, and sets the name equal to the binding fault. Subclasses can override this behavior.
     *
     * @param bindingFault the WSDL4J <code>BindingFault</code>
     * @param soapFault    the WSDL4J <code>SOAPFault</code>
     * @throws WSDLException in case of errors
     * @see SOAP12Fault#setUse(String)
     */
    protected void populateSoapFault(BindingFault bindingFault, SOAP12Fault soapFault) throws WSDLException {
        soapFault.setName(bindingFault.getName());
        soapFault.setUse("literal");
    }

    /**
     * Creates a {@link SOAP12Address}, and calls {@link #populateSoapAddress(SOAP12Address)}.
     *
     * @param port the WSDL4J <code>Port</code>
     * @throws WSDLException in case of errors
     */
    protected void populatePort(Port port, Binding binding) throws WSDLException {
        super.populatePort(port, binding);
        SOAP12Address soapAddress = (SOAP12Address) createSoapExtension(Port.class, "address");
        populateSoapAddress(soapAddress);
        port.addExtensibilityElement(soapAddress);
    }

    /**
     * Called after the {@link SOAP12Address} has been created. Default implementation sets the location URI to the
     * value set on this builder. Subclasses can override this behavior.
     *
     * @param soapAddress the WSDL4J <code>SOAPAddress</code>
     * @throws WSDLException in case of errors
     * @see SOAP12Address#setLocationURI(String)
     * @see #setLocationUri(String)
     */
    protected void populateSoapAddress(SOAP12Address soapAddress) throws WSDLException {
        soapAddress.setLocationURI(locationUri);
    }

    /**
     * Creates a SOAP 1.2 extensibility element.
     *
     * @param parentType a class object indicating where in the WSDL definition this extension will exist
     * @param localName  the local name of the extensibility element
     * @return the extensibility element
     * @throws WSDLException in case of errors
     * @see ExtensionRegistry#createExtension(Class,javax.xml.namespace.QName)
     */
    protected ExtensibilityElement createSoapExtension(Class parentType, String localName) throws WSDLException {
        return createExtension(parentType, new QName(WSDL_SOAP_NAMESPACE_URI, localName));
    }

}
