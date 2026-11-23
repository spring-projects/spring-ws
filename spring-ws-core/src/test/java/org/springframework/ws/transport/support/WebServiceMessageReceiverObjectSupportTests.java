/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.support;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebServiceMessageReceiverObjectSupportTests {

	private WebServiceMessageReceiverObjectSupport receiverSupport;

	private FaultAwareWebServiceConnection connectionMock;

	private MockWebServiceMessageFactory messageFactory;

	private MockWebServiceMessage request;

	@BeforeEach
	void setUp() {

		this.receiverSupport = new MyReceiverSupport();
		this.messageFactory = new MockWebServiceMessageFactory();
		this.receiverSupport.setMessageFactory(this.messageFactory);
		this.connectionMock = mock(FaultAwareWebServiceConnection.class);
		this.request = new MockWebServiceMessage();
	}

	@Test
	void handleConnectionResponse() throws Exception {

		when(this.connectionMock.receive(this.messageFactory)).thenReturn(this.request);

		this.connectionMock.setFaultCode(null);
		this.connectionMock.send(isA(WebServiceMessage.class));
		this.connectionMock.close();

		WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

			@Override
			public void receive(MessageContext messageContext) {
				assertThat(messageContext).isNotNull();
				messageContext.getResponse();
			}
		};

		this.receiverSupport.handleConnection(this.connectionMock, receiver);

		verify(this.connectionMock).receive(this.messageFactory);
	}

	@Test
	void handleConnectionFaultResponse() throws Exception {

		final QName faultCode = SoapVersion.SOAP_11.getClientOrSenderFaultName();

		when(this.connectionMock.receive(this.messageFactory)).thenReturn(this.request);
		this.connectionMock.setFaultCode(faultCode);
		this.connectionMock.send(isA(WebServiceMessage.class));
		this.connectionMock.close();

		WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

			@Override
			public void receive(MessageContext messageContext) {

				assertThat(messageContext).isNotNull();
				MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();
				response.setFaultCode(faultCode);
			}
		};

		this.receiverSupport.handleConnection(this.connectionMock, receiver);

		verify(this.connectionMock).receive(this.messageFactory);
	}

	@Test
	void handleConnectionNoResponse() throws Exception {

		when(this.connectionMock.receive(this.messageFactory)).thenReturn(this.request);
		this.connectionMock.close();

		WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

			public void receive(MessageContext messageContext) {
				assertThat(messageContext).isNotNull();
			}
		};

		this.receiverSupport.handleConnection(this.connectionMock, receiver);

		verify(this.connectionMock).receive(this.messageFactory);
	}

	private static final class MyReceiverSupport extends WebServiceMessageReceiverObjectSupport {

	}

}
