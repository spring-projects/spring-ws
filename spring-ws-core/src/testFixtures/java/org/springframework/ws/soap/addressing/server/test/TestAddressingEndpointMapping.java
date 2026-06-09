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

package org.springframework.ws.soap.addressing.server.test;

import java.net.URI;
import java.util.function.Consumer;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;
import org.springframework.ws.soap.addressing.server.AbstractAddressingEndpointMapping;

/**
 * Test implementation of {@link AbstractAddressingEndpointMapping}.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public class TestAddressingEndpointMapping extends AbstractAddressingEndpointMapping {

	/**
	 * Create an empty mapping using the given context.
	 * @param applicationContext the application context
	 * @return a mapping
	 */
	public static TestAddressingEndpointMapping create(ConfigurableApplicationContext applicationContext) {
		return create(applicationContext, mapping -> {
		});
	}

	/**
	 * Create an empty mapping configured with the given context and apply the given
	 * configuration.
	 * @param applicationContext the application context
	 * @param beanConfiguration the configuration to apply
	 * @return a mapping
	 */
	public static TestAddressingEndpointMapping create(ConfigurableApplicationContext applicationContext,
			Consumer<AbstractAddressingEndpointMapping> beanConfiguration) {
		if (!applicationContext.isActive()) {
			applicationContext.refresh();
		}
		TestAddressingEndpointMapping mapping = new TestAddressingEndpointMapping();
		mapping.setApplicationContext(applicationContext);
		beanConfiguration.accept(mapping);
		try {
			mapping.afterPropertiesSet();
		}
		catch (Exception ex) {
			throw new IllegalStateException("mapping failed to initialize", ex);
		}
		return mapping;
	}

	@Override
	protected Object getEndpointInternal(MessageAddressingProperties map) {
		return new Object();
	}

	@Override
	protected URI getResponseAction(Object endpoint, MessageAddressingProperties requestMap) {
		return URI.create("https://ws.example.com/reply");
	}

	@Override
	protected URI getFaultAction(Object endpoint, MessageAddressingProperties requestMap) {
		return URI.create("https://ws.example.com/fault");
	}

}
