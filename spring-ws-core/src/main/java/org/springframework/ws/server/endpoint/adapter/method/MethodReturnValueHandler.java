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

package org.springframework.ws.server.endpoint.adapter.method;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;

/**
 * Strategy interface used to handle method return values. This interface is used to allow the {@link
 * org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter DefaultMethodEndpointAdapter} to be
 * indefinitely extensible.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface MethodReturnValueHandler {

	/**
	 * Indicates whether the given {@linkplain MethodParameter method return type} is supported by this handler.
	 *
	 * @param returnType the method return type to check
	 * @return {@code true} if this handler supports the supplied return type; {@code false} otherwise
	 */
	boolean supportsReturnType(MethodParameter returnType);

	/**
	 * Handles the given return value.
	 *
	 * @param messageContext the current message context
	 * @param returnType	 the return type to handle. This type must have previously been passed to the {@link
	 *						 #supportsReturnType(MethodParameter)} method of this interface, which must have returned
	 *						 {@code true}.
	 * @param returnValue	 the return value to handle
	 * @throws Exception in case of errors
	 */
	void handleReturnValue(MessageContext messageContext, MethodParameter returnType, Object returnValue)
			throws Exception;


}
