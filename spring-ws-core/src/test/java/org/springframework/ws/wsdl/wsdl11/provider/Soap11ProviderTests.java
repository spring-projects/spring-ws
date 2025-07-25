/*
 * Copyright 2005-present the original author or authors.
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
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Soap11ProviderTests {

	private Soap11Provider provider;

	private Definition definition;

	@BeforeEach
	void setUp() throws Exception {

		this.provider = new Soap11Provider();
		WSDLFactory factory = WSDLFactory.newInstance();
		this.definition = factory.newDefinition();
	}

	@Test
	@SuppressWarnings("unchecked")
	void testPopulateBinding() throws Exception {

		String namespace = "http://springframework.org/spring-ws";
		this.definition.addNamespace("tns", namespace);
		this.definition.setTargetNamespace(namespace);

		PortType portType = this.definition.createPortType();
		portType.setQName(new QName(namespace, "PortType"));
		portType.setUndefined(false);
		this.definition.addPortType(portType);
		Operation operation = this.definition.createOperation();
		operation.setName("Operation");
		operation.setUndefined(false);
		operation.setStyle(OperationType.REQUEST_RESPONSE);
		portType.addOperation(operation);
		Input input = this.definition.createInput();
		input.setName("Input");
		operation.setInput(input);
		Output output = this.definition.createOutput();
		output.setName("Output");
		operation.setOutput(output);
		Fault fault = this.definition.createFault();
		fault.setName("Fault");
		operation.addFault(fault);

		Properties soapActions = new Properties();
		soapActions.setProperty("Operation", namespace + "/Action");
		this.provider.setSoapActions(soapActions);

		this.provider.setServiceName("Service");

		String locationUri = "http://localhost:8080/services";
		this.provider.setLocationUri(locationUri);

		this.provider.addBindings(this.definition);
		this.provider.addServices(this.definition);

		Binding binding = this.definition.getBinding(new QName(namespace, "PortTypeSoap11"));

		assertThat(binding).isNotNull();
		assertThat(binding.getPortType()).isEqualTo(portType);
		assertThat(binding.getExtensibilityElements()).hasSize(1);

		SOAPBinding soapBinding = (SOAPBinding) binding.getExtensibilityElements().get(0);

		assertThat(soapBinding.getStyle()).isEqualTo("document");
		assertThat(binding.getBindingOperations()).hasSize(1);

		BindingOperation bindingOperation = binding.getBindingOperation("Operation", "Input", "Output");

		assertThat(bindingOperation).isNotNull();
		assertThat(bindingOperation.getExtensibilityElements()).hasSize(1);

		SOAPOperation soapOperation = (SOAPOperation) bindingOperation.getExtensibilityElements().get(0);

		assertThat(soapOperation.getSoapActionURI()).isEqualTo(namespace + "/Action");

		BindingInput bindingInput = bindingOperation.getBindingInput();

		assertThat(bindingInput).isNotNull();
		assertThat(bindingInput.getName()).isEqualTo("Input");
		assertThat(bindingInput.getExtensibilityElements()).hasSize(1);

		SOAPBody soapBody = (SOAPBody) bindingInput.getExtensibilityElements().get(0);

		assertThat(soapBody.getUse()).isEqualTo("literal");

		BindingOutput bindingOutput = bindingOperation.getBindingOutput();

		assertThat(bindingOutput).isNotNull();
		assertThat(bindingOutput.getName()).isEqualTo("Output");
		assertThat(bindingOutput.getExtensibilityElements()).hasSize(1);

		soapBody = (SOAPBody) bindingOutput.getExtensibilityElements().get(0);

		assertThat(soapBody.getUse()).isEqualTo("literal");

		BindingFault bindingFault = bindingOperation.getBindingFault("Fault");

		assertThat(bindingFault).isNotNull();
		assertThat(bindingFault.getExtensibilityElements()).hasSize(1);

		SOAPFault soapFault = (SOAPFault) bindingFault.getExtensibilityElements().get(0);

		assertThat(soapFault.getUse()).isEqualTo("literal");

		Service service = this.definition.getService(new QName(namespace, "Service"));

		assertThat(service).isNotNull();
		assertThat(service.getPorts()).hasSize(1);

		Port port = service.getPort("PortTypeSoap11");

		assertThat(port).isNotNull();
		assertThat(port.getBinding()).isEqualTo(binding);
		assertThat(port.getExtensibilityElements()).hasSize(1);

		SOAPAddress soapAddress = (SOAPAddress) port.getExtensibilityElements().get(0);

		assertThat(soapAddress.getLocationURI()).isEqualTo(locationUri);
	}

}
