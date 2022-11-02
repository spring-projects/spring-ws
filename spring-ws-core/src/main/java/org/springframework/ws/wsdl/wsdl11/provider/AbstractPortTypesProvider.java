/*
 * Copyright 2005-2022 the original author or authors.
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
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Abstract base class for {@link PortTypesProvider} implementations.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class AbstractPortTypesProvider implements PortTypesProvider {

	/** Logger available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	private String portTypeName;

	/** Returns the port type name used for this definition. */
	public String getPortTypeName() {
		return portTypeName;
	}

	/** Sets the port type name used for this definition. Required. */
	public void setPortTypeName(String portTypeName) {
		this.portTypeName = portTypeName;
	}

	/**
	 * Creates a single {@link PortType}, and calls {@link #populatePortType(Definition, PortType)} with it.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @throws WSDLException in case of errors
	 */
	@Override
	public void addPortTypes(Definition definition) throws WSDLException {
		Assert.notNull(getPortTypeName(), "'portTypeName' is required");
		PortType portType = definition.createPortType();
		populatePortType(definition, portType);
		createOperations(definition, portType);
		portType.setUndefined(false);
		definition.addPortType(portType);
	}

	/**
	 * Called after the {@link PortType} has been created.
	 * <p>
	 * Default implementation sets the name of the port type to the defined value.
	 *
	 * @param portType the WSDL4J {@code PortType}
	 * @throws WSDLException in case of errors
	 * @see #setPortTypeName(String)
	 */
	protected void populatePortType(Definition definition, PortType portType) throws WSDLException {
		QName portTypeName = new QName(definition.getTargetNamespace(), getPortTypeName());
		if (logger.isDebugEnabled()) {
			logger.debug("Creating port type [" + portTypeName + "]");
		}
		portType.setQName(portTypeName);
	}

	private void createOperations(Definition definition, PortType portType) throws WSDLException {
		MultiValueMap<String, Message> operations = new LinkedMultiValueMap<String, Message>();
		for (Iterator<?> iterator = definition.getMessages().values().iterator(); iterator.hasNext();) {
			Message message = (Message) iterator.next();
			String operationName = getOperationName(message);
			if (StringUtils.hasText(operationName)) {
				operations.add(operationName, message);
			}
		}
		if (operations.isEmpty() && logger.isWarnEnabled()) {
			logger.warn("No operations were created, make sure the WSDL contains messages");
		}
		for (String operationName : operations.keySet()) {
			Operation operation = definition.createOperation();
			operation.setName(operationName);
			List<Message> messages = operations.get(operationName);
			for (Message message : messages) {
				if (isInputMessage(message)) {
					Input input = definition.createInput();
					input.setMessage(message);
					populateInput(definition, input);
					operation.setInput(input);
				} else if (isOutputMessage(message)) {
					Output output = definition.createOutput();
					output.setMessage(message);
					populateOutput(definition, output);
					operation.setOutput(output);
				} else if (isFaultMessage(message)) {
					Fault fault = definition.createFault();
					fault.setMessage(message);
					populateFault(definition, fault);
					operation.addFault(fault);
				}
			}
			operation.setStyle(getOperationType(operation));
			operation.setUndefined(false);
			if (logger.isDebugEnabled()) {
				logger.debug("Adding operation [" + operation.getName() + "] to port type [" + portType.getQName() + "]");
			}
			portType.addOperation(operation);
		}
	}

	/**
	 * Template method that returns the name of the operation coupled to the given {@link Message}. Subclasses can return
	 * {@code null} to indicate that a message should not be coupled to an operation.
	 *
	 * @param message the WSDL4J {@code Message}
	 * @return the operation name; or {@code null}
	 */
	protected abstract String getOperationName(Message message);

	/**
	 * Indicates whether the given name name should be included as {@link Input} message in the definition.
	 *
	 * @param message the message
	 * @return {@code true} if to be included as input; {@code false} otherwise
	 */
	protected abstract boolean isInputMessage(Message message);

	/**
	 * Called after the {@link javax.wsdl.Input} has been created, but it's added to the operation. Subclasses can
	 * override this method to define the input name.
	 * <p>
	 * Default implementation sets the input name to the message name.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @param input the WSDL4J {@code Input}
	 */
	protected void populateInput(Definition definition, Input input) {
		input.setName(input.getMessage().getQName().getLocalPart());
	}

	/**
	 * Indicates whether the given name name should be included as {@link Output} message in the definition.
	 *
	 * @param message the message
	 * @return {@code true} if to be included as output; {@code false} otherwise
	 */
	protected abstract boolean isOutputMessage(Message message);

	/**
	 * Called after the {@link javax.wsdl.Output} has been created, but it's added to the operation. Subclasses can
	 * override this method to define the output name.
	 * <p>
	 * Default implementation sets the output name to the message name.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @param output the WSDL4J {@code Output}
	 */
	protected void populateOutput(Definition definition, Output output) {
		output.setName(output.getMessage().getQName().getLocalPart());
	}

	/**
	 * Indicates whether the given name name should be included as {@link Fault} message in the definition.
	 *
	 * @param message the message
	 * @return {@code true} if to be included as fault; {@code false} otherwise
	 */
	protected abstract boolean isFaultMessage(Message message);

	/**
	 * Called after the {@link javax.wsdl.Fault} has been created, but it's added to the operation. Subclasses can
	 * override this method to define the fault name.
	 * <p>
	 * Default implementation sets the fault name to the message name.
	 *
	 * @param definition the WSDL4J {@code Definition}
	 * @param fault the WSDL4J {@code Fault}
	 */
	protected void populateFault(Definition definition, Fault fault) {
		fault.setName(fault.getMessage().getQName().getLocalPart());
	}

	/**
	 * Returns the {@link OperationType} for the given operation.
	 * <p>
	 * Default implementation returns {@link OperationType#REQUEST_RESPONSE} if both input and output are set;
	 * {@link OperationType#ONE_WAY} if only input is set, or {@link OperationType#NOTIFICATION} if only output is set.
	 *
	 * @param operation the WSDL4J {@code Operation}
	 * @return the operation type for the operation
	 */
	protected OperationType getOperationType(Operation operation) {
		if (operation.getInput() != null && operation.getOutput() != null) {
			return OperationType.REQUEST_RESPONSE;
		} else if (operation.getInput() != null && operation.getOutput() == null) {
			return OperationType.ONE_WAY;
		} else if (operation.getInput() == null && operation.getOutput() != null) {
			return OperationType.NOTIFICATION;
		} else {
			return null;
		}
	}

}
