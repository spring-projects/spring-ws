/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.mapping;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.context.MessageContext;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Abstract base class for {@code EndpointMapping}s that resolve qualified names as
 * registration keys.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractQNameEndpointMapping extends AbstractMapBasedEndpointMapping {

	@Override
	protected final @Nullable String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
		QName qName = resolveQName(messageContext);
		return (qName != null) ? qName.toString() : null;
	}

	/**
	 * Template method that resolves the qualified names from the given SOAP message.
	 * @return an array of qualified names that serve as registration keys
	 */
	protected abstract @Nullable QName resolveQName(MessageContext messageContext) throws Exception;

	@Override
	protected boolean validateLookupKey(String key) {
		return QNameUtils.validateQName(key);
	}

	@Override
	protected Object getLookupKeyPropertyValue(String key) {
		return QName.valueOf(key);
	}

}
