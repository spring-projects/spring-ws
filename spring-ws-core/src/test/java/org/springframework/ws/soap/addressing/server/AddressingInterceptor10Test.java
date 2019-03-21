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

package org.springframework.ws.soap.addressing.server;

import java.net.URI;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.version.Addressing10;
import org.springframework.ws.soap.addressing.version.AddressingVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertTrue;

public class AddressingInterceptor10Test extends AbstractAddressingInterceptorTestCase {

	@Override
	protected AddressingVersion getVersion() {
		return new Addressing10();
	}

	@Override
	protected String getTestPath() {
		return "10";
	}

	public void testNoTo() throws Exception {
		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-no-to.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
		URI messageId = new URI("uid:1234");
		expect(strategyMock.newMessageId((SoapMessage) context.getResponse())).andReturn(messageId);
		replay(strategyMock);
		boolean result = interceptor.handleResponse(context, null);
		assertTrue("Request with no To not handled", result);
		assertTrue("Message Context has no response", context.hasResponse());
		SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-anonymous.xml");
		assertXMLEqual("Invalid response for message with invalid MAP", expectedResponse,
				(SaajSoapMessage) context.getResponse());
		verify(strategyMock);
	}

}