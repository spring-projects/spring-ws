/*
 * Copyright 2005-2025 the original author or authors.
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

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SuffixBasedPortTypesProviderTests {

	private SuffixBasedPortTypesProvider provider;

	private Definition definition;

	@BeforeEach
	public void setUp() throws Exception {

		this.provider = new SuffixBasedPortTypesProvider();
		WSDLFactory factory = WSDLFactory.newInstance();
		this.definition = factory.newDefinition();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddPortTypes() throws Exception {

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
