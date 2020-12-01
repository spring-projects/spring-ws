/*
 * Copyright 2005-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.wsdl.wsdl11.provider;

import java.util.Iterator;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the {@link BindingsProvider} and {@link ServicesProvider} interfaces.
 * <p>
 * Creates a {@code binding} that matches any present {@code portType}, and a service containing {@code port}s that
 * match the {@code binding}s. Lets subclasses populate these through template methods. *
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class DefaultConcretePartProvider implements BindingsProvider, ServicesProvider {

	/** Logger available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	private String bindingSuffix;

	private String serviceName;

	/** Returns the service name. */
	public String getServiceName() {
		return serviceName;
	}

	/** Sets the service name. */
	public void setServiceName(String serviceName) {
		Assert.hasText(serviceName, "'serviceName' must not be null");
		this.serviceName = serviceName;
	}

	/** Returns the suffix to append to the port type name to obtain the binding name. */
	public String getBindingSuffix() {
		return bindingSuffix;
	}

	/** Sets the suffix to append to the port type name to obtain the binding name. */
	public void setBindingSuffix(String bindingSuffix) {
		Assert.notNull(bindingSuffix, "'bindingSuffix' must not be null");
		this.bindingSuffix = bindingSuffix;
	}

	/**
	 * Creates a {@link Binding} for each {@link PortType} in the definition, and calls
	 * {@link #populateBinding(Definition,javax.wsdl.Binding)} with it. Creates a {@link BindingOperation} for each
	 * {@link Operation} in the port type, a {@link BindingInput} for each {@link Input} in the operation, etc.
	 * <p>
	 * Calls the various {@code populate} methods with the created WSDL4J objects.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @throws WSDLException in case of errors
	 * @see #populateBinding(Definition,javax.wsdl.Binding)
	 * @see #populateBindingOperation(Definition,javax.wsdl.BindingOperation)
	 * @see #populateBindingInput(Definition,javax.wsdl.BindingInput,javax.wsdl.Input)
	 * @see #populateBindingOutput(Definition,javax.wsdl.BindingOutput,javax.wsdl.Output)
	 * @see #populateBindingFault(Definition,javax.wsdl.BindingFault,javax.wsdl.Fault)
	 */
	@Override
	public void addBindings(Definition definition) throws WSDLException {
		for (Iterator<?> iterator = definition.getPortTypes().values().iterator(); iterator.hasNext();) {
			PortType portType = (PortType) iterator.next();
			Binding binding = definition.createBinding();
			binding.setPortType(portType);
			populateBinding(definition, binding);
			createBindingOperations(definition, binding);
			binding.setUndefined(false);
			if (binding.getQName() != null) {
				definition.addBinding(binding);
			}
		}
		if (definition.getBindings().isEmpty() && logger.isWarnEnabled()) {
			logger.warn("No bindings were created, make sure the WSDL contains port types");
		}
	}

	/**
	 * Called after the {@link Binding} has been created, but before any sub-elements are added. Subclasses can override
	 * this method to define the binding name, or add extensions to it.
	 * <p>
	 * Default implementation sets the binding name to the port type name with the {@link #getBindingSuffix() suffix}
	 * appended to it.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @param binding the WSDL4J {@code Binding}
	 */
	protected void populateBinding(Definition definition, Binding binding) throws WSDLException {
		QName portTypeName = binding.getPortType().getQName();
		if (portTypeName != null) {
			QName bindingName = new QName(portTypeName.getNamespaceURI(), portTypeName.getLocalPart() + getBindingSuffix());
			if (logger.isDebugEnabled()) {
				logger.debug("Creating binding [" + bindingName + "]");
			}
			binding.setQName(bindingName);
		}
	}

	private void createBindingOperations(Definition definition, Binding binding) throws WSDLException {
		PortType portType = binding.getPortType();
		for (Iterator<?> operationIterator = portType.getOperations().iterator(); operationIterator.hasNext();) {
			Operation operation = (Operation) operationIterator.next();
			BindingOperation bindingOperation = definition.createBindingOperation();
			bindingOperation.setOperation(operation);
			populateBindingOperation(definition, bindingOperation);
			if (OperationType.REQUEST_RESPONSE.equals(operation.getStyle())) {
				createBindingInput(definition, operation, bindingOperation);
				createBindingOutput(definition, operation, bindingOperation);
			} else if (OperationType.ONE_WAY.equals(operation.getStyle())) {
				createBindingInput(definition, operation, bindingOperation);
			} else if (OperationType.NOTIFICATION.equals(operation.getStyle())) {
				createBindingOutput(definition, operation, bindingOperation);
			} else if (OperationType.SOLICIT_RESPONSE.equals(operation.getStyle())) {
				createBindingOutput(definition, operation, bindingOperation);
				createBindingInput(definition, operation, bindingOperation);
			}
			for (Iterator<?> faultIterator = operation.getFaults().values().iterator(); faultIterator.hasNext();) {
				Fault fault = (Fault) faultIterator.next();
				BindingFault bindingFault = definition.createBindingFault();
				populateBindingFault(definition, bindingFault, fault);
				if (StringUtils.hasText(bindingFault.getName())) {
					bindingOperation.addBindingFault(bindingFault);
				}
			}
			binding.addBindingOperation(bindingOperation);
		}
	}

	/**
	 * Called after the {@link BindingOperation} has been created, but before any sub-elements are added. Subclasses can
	 * override this method to define the binding name, or add extensions to it.
	 * <p>
	 * Default implementation sets the name of the binding operation to the name of the operation.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @param bindingOperation the WSDL4J {@code BindingOperation}
	 * @throws WSDLException in case of errors
	 */
	protected void populateBindingOperation(Definition definition, BindingOperation bindingOperation)
			throws WSDLException {
		bindingOperation.setName(bindingOperation.getOperation().getName());
	}

	private void createBindingInput(Definition definition, Operation operation, BindingOperation bindingOperation)
			throws WSDLException {
		BindingInput bindingInput = definition.createBindingInput();
		populateBindingInput(definition, bindingInput, operation.getInput());
		bindingOperation.setBindingInput(bindingInput);
	}

	private void createBindingOutput(Definition definition, Operation operation, BindingOperation bindingOperation)
			throws WSDLException {
		BindingOutput bindingOutput = definition.createBindingOutput();
		populateBindingOutput(definition, bindingOutput, operation.getOutput());
		bindingOperation.setBindingOutput(bindingOutput);
	}

	/**
	 * Called after the {@link BindingInput} has been created. Subclasses can override this method to define the name, or
	 * add extensions to it.
	 * <p>
	 * Default implementation set the name of the binding input to the name of the input.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @param bindingInput the WSDL4J {@code BindingInput}
	 * @param input the corresponding WSDL4J {@code Input} @throws WSDLException in case of errors
	 */
	protected void populateBindingInput(Definition definition, BindingInput bindingInput, Input input)
			throws WSDLException {
		bindingInput.setName(input.getName());
	}

	/**
	 * Called after the {@link BindingOutput} has been created. Subclasses can override this method to define the name, or
	 * add extensions to it.
	 * <p>
	 * Default implementation sets the name of the binding output to the name of the output.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @param bindingOutput the WSDL4J {@code BindingOutput}
	 * @param output the corresponding WSDL4J {@code Output} @throws WSDLException in case of errors
	 */
	protected void populateBindingOutput(Definition definition, BindingOutput bindingOutput, Output output)
			throws WSDLException {
		bindingOutput.setName(output.getName());
	}

	/**
	 * Called after the {@link BindingFault} has been created. Subclasses can implement this method to define the name, or
	 * add extensions to it.
	 * <p>
	 * Default implementation set the name of the binding fault to the name of the fault.
	 *
	 * @param bindingFault the WSDL4J {@code BindingFault}
	 * @param fault the corresponding WSDL4J {@code Fault} @throws WSDLException in case of errors
	 */
	protected void populateBindingFault(Definition definition, BindingFault bindingFault, Fault fault)
			throws WSDLException {
		bindingFault.setName(fault.getName());
	}

	/**
	 * Creates a single {@link Service} if not present, and calls {@link #populateService(Definition, Service)} with it.
	 * Creates a corresponding {@link Port} for each {@link Binding}, which is passed to
	 * {@link #populatePort(javax.wsdl.Definition,javax.wsdl.Port)}.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @throws WSDLException in case of errors
	 */
	@Override
	public void addServices(Definition definition) throws WSDLException {
		Assert.notNull(getServiceName(), "'serviceName' is required");
		Service service;
		if (definition.getServices().isEmpty()) {
			service = definition.createService();
		} else {
			service = (Service) definition.getServices().values().iterator().next();
		}
		populateService(definition, service);
		createPorts(definition, service);
		if (service.getQName() != null) {
			definition.addService(service);
		}
	}

	/**
	 * Called after the {@link Service} has been created, but before any sub-elements are added. Subclasses can implement
	 * this method to define the service name, or add extensions to it.
	 * <p>
	 * Default implementation sets the name to the {@link #setServiceName(String) serviceName} property.
	 *
	 * @param service the WSDL4J {@code Service}
	 * @throws WSDLException in case of errors
	 */
	protected void populateService(Definition definition, Service service) throws WSDLException {
		if (StringUtils.hasText(definition.getTargetNamespace()) && StringUtils.hasText(getServiceName())) {
			QName serviceName = new QName(definition.getTargetNamespace(), getServiceName());
			if (logger.isDebugEnabled()) {
				logger.debug("Creating service [" + serviceName + "]");
			}
			service.setQName(serviceName);
		}
	}

	private void createPorts(Definition definition, Service service) throws WSDLException {
		for (Iterator<?> iterator = definition.getBindings().values().iterator(); iterator.hasNext();) {
			Binding binding = (Binding) iterator.next();
			Port port = null;
			for (Iterator<?> iterator1 = service.getPorts().values().iterator(); iterator1.hasNext();) {
				Port existingPort = (Port) iterator1.next();
				if (binding.equals(existingPort.getBinding())) {
					port = existingPort;
				}
			}
			if (port == null) {
				port = definition.createPort();
				port.setBinding(binding);
			}
			populatePort(definition, port);
			if (StringUtils.hasText(port.getName())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Adding port [" + port.getName() + "] to service [" + service.getQName() + "]");
				}
				service.addPort(port);
			}
		}
		if (service.getPorts().isEmpty() && logger.isWarnEnabled()) {
			logger.warn("No ports were created, make sure the WSDL contains bindings");
		}
	}

	/**
	 * Called after the {@link Port} has been created, but before any sub-elements are added. Subclasses can implement
	 * this method to define the port name, or add extensions to it.
	 * <p>
	 * Default implementation sets the port name to the binding name.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @param port the WSDL4J {@code Port}
	 * @throws WSDLException in case of errors
	 */
	protected void populatePort(Definition definition, Port port) throws WSDLException {
		String portName = port.getBinding().getQName().getLocalPart();
		port.setName(portName);
	}

}
