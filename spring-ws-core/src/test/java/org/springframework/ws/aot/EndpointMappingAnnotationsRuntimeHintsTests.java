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

package org.springframework.ws.aot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.annotation.ReflectiveRuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.soap.addressing.server.annotation.Action;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the runtime hints of the endpoint methods that are mapped with the
 * {@link PayloadRoot}, {@link SoapAction} and {@link Action} annotations.
 *
 * @author Sharang Gupta
 */
class EndpointMappingAnnotationsRuntimeHintsTests {

	private final RuntimeHints hints = new RuntimeHints();

	@ParameterizedTest
	@ValueSource(strings = { "payloadRoot", "payloadRoots", "soapAction", "soapActions", "action" })
	void mappedEndpointMethodCanBeInvoked(String methodName) {
		new ReflectiveRuntimeHintsRegistrar().registerRuntimeHints(this.hints, SampleEndpoint.class);

		assertThat(RuntimeHintsPredicates.reflection().onMethodInvocation(SampleEndpoint.class, methodName))
			.accepts(this.hints);
	}

	@Test
	void unmappedEndpointMethodIsNotRegistered() {
		new ReflectiveRuntimeHintsRegistrar().registerRuntimeHints(this.hints, SampleEndpoint.class);

		assertThat(RuntimeHintsPredicates.reflection().onMethodInvocation(SampleEndpoint.class, "unmapped"))
			.rejects(this.hints);
	}

	@Endpoint
	static class SampleEndpoint {

		@PayloadRoot(namespace = "https://example.com/samples", localPart = "Request")
		void payloadRoot() {
		}

		@PayloadRoot(namespace = "https://example.com/samples", localPart = "FirstRequest")
		@PayloadRoot(namespace = "https://example.com/samples", localPart = "SecondRequest")
		void payloadRoots() {
		}

		@SoapAction("https://example.com/samples/Action")
		void soapAction() {
		}

		@SoapAction("https://example.com/samples/FirstAction")
		@SoapAction("https://example.com/samples/SecondAction")
		void soapActions() {
		}

		@Action("https://example.com/samples/AddressingAction")
		void action() {
		}

		void unmapped() {
		}

	}

}
