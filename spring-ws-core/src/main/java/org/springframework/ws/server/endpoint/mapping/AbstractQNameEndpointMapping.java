/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping;

import javax.xml.namespace.QName;

import org.springframework.ws.context.MessageContext;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Abstract base class for {@code EndpointMapping}s that resolve qualified names as registration keys.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractQNameEndpointMapping extends AbstractMapBasedEndpointMapping {

	@Override
	protected final String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
		QName qName = resolveQName(messageContext);
		return qName != null ? qName.toString() : null;
	}

	/**
	 * Template method that resolves the qualified names from the given SOAP message.
	 *
	 * @return an array of qualified names that serve as registration keys
	 */
	protected abstract QName resolveQName(MessageContext messageContext) throws Exception;

	@Override
	protected boolean validateLookupKey(String key) {
		return QNameUtils.validateQName(key);
	}

}
