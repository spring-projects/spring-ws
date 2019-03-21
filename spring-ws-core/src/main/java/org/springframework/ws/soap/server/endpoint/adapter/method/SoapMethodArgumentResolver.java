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

package org.springframework.ws.soap.server.endpoint.adapter.method;

import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;

/**
 * Implementation of {@link MethodArgumentResolver} that supports {@link SoapMessage}, {@link SoapBody}, {@link
 * SoapEnvelope}, and {@link SoapHeader}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class SoapMethodArgumentResolver implements MethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> parameterType = parameter.getParameterType();
		return SoapMessage.class.equals(parameterType) || SoapBody.class.equals(parameterType) ||
				SoapEnvelope.class.equals(parameterType) || SoapHeader.class.equals(parameterType);
	}

	@Override
	public Object resolveArgument(MessageContext messageContext, MethodParameter parameter) {
		Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest());
		SoapMessage request = (SoapMessage) messageContext.getRequest();

		Class<?> parameterType = parameter.getParameterType();

		if (SoapMessage.class.equals(parameterType)) {
			return request;
		}
		else if (SoapBody.class.equals(parameterType)) {
			return request.getSoapBody();
		}
		else if (SoapEnvelope.class.equals(parameterType)) {
			return request.getEnvelope();
		}
		else if (SoapHeader.class.equals(parameterType)) {
			return request.getSoapHeader();
		}
		// should not happen
		throw new UnsupportedOperationException();
	}
}
