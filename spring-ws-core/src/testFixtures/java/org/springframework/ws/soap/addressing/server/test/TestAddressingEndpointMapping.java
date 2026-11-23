/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
