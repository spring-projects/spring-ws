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

import java.util.Properties;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SoapProviderTest {

	private SoapProvider provider;

	private Definition definition;

	@Before
	public void setUp() throws Exception {
		provider = new SoapProvider();
		WSDLFactory factory = WSDLFactory.newInstance();
		definition = factory.newDefinition();
	}

	@Test
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

		provider.setCreateSoap11Binding(true);
		provider.setCreateSoap12Binding(true);

		provider.addBindings(definition);
		provider.addServices(definition);

		Binding binding = definition.getBinding(new QName(namespace, "PortTypeSoap11"));
		Assert.assertNotNull("No SOAP 1.1 binding created", binding);
		binding = definition.getBinding(new QName(namespace, "PortTypeSoap12"));
		Assert.assertNotNull("No SOAP 1.2 binding created", binding);

		Service service = definition.getService(new QName(namespace, "Service"));
		Assert.assertNotNull("No Service created", service);
		Assert.assertEquals("Invalid amount of ports", 2, service.getPorts().size());

		Port port = service.getPort("PortTypeSoap11");
		Assert.assertNotNull("No SOAP 1.1 port created", port);
		port = service.getPort("PortTypeSoap12");
		Assert.assertNotNull("No SOAP 1.2 port created", port);
	}

}
