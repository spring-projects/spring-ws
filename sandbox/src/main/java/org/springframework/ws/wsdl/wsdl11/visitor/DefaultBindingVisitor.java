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

package org.springframework.ws.wsdl.wsdl11.visitor;

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
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

/**
 * Abstract base class for <code>Wsdl11DefinitionBuilder</code> implementations that use WSDL4J and contain a concrete
 * part. Creates a <code>binding</code> that matches any present <code>portType</code>. Lets subclasses populate these
 * through template methods.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class DefaultBindingVisitor implements PortTypeVisitor, DefinitionVisitor {

    /** The suffix used to create a binding name from a port type name. */
    public static final String DEFAULT_BINDING_SUFFIX = "Binding";

    private Binding binding;

    private BindingOperation bindingOperation;

    private Definition definition;

    private String bindingSuffix = DEFAULT_BINDING_SUFFIX;

    public String getBindingSuffix() {
        return bindingSuffix;
    }

    public void setBindingSuffix(String bindingSuffix) {
        this.bindingSuffix = bindingSuffix;
    }

    public void startDefinition(Definition definition) throws WSDLException {
        this.definition = definition;
    }

    public void startPortType(PortType portType) throws WSDLException {
        binding = definition.createBinding();
        binding.setPortType(portType);
        populateBinding(binding, portType);
        binding.setUndefined(false);
    }

    /**
     * Called after the {@link Binding} has been created, but before any sub-elements are added. Subclasses can
     * implement this method to define the binding name, or add extensions to it.
     * <p/>
     * Default implementation sets the binding name to the port type name with the suffix {@link Binding} appended to
     * it.
     *
     * @param binding  the WSDL4J <code>Binding</code>
     * @param portType the corresponding <code>PortType</code>
     * @throws WSDLException in case of errors
     */
    protected void populateBinding(Binding binding, PortType portType) throws WSDLException {
        QName portTypeName = portType.getQName();
        if (portTypeName != null) {
            binding.setQName(new QName(portTypeName.getNamespaceURI(), portTypeName.getLocalPart() +
                    getBindingSuffix()));
        }
    }

    public void endPortType(PortType portType) throws WSDLException {
        definition.addBinding(binding);
    }

    public void startOperation(Operation operation) throws WSDLException {
        bindingOperation = definition.createBindingOperation();
        bindingOperation.setOperation(operation);
        populateBindingOperation(bindingOperation, operation);
    }

    /**
     * Called after the {@link BindingOperation} has been created, but before any sub-elements are added. Subclasses can
     * implement this method to define the binding name, or add extensions to it.
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

    public void input(Input input) throws WSDLException {
        BindingInput bindingInput = definition.createBindingInput();
        populateBindingInput(bindingInput, bindingOperation.getOperation().getInput());
        bindingOperation.setBindingInput(bindingInput);
    }

    /**
     * Called after the {@link BindingInput} has been created. Subclasses can implement this method to define the name,
     * or add extensions to it.
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

    public void output(Output output) throws WSDLException {
        BindingOutput bindingOutput = definition.createBindingOutput();
        populateBindingOutput(bindingOutput, bindingOperation.getOperation().getOutput());
        bindingOperation.setBindingOutput(bindingOutput);
    }

    /**
     * Called after the {@link BindingOutput} has been created. Subclasses can implement this method to define the name,
     * or add extensions to it.
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

    public void fault(Fault fault) throws WSDLException {
        BindingFault bindingFault = definition.createBindingFault();
        populateBindingFault(bindingFault, fault);
        bindingOperation.addBindingFault(bindingFault);
    }

    /**
     * Called after the {@link BindingFault} has been created. Subclasses can implement this method to define the name,
     * or add extensions to it.
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

    public void endOperation(Operation operation) throws WSDLException {
        binding.addBindingOperation(bindingOperation);
        bindingOperation = null;
    }

    public void endDefinition(Definition definition) throws WSDLException {
        this.definition = null;
    }

}
