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

import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.PortType;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PolicyProvider}.
 *
 * @author Stephane Nicoll
 */
class PolicyProviderTests {

	private PolicyProvider provider;

	private Definition definition;

	private String namespace;

	@BeforeEach
	void setUp() throws Exception {
		this.provider = new PolicyProvider();
		this.provider.setDelegate(new Soap11Provider());
		WSDLFactory factory = WSDLFactory.newInstance();
		this.definition = factory.newDefinition();
		this.namespace = "http://springframework.org/spring-ws";
		this.definition.addNamespace("tns", this.namespace);
		this.definition.setTargetNamespace(this.namespace);
		PortType portType = this.definition.createPortType();
		portType.setQName(new QName(this.namespace, "PortType"));
		portType.setUndefined(false);
		this.definition.addPortType(portType);
		Operation operation = this.definition.createOperation();
		operation.setName("Operation");
		operation.setUndefined(false);
		operation.setStyle(OperationType.REQUEST_RESPONSE);
		Input input = this.definition.createInput();
		input.setName("Input");
		operation.setInput(input);
		Output output = this.definition.createOutput();
		output.setName("Output");
		operation.setOutput(output);
		portType.addOperation(operation);
	}

	@Test
	void addBindingsWithoutPolicyDelegatesOnly() throws Exception {
		this.provider.addBindings(this.definition);
		Binding binding = this.definition.getBinding(new QName(this.namespace, "PortTypeSoap11"));
		assertThat(binding).isNotNull();
		assertThat(getExtensibilityElements(binding)).hasSize(1);
	}

	@Test
	void addBindingsWithPolicyAttachesPolicyToEveryBinding() throws Exception {
		Resource policy = new ClassPathResource("policy.xml", getClass());
		this.provider.setPolicy(policy);
		this.provider.addBindings(this.definition);

		Binding binding = this.definition.getBinding(new QName(this.namespace, "PortTypeSoap11"));
		assertThat(binding).isNotNull();
		List<?> extensibilityElements = binding.getExtensibilityElements();
		assertThat(extensibilityElements).hasSize(2);
		UnknownExtensibilityElement policyExtension = (UnknownExtensibilityElement) extensibilityElements.get(1);
		assertThat(policyExtension.getElementType()).isEqualTo(new QName("http://www.w3.org/ns/ws-policy", "Policy"));
		assertThat(policyExtension.getElement()
			.getAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id"))
			.isEqualTo("UsernameTokenPolicy");
	}

	private static List<?> getExtensibilityElements(Binding bindings) {
		return bindings.getExtensibilityElements();
	}

}
