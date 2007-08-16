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
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.springframework.ws.wsdl.wsdl11.Wsdl4jDefinitionException;

/**
 * Abstract base class for <code>Wsdl11DefinitionBuilder</code> implementations that use WSDL4J and contain a concrete
 * part. Creates a <code>binding</code> that matches any present <code>portType</code>, and a service containing
 * <code>port</code>s that match the <code>binding</code>s. Lets subclasses populate these through template methods.
 *
 * @author Arjen Poutsma
 * @since 1.0
 */
public abstract class AbstractBindingWsdl4jDefinitionBuilder extends AbstractWsdl4jDefinitionBuilder {

    /** The suffix used to create a binding name from a port type name. */
    private static final String BINDING_SUFFIX = "Binding";

    /** The suffix used to create a binding name from a port type name. */
    private static final String PORT_SUFFIX = "Port";

    /**
     * Creates a <code>Binding</code> for each <code>PortType</code> in the definition, and calls
     * <code>populateBinding</code> with it. Creates a <code>BindingOperation</code> for each <code>Operation</code> in
     * the port type, a <code>BindingInput</code> for each <code>Input</code> in the operation, etc.
     * <p/>
     * Calls the various <code>populate</code> methods with the created WSDL4J objects.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     * @see javax.wsdl.Binding
     * @see javax.wsdl.extensions.soap.SOAPBinding
     * @see #populateBinding(javax.wsdl.Binding,javax.wsdl.PortType)
     * @see javax.wsdl.BindingOperation
     * @see #populateBindingOperation(javax.wsdl.BindingOperation,javax.wsdl.Operation)
     * @see javax.wsdl.BindingInput
     * @see #populateBindingInput(javax.wsdl.BindingInput,javax.wsdl.Input)
     * @see javax.wsdl.BindingOutput
     * @see #populateBindingOutput(javax.wsdl.BindingOutput,javax.wsdl.Output)
     * @see javax.wsdl.BindingFault
     * @see #populateBindingFault(javax.wsdl.BindingFault,javax.wsdl.Fault)
     */
    public void buildBindings(Definition definition) throws WSDLException {
        try {
            for (Iterator iterator = definition.getPortTypes().values().iterator(); iterator.hasNext();) {
                PortType portType = (PortType) iterator.next();
                Binding binding = definition.createBinding();
                binding.setPortType(portType);
                populateBinding(binding, portType);
                createBindingOperations(definition, binding, portType);
                binding.setUndefined(false);
                definition.addBinding(binding);
            }
        }
        catch (WSDLException ex) {
            throw new Wsdl4jDefinitionException(ex);
        }
    }

    /**
     * Called after the <code>Binding</code> has been created, but before any sub-elements are added. Subclasses can
     * implement this method to define the binding name, or add extensions to it.
     * <p/>
     * Default implementation sets the binding name to the port type name with the suffix <code>Binding</code> appended
     * to it.
     *
     * @param binding  the WSDL4J <code>Binding</code>
     * @param portType the corresponding <code>PortType</code>
     * @throws WSDLException in case of errors
     */
    protected void populateBinding(Binding binding, PortType portType) throws WSDLException {
        QName portTypeName = portType.getQName();
        if (portTypeName != null) {
            binding.setQName(new QName(portTypeName.getNamespaceURI(), portTypeName.getLocalPart() + BINDING_SUFFIX));
        }
    }

    private void createBindingOperations(Definition definition, Binding binding, PortType portType)
            throws WSDLException {
        for (Iterator operationIterator = portType.getOperations().iterator(); operationIterator.hasNext();) {
            Operation operation = (Operation) operationIterator.next();
            BindingOperation bindingOperation = definition.createBindingOperation();
            bindingOperation.setOperation(operation);
            populateBindingOperation(bindingOperation, operation);
            if (operation.getInput() != null) {
                BindingInput bindingInput = definition.createBindingInput();
                populateBindingInput(bindingInput, operation.getInput());
                bindingOperation.setBindingInput(bindingInput);
            }
            if (operation.getOutput() != null) {
                BindingOutput bindingOutput = definition.createBindingOutput();
                populateBindingOutput(bindingOutput, operation.getOutput());
                bindingOperation.setBindingOutput(bindingOutput);
            }
            for (Iterator faultIterator = operation.getFaults().values().iterator(); faultIterator.hasNext();) {
                Fault fault = (Fault) faultIterator.next();
                BindingFault bindingFault = definition.createBindingFault();
                populateBindingFault(bindingFault, fault);
                bindingOperation.addBindingFault(bindingFault);
            }
            binding.addBindingOperation(bindingOperation);
        }
    }

    /**
     * Called after the <code>BindingOperation</code> has been created, but before any sub-elements are added.
     * Subclasses can implement this method to define the binding name, or add extensions to it.
     * <p/>
     * Default implementation sets the name of the binding operation to the name of the operation.
     *
     * @param bindingOperation the WSDL4J <code>BindingOperation</code>
     * @param operation        the corresponding WSDL4J <code>Operation</code>
     * @throws WSDLException in case of errors
     */
    protected void populateBindingOperation(BindingOperation bindingOperation, Operation operation)
            throws WSDLException {
        bindingOperation.setName(operation.getName());
    }

    /**
     * Called after the <code>BindingInput</code> has been created. Subclasses can implement this method to define the
     * name, or add extensions to it.
     * <p/>
     * Default implementation set the name of the binding input to the name of the input.
     *
     * @param bindingInput the WSDL4J <code>BindingInput</code>
     * @param input        the corresponding WSDL4J <code>Input</code>
     * @throws WSDLException in case of errors
     */
    protected void populateBindingInput(BindingInput bindingInput, Input input) throws WSDLException {
        bindingInput.setName(input.getName());
    }

    /**
     * Called after the <code>BindingOutput</code> has been created. Subclasses can implement this method to define the
     * name, or add extensions to it.
     * <p/>
     * Default implementation set the name of the binding output to the name of the output.
     *
     * @param bindingOutput the WSDL4J <code>BindingOutput</code>
     * @param output        the corresponding WSDL4J <code>Output</code>
     * @throws WSDLException in case of errors
     */
    protected void populateBindingOutput(BindingOutput bindingOutput, Output output) throws WSDLException {
        bindingOutput.setName(output.getName());
    }

    /**
     * Called after the <code>BindingFault</code> has been created. Subclasses can implement this method to define the
     * name, or add extensions to it.
     * <p/>
     * Default implementation set the name of the binding fault to the name of the fault.
     *
     * @param bindingFault the WSDL4J <code>BindingFault</code>
     * @param fault        the corresponding WSDL4J <code>Fault</code>
     * @throws WSDLException in case of errors
     */
    protected void populateBindingFault(BindingFault bindingFault, Fault fault) throws WSDLException {
        bindingFault.setName(fault.getName());
    }

    /**
     * Creates a single <code>Service</code>, and calls <code>populateService()</code> with it. Creates a corresponding
     * <code>Port</code> for each <code>Binding</code>, which is passed to <code>populatePort()</code>.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     * @see javax.wsdl.Service
     * @see javax.wsdl.Port
     * @see #populatePort(javax.wsdl.Port,javax.wsdl.Binding)
     */
    public void buildServices(Definition definition) throws WSDLException {
        Service service = definition.createService();
        populateService(service);
        createPorts(definition, service);
        definition.addService(service);
    }

    /**
     * Called after the <code>Binding</code> has been created, but before any sub-elements are added. Subclasses can
     * implement this method to define the binding name, or add extensions to it.
     * <p/>
     * Default implementation is empty.
     *
     * @param service the WSDL4J <code>Service</code>
     * @throws WSDLException in case of errors
     */
    protected void populateService(Service service) throws WSDLException {
    }

    private void createPorts(Definition definition, Service service) throws WSDLException {
        for (Iterator iterator = definition.getBindings().values().iterator(); iterator.hasNext();) {
            Binding binding = (Binding) iterator.next();
            Port port = definition.createPort();
            port.setBinding(binding);
            populatePort(port, binding);
            service.addPort(port);
        }
    }

    /**
     * Called after the <code>Port</code> has been created, but before any sub-elements are added. Subclasses can
     * implement this method to define the port name, or add extensions to it.
     * <p/>
     * <p/>
     * Default implementation sets the port name to the port type name with the suffix <code>Port</code> appended to
     * it.
     *
     * @param port    the WSDL4J <code>Port</code>
     * @param binding the corresponding WSDL4J <code>Binding</code>
     * @throws WSDLException in case of errors
     */
    protected void populatePort(Port port, Binding binding) throws WSDLException {
        if (binding.getPortType() != null && binding.getPortType().getQName() != null) {
            port.setName(binding.getPortType().getQName().getLocalPart() + PORT_SUFFIX);
        }
    }

}
