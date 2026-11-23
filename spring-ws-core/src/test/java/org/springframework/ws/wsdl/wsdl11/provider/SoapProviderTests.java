/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SoapProviderTests {

	private SoapProvider provider;

	private Definition definition;

	@BeforeEach
	void setUp() throws Exception {

		this.provider = new SoapProvider();
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

		this.provider.setCreateSoap11Binding(true);
		this.provider.setCreateSoap12Binding(true);

		this.provider.addBindings(this.definition);
		this.provider.addServices(this.definition);

		Binding binding = this.definition.getBinding(new QName(namespace, "PortTypeSoap11"));

		assertThat(binding).isNotNull();

		binding = this.definition.getBinding(new QName(namespace, "PortTypeSoap12"));

		assertThat(binding).isNotNull();

		Service service = this.definition.getService(new QName(namespace, "Service"));

		assertThat(service).isNotNull();
		assertThat(service.getPorts()).hasSize(2);

		Port port = service.getPort("PortTypeSoap11");

		assertThat(port).isNotNull();

		port = service.getPort("PortTypeSoap12");

		assertThat(port).isNotNull();
	}

}
