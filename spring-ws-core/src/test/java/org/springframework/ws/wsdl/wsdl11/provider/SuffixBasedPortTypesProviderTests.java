/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.wsdl.wsdl11.provider;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SuffixBasedPortTypesProviderTests {

	private SuffixBasedPortTypesProvider provider;

	private Definition definition;

	@BeforeEach
	void setUp() throws Exception {

		this.provider = new SuffixBasedPortTypesProvider();
		WSDLFactory factory = WSDLFactory.newInstance();
		this.definition = factory.newDefinition();
	}

	@Test
	@SuppressWarnings("unchecked")
	void testAddPortTypes() throws Exception {

		String namespace = "http://springframework.org/spring-ws";
		this.definition.addNamespace("tns", namespace);
		this.definition.setTargetNamespace(namespace);

		Message message = this.definition.createMessage();
		message.setQName(new QName(namespace, "OperationRequest"));
		this.definition.addMessage(message);

		message = this.definition.createMessage();
		message.setQName(new QName(namespace, "OperationResponse"));
		this.definition.addMessage(message);

		message = this.definition.createMessage();
		message.setQName(new QName(namespace, "OperationFault"));
		this.definition.addMessage(message);

		this.provider.setPortTypeName("PortType");
		this.provider.addPortTypes(this.definition);

		PortType portType = this.definition.getPortType(new QName(namespace, "PortType"));

		assertThat(portType).isNotNull();

		Operation operation = portType.getOperation("Operation", "OperationRequest", "OperationResponse");

		assertThat(operation).isNotNull();
		assertThat(operation.getInput()).isNotNull();
		assertThat(operation.getOutput()).isNotNull();
		assertThat(operation.getFaults()).isNotEmpty();
	}

}
