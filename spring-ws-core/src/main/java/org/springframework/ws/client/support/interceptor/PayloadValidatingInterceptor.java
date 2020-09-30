/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.client.support.interceptor;

import javax.xml.transform.Source;

import org.springframework.ws.WebServiceMessage;

/**
 * Client-side interceptor that validates the contents of {@code WebServiceMessage}s using a schema. Allows for both W3C
 * XML and RELAX NG schemas.
 * <p>
 * When the payload is invalid, this interceptor stops processing of the interceptor chain.
 * <p>
 * The schema to validate against is set with the {@code schema} property or {@code schemas} property. By default, only
 * the request message is validated, but this behaviour can be changed using the {@code validateRequest} and
 * {@code validateResponse} properties. Responses that contains faults are not validated.
 *
 * @author Stefan Schmidt
 * @author Arjen Poutsma
 * @see #setSchema(org.springframework.core.io.Resource)
 * @see #setSchemas(org.springframework.core.io.Resource[])
 * @see #setValidateRequest(boolean)
 * @see #setValidateResponse(boolean)
 * @since 1.5.4
 */
public class PayloadValidatingInterceptor extends AbstractValidatingInterceptor {

	/**
	 * Returns the part of the request message that is to be validated. Default
	 *
	 * @param request the request message
	 * @return the part of the message that is to validated, or {@code null} not to validate anything
	 */
	@Override
	protected Source getValidationRequestSource(WebServiceMessage request) {
		return request.getPayloadSource();
	}

	/**
	 * Returns the part of the response message that is to be validated.
	 *
	 * @param response the response message
	 * @return the part of the message that is to validated, or {@code null} not to validate anything
	 */
	@Override
	protected Source getValidationResponseSource(WebServiceMessage response) {
		return response.getPayloadSource();
	}
}
