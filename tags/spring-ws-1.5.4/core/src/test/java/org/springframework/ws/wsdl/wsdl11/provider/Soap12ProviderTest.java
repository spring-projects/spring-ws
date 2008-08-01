/*
 * Copyright ${YEAR} the original author or authors.
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

import java.util.Properties;
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
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Fault;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class Soap12ProviderTest extends TestCase {

    private Soap12Provider provider;

    private Definition definition;

    protected void setUp() throws Exception {
        provider = new Soap12Provider();
        WSDLFactory factory = WSDLFactory.newInstance();
        definition = factory.newDefinition();
    }

    public void testPopulateBinding() throws Exception {
        String namespace = "http://springframework.org/spring-ws";
        definition.addNamespace("tns", namespace);
        definition.setTargetNamespace(namespace);

        PortType portType = definition.createPortType();
        portType.setQName(new QName(namespace, "PortType"));
        portType.setUndefined(false);
        definition.addPortType(portType);
        Operation operation = definition.createOperation();
        operation.setName("Operation");
        operation.setUndefined(false);
        operation.setStyle(OperationType.REQUEST_RESPONSE);
        portType.addOperation(operation);
        Input input = definition.createInput();
        input.setName("Input");
        operation.setInput(input);
        Output output = definition.createOutput();
        output.setName("Output");
        operation.setOutput(output);
        Fault fault = definition.createFault();
        fault.setName("Fault");
        operation.addFault(fault);

        Properties soapActions = new Properties();
        soapActions.setProperty("Operation", namespace + "/Action");
        provider.setSoapActions(soapActions);

        provider.setServiceName("Service");

        String locationUri = "http://localhost:8080/services";
        provider.setLocationUri(locationUri);

        provider.addBindings(definition);
        provider.addServices(definition);

        Binding binding = definition.getBinding(new QName(namespace, "PortTypeSoap12"));
        assertNotNull("No binding created", binding);
        assertEquals("Invalid port type", portType, binding.getPortType());
        assertEquals("Invalid amount of extensibility elements", 1, binding.getExtensibilityElements().size());

        SOAP12Binding soapBinding = (SOAP12Binding) binding.getExtensibilityElements().get(0);
        assertEquals("Invalid style", "document", soapBinding.getStyle());
        assertEquals("Invalid amount of binding operations", 1, binding.getBindingOperations().size());

        BindingOperation bindingOperation = binding.getBindingOperation("Operation", "Input", "Output");
        assertNotNull("No binding operation created", bindingOperation);
        assertEquals("Invalid amount of extensibility elements", 1, bindingOperation.getExtensibilityElements().size());

        SOAP12Operation soapOperation = (SOAP12Operation) bindingOperation.getExtensibilityElements().get(0);
        assertEquals("Invalid SOAPAction", namespace + "/Action", soapOperation.getSoapActionURI());

        BindingInput bindingInput = bindingOperation.getBindingInput();
        assertNotNull("No binding input", bindingInput);
        assertEquals("Invalid name", "Input", bindingInput.getName());
        assertEquals("Invalid amount of extensibility elements", 1, bindingInput.getExtensibilityElements().size());
        SOAP12Body soapBody = (SOAP12Body) bindingInput.getExtensibilityElements().get(0);
        assertEquals("Invalid soap body use", "literal", soapBody.getUse());

        BindingOutput bindingOutput = bindingOperation.getBindingOutput();
        assertNotNull("No binding output", bindingOutput);
        assertEquals("Invalid name", "Output", bindingOutput.getName());
        assertEquals("Invalid amount of extensibility elements", 1, bindingOutput.getExtensibilityElements().size());
        soapBody = (SOAP12Body) bindingOutput.getExtensibilityElements().get(0);
        assertEquals("Invalid soap body use", "literal", soapBody.getUse());

        BindingFault bindingFault = bindingOperation.getBindingFault("Fault");
        assertNotNull("No binding fault", bindingFault);
        assertEquals("Invalid amount of extensibility elements", 1, bindingFault.getExtensibilityElements().size());
        SOAP12Fault soapFault = (SOAP12Fault) bindingFault.getExtensibilityElements().get(0);
        assertEquals("Invalid soap fault use", "literal", soapFault.getUse());

        Service service = definition.getService(new QName(namespace, "Service"));
        assertNotNull("No Service created", service);
        assertEquals("Invalid amount of ports", 1, service.getPorts().size());

        Port port = service.getPort("PortTypeSoap12");
        assertNotNull("No port created", port);
        assertEquals("Invalid binding", binding, port.getBinding());
        assertEquals("Invalid amount of extensibility elements", 1, port.getExtensibilityElements().size());

        SOAP12Address soapAddress = (SOAP12Address) port.getExtensibilityElements().get(0);
        assertEquals("Invalid soap address", locationUri, soapAddress.getLocationURI());
    }


}