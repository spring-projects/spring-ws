/*
 * Copyright 2005-2014 the original author or authors.
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

import javax.xml.transform.Source;

import org.springframework.core.MethodParameter;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

/**
 * Abstract base class for {@link MethodArgumentResolver} and {@link MethodReturnValueHandler} implementations based on
 * {@link Source}s.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class AbstractPayloadSourceMethodProcessor extends AbstractPayloadMethodProcessor {

	// MethodArgumentResolver

	@Override
	public final Object resolveArgument(MessageContext messageContext, MethodParameter parameter) throws Exception {
		Source requestPayload = getRequestPayload(messageContext);
		return requestPayload != null ? resolveRequestPayloadArgument(parameter, requestPayload) : null;
	}

	/** Returns the request payload as {@code Source}. */
	private Source getRequestPayload(MessageContext messageContext) {
		WebServiceMessage request = messageContext.getRequest();
		return request != null ? request.getPayloadSource() : null;
	}

	/**
	 * Resolves the given parameter, annotated with {@link RequestPayload}, into a method argument.
	 *
	 * @param parameter		 the parameter to resolve to an argument
	 * @param requestPayload the request payload
	 * @return the resolved argument. May be {@code null}.
	 * @throws Exception in case of errors
	 */
	protected abstract Object resolveRequestPayloadArgument(MethodParameter parameter, Source requestPayload)
			throws Exception;

	// MethodReturnValueHandler

	@Override
	public final void handleReturnValue(MessageContext messageContext, MethodParameter returnType, Object returnValue)
			throws Exception {
		if (returnValue != null) {
			Source responsePayload = createResponsePayload(returnType, returnValue);
			if (responsePayload != null) {
				WebServiceMessage response = messageContext.getResponse();
				transform(responsePayload, response.getPayloadResult());
			}
		}
	}

	/**
	 * Creates a response payload for the given return value.
	 *
	 * @param returnType  the return type to handle
	 * @param returnValue the return value to handle
	 * @return the response payload
	 * @throws Exception in case of errors
	 */
	protected abstract Source createResponsePayload(MethodParameter returnType, Object returnValue) throws Exception;

}
