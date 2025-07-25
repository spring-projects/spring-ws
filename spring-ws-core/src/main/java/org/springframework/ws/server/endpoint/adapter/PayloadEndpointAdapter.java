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

package org.springframework.ws.server.endpoint.adapter;

import javax.xml.transform.Source;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointAdapter;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.server.endpoint.PayloadEndpoint;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Adapter to use a {@code PayloadEndpoint} as the endpoint for a
 * {@code EndpointInvocationChain}.
 * <p>
 * This adapter is registered by default by the {@link MessageDispatcher} and
 * {@link SoapMessageDispatcher}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.ws.server.endpoint.PayloadEndpoint
 * @see org.springframework.ws.server.EndpointInvocationChain
 */
public class PayloadEndpointAdapter extends TransformerObjectSupport implements EndpointAdapter {

	@Override
	public boolean supports(Object endpoint) {
		return endpoint instanceof PayloadEndpoint;
	}

	@Override
	public void invoke(MessageContext messageContext, Object endpoint) throws Exception {
		PayloadEndpoint payloadEndpoint = (PayloadEndpoint) endpoint;
		Source requestSource = messageContext.getRequest().getPayloadSource();
		Source responseSource = payloadEndpoint.invoke(requestSource);
		if (responseSource != null) {
			WebServiceMessage response = messageContext.getResponse();
			transform(responseSource, response.getPayloadResult());
		}
	}

}
