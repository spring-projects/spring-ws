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
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class DefaultServiceVisitor implements PortTypeVisitor, BindingVisitor, DefinitionVisitor {

    private Definition definition;

    /** The suffix used to create a binding name from a port type name. */
    private static final String PORT_SUFFIX = "Port";

    /** The suffix used to create a service name from a port type name. */
    private static final String SERVICE_SUFFIX = "Service";

    private Service service;

    public void startDefinition(Definition definition) throws WSDLException {
        this.definition = definition;
        this.service = this.definition.createService();
    }

    public void startPortType(PortType portType) throws WSDLException {
    }

    public void startOperation(Operation operation) throws WSDLException {
    }

    public void input(Input input) throws WSDLException {
    }

    public void output(Output output) throws WSDLException {
    }

    public void fault(Fault fault) throws WSDLException {
    }

    public void endOperation(Operation operation) throws WSDLException {
    }

    public void endPortType(PortType portType) throws WSDLException {
        QName portTypeName = portType.getQName();
        QName serviceName = new QName(portTypeName.getNamespaceURI(), portTypeName.getLocalPart() + SERVICE_SUFFIX);
        service.setQName(serviceName);
    }

    public void startBinding(Binding binding) throws WSDLException {
        Port port = definition.createPort();
        port.setBinding(binding);
        populatePort(port, binding);
        service.addPort(port);
    }


    /**
     * Called after the {@link Port} has been created, but before any sub-elements are added. Subclasses can implement
     * this method to define the port name, or add extensions to it.
     * <p/>
     * Default implementation sets the port name to the port type name with the suffix {@link Port} appended to it.
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
    
    public void startBindingOperation(BindingOperation operation) throws WSDLException {
    }

    public void bindingInput(BindingInput input) throws WSDLException {
    }

    public void bindingOutput(BindingOutput output) throws WSDLException {
    }

    public void bindingFault(BindingFault fault) throws WSDLException {
    }

    public void endBindingOperation(BindingOperation operation) throws WSDLException {
    }

    public void endBinding(Binding binding) throws WSDLException {
    }

    public void endDefinition(Definition definition) throws WSDLException {
        this.definition.addService(service);
        this.definition = null;
        this.service = null;
    }
}
