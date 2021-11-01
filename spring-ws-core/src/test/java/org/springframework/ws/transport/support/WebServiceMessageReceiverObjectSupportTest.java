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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

	@BeforeEach
	public void setUp() throws Exception {

		receiverSupport = new MyReceiverSupport();
		messageFactory = new MockWebServiceMessageFactory();
		receiverSupport.setMessageFactory(messageFactory);
		connectionMock = mock(FaultAwareWebServiceConnection.class);
		request = new MockWebServiceMessage();
	}

	@Test
	public void handleConnectionResponse() throws Exception {

		when(connectionMock.receive(messageFactory)).thenReturn(request);

		connectionMock.setFaultCode(null);
		connectionMock.send(isA(WebServiceMessage.class));
		connectionMock.close();

		WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

			@Override
			public void receive(MessageContext messageContext) throws Exception {
				assertThat(messageContext).isNotNull();
				messageContext.getResponse();
			}
		};

		receiverSupport.handleConnection(connectionMock, receiver);

        verify(connectionMock).receive(messageFactory);
    }

	@Test
	public void handleConnectionFaultResponse() throws Exception {

		final QName faultCode = SoapVersion.SOAP_11.getClientOrSenderFaultName();

		when(connectionMock.receive(messageFactory)).thenReturn(request);
		connectionMock.setFaultCode(faultCode);
		connectionMock.send(isA(WebServiceMessage.class));
		connectionMock.close();

		WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

			@Override
			public void receive(MessageContext messageContext) throws Exception {

				assertThat(messageContext).isNotNull();
				MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();
				response.setFaultCode(faultCode);
			}
		};

		receiverSupport.handleConnection(connectionMock, receiver);

		verify(connectionMock).receive(messageFactory);
	}

	@Test
	public void handleConnectionNoResponse() throws Exception {

        when(connectionMock.receive(messageFactory)).thenReturn(request);
		connectionMock.close();

		WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

			public void receive(MessageContext messageContext) throws Exception {
				assertThat(messageContext).isNotNull();
			}
		};

		receiverSupport.handleConnection(connectionMock, receiver);

		verify(connectionMock).receive(messageFactory);
	}

	private static class MyReceiverSupport extends WebServiceMessageReceiverObjectSupport {

	}
}
