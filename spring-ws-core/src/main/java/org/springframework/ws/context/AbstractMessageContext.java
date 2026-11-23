/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.context;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * Abstract base class for {@link MessageContext} instances.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractMessageContext implements MessageContext {

	/**
	 * Keys are {@code Strings}, values are {@code Objects}. Lazily initialized by
	 * {@code getProperties()}.
	 */
	private @Nullable Map<String, Object> properties;

	@Override
	public boolean containsProperty(String name) {
		return getProperties().containsKey(name);
	}

	@Override
	public @Nullable Object getProperty(String name) {
		return getProperties().get(name);
	}

	@Override
	public String[] getPropertyNames() {
		return StringUtils.toStringArray(getProperties().keySet());
	}

	@Override
	public void removeProperty(String name) {
		getProperties().remove(name);
	}

	@Override
	public void setProperty(String name, Object value) {
		getProperties().put(name, value);
	}

	private Map<String, Object> getProperties() {
		if (this.properties == null) {
			this.properties = new HashMap<>();
		}
		return this.properties;
	}

}
