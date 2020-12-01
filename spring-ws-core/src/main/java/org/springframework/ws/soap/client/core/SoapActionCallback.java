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

package org.springframework.ws.soap.client.core;

import java.io.IOException;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapMessage;

/**
 * {@link WebServiceMessageCallback} implementation that sets the SOAP Action header on the message.
 * <p>
 * A usage example with {@link org.springframework.ws.client.core.WebServiceTemplate}:
 * 
 * <pre>
 * WebServiceTemplate template = new WebServiceTemplate(messageFactory);
 * Result result = new DOMResult();
 * template.sendSourceAndReceiveToResult(new StringSource("&lt;content xmlns=\"http://tempuri.org\"/&gt;"),
 * 		new SoapActionCallback("http://tempuri.org/SOAPAction"), result);
 * </pre>
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class SoapActionCallback implements WebServiceMessageCallback {

	private final String soapAction;

	/** Create a new {@code SoapActionCallback} with the given string SOAPAction. */
	public SoapActionCallback(String soapAction) {
		if (!StringUtils.hasText(soapAction)) {
			soapAction = "\"\"";
		}
		this.soapAction = soapAction;
	}

	@Override
	public void doWithMessage(WebServiceMessage message) throws IOException {
		Assert.isInstanceOf(SoapMessage.class, message);
		SoapMessage soapMessage = (SoapMessage) message;
		soapMessage.setSoapAction(soapAction);
	}

}
