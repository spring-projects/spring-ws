/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.transport.support;

import static org.easymock.EasyMock.*;

import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageReceiver;

public class WebServiceMessageReceiverObjectSupportTest {

	private WebServiceMessageReceiverObjectSupport receiverSupport;

	private FaultAwareWebServiceConnection connectionMock;

	private MockWebServiceMessageFactory messageFactory;

	private MockWebServiceMessage request;

	@Before
	public void setUp() throws Exception {

		receiverSupport = new MyReceiverSupport();
		messageFactory = new MockWebServiceMessageFactory();
		receiverSupport.setMessageFactory(messageFactory);
		connectionMock = createStrictMock(FaultAwareWebServiceConnection.class);
		request = new MockWebServiceMessage();
	}

	@Test
	public void handleConnectionResponse() throws Exception {

		expect(connectionMock.receive(messageFactory)).andReturn(request);
		connectionMock.setFaultCode(null);
		connectionMock.send(isA(WebServiceMessage.class));
		connectionMock.close();

		replay(connectionMock);

		WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

			@Override
			public void receive(MessageContext messageContext) throws Exception {
				Assert.assertNotNull("No message context", messageContext);
				messageContext.getResponse();
			}
		};

		receiverSupport.handleConnection(connectionMock, receiver);

		verify(connectionMock);
	}

	@Test
	public void handleConnectionFaultResponse() throws Exception {
		final QName faultCode = SoapVersion.SOAP_11.getClientOrSenderFaultName();

		expect(connectionMock.receive(messageFactory)).andReturn(request);
		connectionMock.setFaultCode(faultCode);
		connectionMock.send(isA(WebServiceMessage.class));
		connectionMock.close();

		replay(connectionMock);

		WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

			@Override
			public void receive(MessageContext messageContext) throws Exception {
				Assert.assertNotNull("No message context", messageContext);
				MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();
				response.setFaultCode(faultCode);
			}
		};

		receiverSupport.handleConnection(connectionMock, receiver);

		verify(connectionMock);
	}

	@Test
	public void handleConnectionNoResponse() throws Exception {

		expect(connectionMock.receive(messageFactory)).andReturn(request);
		connectionMock.close();

		replay(connectionMock);

		WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

			public void receive(MessageContext messageContext) throws Exception {
				Assert.assertNotNull("No message context", messageContext);
			}
		};

		receiverSupport.handleConnection(connectionMock, receiver);

		verify(connectionMock);
	}

	private static class MyReceiverSupport extends WebServiceMessageReceiverObjectSupport {

	}
}
