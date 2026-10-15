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

import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.ws.soap.server.endpoint.adapter.method.SoapHeaderElementMethodArgumentResolver;
import org.springframework.ws.soap.server.endpoint.adapter.method.SoapMethodArgumentResolver;
import org.springframework.ws.support.DefaultStrategiesHelper;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

/**
 * {@link RuntimeHintsRegistrar} for the reflection that Spring Web Services performs
 * itself.
 * <ul>
 * <li>The default strategies of the {@link WebServiceTemplate}, the
 * {@link MessageDispatcher}, the {@link SoapMessageDispatcher} and the
 * {@link MessageDispatcherServlet} are listed in properties files, and instantiated by
 * the {@link DefaultStrategiesHelper}.</li>
 * <li>The SOAP-specific method argument resolvers are instantiated by class name by the
 * {@link org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter
 * DefaultMethodEndpointAdapter}, which also detects the optional XML libraries by class
 * name.</li>
 * <li>The WS-Addressing support evaluates XPath expressions with the
 * {@code normalize-space} function, which the XPath implementation of the JDK
 * instantiates reflectively.</li>
 * </ul>
 *
 * @author Sharang Gupta
 * @since 5.1
 */
class WebServicesRuntimeHints implements RuntimeHintsRegistrar {

	private static final Class<?>[] TYPES_WITH_DEFAULT_STRATEGIES = { WebServiceTemplate.class, MessageDispatcher.class,
			SoapMessageDispatcher.class, MessageDispatcherServlet.class };

	private static final Class<?>[] SOAP_METHOD_ARGUMENT_RESOLVERS = { SoapMethodArgumentResolver.class,
			SoapHeaderElementMethodArgumentResolver.class };

	private static final String[] OPTIONAL_XML_LIBRARY_TYPES = { "org.dom4j.Element", "jakarta.xml.bind.Binder",
			"org.jdom2.Element", "javax.xml.stream.XMLInputFactory", "nu.xom.Element" };

	private static final String NORMALIZE_SPACE_XPATH_FUNCTION = "com.sun.org.apache.xpath.internal.functions.FuncNormalizeSpace";

	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
		for (Class<?> type : TYPES_WITH_DEFAULT_STRATEGIES) {
			registerDefaultStrategies(hints, type);
		}
		for (Class<?> resolver : SOAP_METHOD_ARGUMENT_RESOLVERS) {
			hints.reflection().registerType(resolver, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
		}
		for (String type : OPTIONAL_XML_LIBRARY_TYPES) {
			hints.reflection().registerType(TypeReference.of(type));
		}
		hints.reflection()
			.registerType(TypeReference.of(NORMALIZE_SPACE_XPATH_FUNCTION),
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
	}

	/**
	 * Register the properties file of the default strategies of the given type, which is
	 * located like {@link DefaultStrategiesHelper#DefaultStrategiesHelper(Class)} does,
	 * and the constructors of the strategies that it lists.
	 */
	private static void registerDefaultStrategies(RuntimeHints hints, Class<?> type) {
		ClassPathResource resource = new ClassPathResource(ClassUtils.getShortName(type) + ".properties", type);
		hints.resources().registerResource(resource);
		Properties defaultStrategies = loadDefaultStrategies(resource);
		for (String strategyInterface : defaultStrategies.stringPropertyNames()) {
			String[] strategies = StringUtils
				.commaDelimitedListToStringArray(defaultStrategies.getProperty(strategyInterface));
			for (String strategy : strategies) {
				hints.reflection()
					.registerType(TypeReference.of(strategy.trim()), MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
			}
		}
	}

	private static Properties loadDefaultStrategies(ClassPathResource resource) {
		try {
			return PropertiesLoaderUtils.loadProperties(resource);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not load default strategies from " + resource, ex);
		}
	}

}
