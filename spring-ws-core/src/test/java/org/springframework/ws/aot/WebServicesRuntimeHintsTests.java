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

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.server.endpoint.adapter.method.SoapHeaderElementMethodArgumentResolver;
import org.springframework.ws.soap.server.endpoint.adapter.method.SoapMethodArgumentResolver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WebServicesRuntimeHints}.
 *
 * @author Sharang Gupta
 */
class WebServicesRuntimeHintsTests {

	private final RuntimeHints hints = new RuntimeHints();

	@BeforeEach
	void registerHints() {
		SpringFactoriesLoader.forResourceLocation("META-INF/spring/aot.factories")
			.load(RuntimeHintsRegistrar.class)
			.forEach((registrar) -> registrar.registerHints(this.hints, ClassUtils.getDefaultClassLoader()));
	}

	@ParameterizedTest
	@ValueSource(strings = { "org/springframework/ws/client/core/WebServiceTemplate.properties",
			"org/springframework/ws/server/MessageDispatcher.properties",
			"org/springframework/ws/soap/server/SoapMessageDispatcher.properties",
			"org/springframework/ws/transport/http/MessageDispatcherServlet.properties" })
	void defaultStrategiesCanBeLoaded(String location) throws IOException {
		assertThat(RuntimeHintsPredicates.resource().forResource(location)).accepts(this.hints);
		Properties defaultStrategies = PropertiesLoaderUtils.loadProperties(new ClassPathResource(location));
		assertThat(defaultStrategies).isNotEmpty();
		for (String strategies : defaultStrategies.stringPropertyNames()) {
			for (String className : StringUtils
				.commaDelimitedListToStringArray(defaultStrategies.getProperty(strategies))) {
				assertThat(RuntimeHintsPredicates.reflection()
					.onType(TypeReference.of(className.trim()))
					.withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(this.hints);
			}
		}
	}

	@ParameterizedTest
	@ValueSource(classes = { SoapMethodArgumentResolver.class, SoapHeaderElementMethodArgumentResolver.class })
	void soapMethodArgumentResolversCanBeInstantiated(Class<?> resolverType) {
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(resolverType)
			.withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(this.hints);
	}

	@ParameterizedTest
	@ValueSource(strings = { "org.dom4j.Element", "jakarta.xml.bind.Binder", "org.jdom2.Element",
			"javax.xml.stream.XMLInputFactory", "nu.xom.Element" })
	void optionalXmlLibrariesCanBeDetectedWhenPresent(String className) {
		assertThat(RuntimeHintsPredicates.reflection().onType(TypeReference.of(className)).test(this.hints))
			.isEqualTo(ClassUtils.isPresent(className, ClassUtils.getDefaultClassLoader()));
	}

	@Test
	void normalizeSpaceXPathFunctionOfTheJdkCanBeInstantiated() {
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(TypeReference.of("com.sun.org.apache.xpath.internal.functions.FuncNormalizeSpace"))
			.withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(this.hints);
	}

}
