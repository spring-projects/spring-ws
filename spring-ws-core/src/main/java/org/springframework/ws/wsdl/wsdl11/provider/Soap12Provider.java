/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.wsdl.wsdl11.provider;

import java.util.Iterator;
import java.util.Properties;
import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Fault;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.xml.namespace.QName;

import org.springframework.util.Assert;

/**
 * Implementation of the {@link BindingsProvider} and {@link ServicesProvider} interfaces that are SOAP 1.2 specific.
 *
 * <p>By setting the {@link #setSoapActions(java.util.Properties) soapActions} property, the SOAP Actions defined in the
 * resulting WSDL can be set. Additionaly, the transport uri can be changed from the default HTTP transport by using the
 * {@link #setTransportUri(String) transportUri} property.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class Soap12Provider extends DefaultConcretePartProvider {

	/** The default transport URI, which indicates an HTTP transport. */
	public static final String DEFAULT_TRANSPORT_URI = "http://schemas.xmlsoap.org/soap/http";

	/** The prefix of the WSDL SOAP 1.2 namespace. */
	public static final String SOAP_12_NAMESPACE_PREFIX = "soap12";

	/** The WSDL SOAP 1.1 namespace. */
	public static final String SOAP_12_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/soap12/";

	private String transportUri = DEFAULT_TRANSPORT_URI;

	private Properties soapActions = new Properties();

	private String locationUri;

	/**
	 * Constructs a new version of the {@link Soap12Provider}.
	 *
	 * <p>Sets the {@link #setBindingSuffix(String) binding suffix} to {@code Soap12}.
	 */
	public Soap12Provider() {
		setBindingSuffix("Soap12");
	}

	/**
	 * Returns the SOAP Actions for this binding. Keys are {@link javax.wsdl.BindingOperation#getName() binding
	 * operation names}; values are {@link javax.wsdl.extensions.soap.SOAPOperation#getSoapActionURI() SOAP Action
	 * URIs}.
	 *
	 * @return the soap actions
	 */
	public Properties getSoapActions() {
		return soapActions;
	}

	/**
	 * Sets the SOAP Actions for this binding. Keys are {@link javax.wsdl.BindingOperation#getName() binding operation
	 * names}; values are {@link javax.wsdl.extensions.soap.SOAPOperation#getSoapActionURI() SOAP Action URIs}.
	 *
	 * @param soapActions the soap
	 */
	public void setSoapActions(Properties soapActions) {
		Assert.notNull(soapActions, "'soapActions' must not be null");
		this.soapActions = soapActions;
	}

	/**
	 * Returns the value used for the binding transport attribute value. Defaults to {@link #DEFAULT_TRANSPORT_URI}.
	 *
	 * @return the binding transport value
	 */
	public String getTransportUri() {
		return transportUri;
	}

	/**
	 * Sets the value used for the binding transport attribute value. Defaults to {@link #DEFAULT_TRANSPORT_URI}.
	 *
	 * @param transportUri the binding transport value
	 */
	public void setTransportUri(String transportUri) {
		Assert.notNull(transportUri, "'transportUri' must not be null");
		this.transportUri = transportUri;
	}

	/** Returns the value used for the SOAP Address location attribute value. */
	public String getLocationUri() {
		return locationUri;
	}

	/** Sets the value used for the SOAP Address location attribute value. */
	public void setLocationUri(String locationUri) {
		this.locationUri = locationUri;
	}

	/**
	 * Called after the {@link javax.wsdl.Binding} has been created, but before any sub-elements are added. Subclasses
	 * can override this method to define the binding name, or add extensions to it.
	 *
	 * <p>Default implementation calls {@link DefaultConcretePartProvider#populateBinding(javax.wsdl.Definition,
	 * javax.wsdl.Binding)}, adds the SOAP 1.1 namespace, creates a {@link javax.wsdl.extensions.soap.SOAPBinding}, and
	 * calls {@link #populateSoapBinding(javax.wsdl.extensions.soap12.SOAP12Binding, javax.wsdl.Binding)} sets the
	 * binding name to the port type name with the {@link #getBindingSuffix() suffix} appended to it.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @param binding	 the WSDL4J {@code Binding}
	 */
	@Override
	protected void populateBinding(Definition definition, Binding binding) throws WSDLException {
		definition.addNamespace(SOAP_12_NAMESPACE_PREFIX, SOAP_12_NAMESPACE_URI);
		super.populateBinding(definition, binding);
		SOAP12Binding soapBinding = (SOAP12Binding) createSoapExtension(definition, Binding.class, "binding");
		populateSoapBinding(soapBinding, binding);
		binding.addExtensibilityElement(soapBinding);
	}

	/**
	 * Called after the {@link javax.wsdl.extensions.soap.SOAPBinding} has been created.
	 *
	 * <p>Default implementation sets the binding style to {@code "document"}, and set the transport URI to the {@link
	 * #setTransportUri(String) transportUri} property value. Subclasses can override this behavior.
	 *
	 * @param soapBinding the WSDL4J {@code SOAPBinding}
	 * @throws javax.wsdl.WSDLException in case of errors
	 * @see javax.wsdl.extensions.soap.SOAPBinding#setStyle(String)
	 * @see javax.wsdl.extensions.soap.SOAPBinding#setTransportURI(String)
	 * @see #setTransportUri(String)
	 * @see #DEFAULT_TRANSPORT_URI
	 */
	protected void populateSoapBinding(SOAP12Binding soapBinding, Binding binding) throws WSDLException {
		soapBinding.setStyle("document");
		soapBinding.setTransportURI(getTransportUri());
	}

	/**
	 * Called after the {@link javax.wsdl.BindingFault} has been created. Subclasses can override this method to define
	 * the name, or add extensions to it.
	 *
	 * <p>Default implementation calls {@link DefaultConcretePartProvider#populateBindingFault(javax.wsdl.Definition,
	 * javax.wsdl.BindingFault, javax.wsdl.Fault)}, creates a {@link javax.wsdl.extensions.soap.SOAPFault}, and calls
	 * {@link #populateSoapFault(javax.wsdl.BindingFault, javax.wsdl.extensions.soap12.SOAP12Fault)}.
	 *
	 * @param definition   the WSDL4J {@code Definition}
	 * @param bindingFault the WSDL4J {@code BindingFault}
	 * @param fault		   the corresponding WSDL4J {@code Fault} @throws WSDLException in case of errors
	 */
	@Override
	protected void populateBindingFault(Definition definition, BindingFault bindingFault, Fault fault)
			throws WSDLException {
		super.populateBindingFault(definition, bindingFault, fault);
		SOAP12Fault soapFault = (SOAP12Fault) createSoapExtension(definition, BindingFault.class, "fault");
		populateSoapFault(bindingFault, soapFault);
		bindingFault.addExtensibilityElement(soapFault);
	}

	/**
	 * Called after the {@link javax.wsdl.extensions.soap.SOAPFault} has been created.
	 *
	 * <p>Default implementation sets the use style to {@code "literal"}, and sets the name equal to the binding
	 * fault. Subclasses can override this behavior.
	 *
	 * @param bindingFault the WSDL4J {@code BindingFault}
	 * @param soapFault	   the WSDL4J {@code SOAPFault}
	 * @throws javax.wsdl.WSDLException in case of errors
	 * @see javax.wsdl.extensions.soap.SOAPFault#setUse(String)
	 */
	protected void populateSoapFault(BindingFault bindingFault, SOAP12Fault soapFault) throws WSDLException {
		soapFault.setName(bindingFault.getName());
		soapFault.setUse("literal");
	}

	/**
	 * Called after the {@link javax.wsdl.BindingInput} has been created. Subclasses can implement this method to define
	 * the name, or add extensions to it.
	 *
	 * <p>Default implementation calls {@link DefaultConcretePartProvider#populateBindingInput(javax.wsdl.Definition,
	 * javax.wsdl.BindingInput, javax.wsdl.Input)}, creates a {@link javax.wsdl.extensions.soap.SOAPBody}, and calls
	 * {@link #populateSoapBody(javax.wsdl.extensions.soap12.SOAP12Body)}. 2
	 *
	 * @param definition   the WSDL4J {@code Definition}
	 * @param bindingInput the WSDL4J {@code BindingInput}
	 * @param input		   the corresponding WSDL4J {@code Input} @throws WSDLException in case of errors
	 */
	@Override
	protected void populateBindingInput(Definition definition, BindingInput bindingInput, Input input)
			throws WSDLException {
		super.populateBindingInput(definition, bindingInput, input);
		SOAP12Body soapBody = (SOAP12Body) createSoapExtension(definition, BindingInput.class, "body");
		populateSoapBody(soapBody);
		bindingInput.addExtensibilityElement(soapBody);
	}

	/**
	 * Called after the {@link javax.wsdl.extensions.soap.SOAPBody} has been created.
	 *
	 * <p>Default implementation sets the use style to {@code "literal"}. Subclasses can override this behavior.
	 *
	 * @param soapBody the WSDL4J {@code SOAPBody}
	 * @throws javax.wsdl.WSDLException in case of errors
	 * @see javax.wsdl.extensions.soap.SOAPBody#setUse(String)
	 */
	protected void populateSoapBody(SOAP12Body soapBody) throws WSDLException {
		soapBody.setUse("literal");
	}

	/**
	 * Called after the {@link javax.wsdl.BindingOperation} has been created, but before any sub-elements are added.
	 * Subclasses can implement this method to define the binding name, or add extensions to it.
	 *
	 * <p>Default implementation calls {@link DefaultConcretePartProvider#populateBindingOperation(javax.wsdl.Definition,
	 * javax.wsdl.BindingOperation)}, creates a {@link javax.wsdl.extensions.soap.SOAPOperation}, and calls {@link
	 * #populateSoapOperation} sets the name of the binding operation to the name of the operation.
	 *
	 * @param definition	   the WSDL4J {@code Definition}
	 * @param bindingOperation the WSDL4J {@code BindingOperation}
	 * @throws javax.wsdl.WSDLException in case of errors
	 */
	@Override
	protected void populateBindingOperation(Definition definition, BindingOperation bindingOperation)
			throws WSDLException {
		super.populateBindingOperation(definition, bindingOperation);
		SOAP12Operation soapOperation =
				(SOAP12Operation) createSoapExtension(definition, BindingOperation.class, "operation");
		populateSoapOperation(soapOperation, bindingOperation);
		bindingOperation.addExtensibilityElement(soapOperation);
	}

	/**
	 * Called after the {@link javax.wsdl.extensions.soap.SOAPOperation} has been created.
	 *
	 * <p>Default implementation sets {@code SOAPAction} to the corresponding {@link
	 * #setSoapActions(java.util.Properties) soapActions} property, and defaults to "".
	 *
	 * @param soapOperation	   the WSDL4J {@code SOAPOperation}
	 * @param bindingOperation the WSDL4J {@code BindingOperation}
	 * @throws javax.wsdl.WSDLException in case of errors
	 * @see javax.wsdl.extensions.soap.SOAPOperation#setSoapActionURI(String)
	 * @see #setSoapActions(java.util.Properties)
	 */
	protected void populateSoapOperation(SOAP12Operation soapOperation, BindingOperation bindingOperation)
			throws WSDLException {
		String bindingOperationName = bindingOperation.getName();
		String soapAction = getSoapActions().getProperty(bindingOperationName, "");
		soapOperation.setSoapActionURI(soapAction);
	}

	/**
	 * Called after the {@link javax.wsdl.BindingInput} has been created. Subclasses can implement this method to define
	 * the name, or add extensions to it.
	 *
	 * <p>Default implementation calls {@link DefaultConcretePartProvider#populateBindingOutput(javax.wsdl.Definition,
	 * javax.wsdl.BindingOutput, javax.wsdl.Output)}, creates a {@link javax.wsdl.extensions.soap.SOAPBody}, and calls
	 * {@link #populateSoapBody(javax.wsdl.extensions.soap12.SOAP12Body)}.
	 *
	 * @param definition	the WSDL4J {@code Definition}
	 * @param bindingOutput the WSDL4J {@code BindingOutput}
	 * @param output		the corresponding WSDL4J {@code Output} @throws WSDLException in case of errors
	 */
	@Override
	protected void populateBindingOutput(Definition definition, BindingOutput bindingOutput, Output output)
			throws WSDLException {
		super.populateBindingOutput(definition, bindingOutput, output);
		SOAP12Body soapBody = (SOAP12Body) createSoapExtension(definition, BindingOutput.class, "body");
		populateSoapBody(soapBody);
		bindingOutput.addExtensibilityElement(soapBody);
	}

	/**
	 * Called after the {@link javax.wsdl.Port} has been created, but before any sub-elements are added. Subclasses can
	 * implement this method to define the port name, or add extensions to it.
	 *
	 * <p>Default implementation calls {@link DefaultConcretePartProvider#populatePort(javax.wsdl.Definition,javax.wsdl.Port)},
	 * creates a {@link javax.wsdl.extensions.soap.SOAPAddress}, and calls {@link #populateSoapAddress(SOAP12Address)}.
	 *
	 * @param port the WSDL4J {@code Port}
	 * @throws WSDLException in case of errors
	 */
	@Override
	protected void populatePort(Definition definition, Port port) throws WSDLException {
		for (Iterator<?> iterator = port.getBinding().getExtensibilityElements().iterator(); iterator.hasNext();) {
			if (iterator.next() instanceof SOAP12Binding) {
				// this is a SOAP 1.2 binding, create a SOAP Address for it 
				super.populatePort(definition, port);
				SOAP12Address soapAddress = (SOAP12Address) createSoapExtension(definition, Port.class, "address");
				populateSoapAddress(soapAddress);
				port.addExtensibilityElement(soapAddress);
				return;
			}
		}
	}

	/**
	 * Called after the {@link SOAP12Address} has been created. Default implementation sets the location URI to the
	 * value set on this builder. Subclasses can override this behavior.
	 *
	 * @param soapAddress the WSDL4J {@code SOAPAddress}
	 * @throws WSDLException in case of errors
	 * @see SOAP12Address#setLocationURI(String)
	 * @see #setLocationUri(String)
	 */
	protected void populateSoapAddress(SOAP12Address soapAddress) throws WSDLException {
		soapAddress.setLocationURI(getLocationUri());
	}

	/**
	 * Creates a SOAP extensibility element.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @param parentType a class object indicating where in the WSDL definition this extension will exist
	 * @param localName	 the local name of the extensibility element
	 * @return the extensibility element
	 * @throws WSDLException in case of errors
	 * @see javax.wsdl.extensions.ExtensionRegistry#createExtension(Class, QName)
	 */
	private ExtensibilityElement createSoapExtension(Definition definition, Class<?> parentType, String localName)
			throws WSDLException {
		return definition.getExtensionRegistry()
				.createExtension(parentType, new QName(SOAP_12_NAMESPACE_URI, localName));
	}

}