/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.wss4j2;

import java.net.URI;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.w3c.dom.Document;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.server.test.TestAddressingEndpointMapping;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration test using {@link Wss4jSecurityInterceptor} with out-of-band replies
 *
 * @author Stephane Nicoll
 */
class SaajWss4jSecurityInterceptorAddressingIntegrationTests extends Wss4jTests {

	@Test
	void outOfBandReplyIsSecuredByWss4jInterceptor() throws Exception {
		Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();
		securityInterceptor.setValidateRequest(false);
		securityInterceptor.setSecurementActions("Timestamp");
		securityInterceptor.afterPropertiesSet();

		URI replyTo = new URI("http://example.com/business/client1");
		WebServiceMessageSender sender = mock(WebServiceMessageSender.class);
		WebServiceConnection connection = mock(WebServiceConnection.class);
		when(sender.supportsRemoteDestinations()).thenReturn(true);
		when(sender.supports(eq(replyTo), eq(UriSource.REMOTE))).thenReturn(true);
		when(sender.createConnection(replyTo)).thenReturn(connection);

		TestAddressingEndpointMapping mapping = TestAddressingEndpointMapping.create(new StaticApplicationContext(),
				(config) -> {
					config.setPreInterceptors(new EndpointInterceptor[] { securityInterceptor });
					config.setMessageSenders(new WebServiceMessageSender[] { sender });
				});

		MessageDispatcher dispatcher = new MessageDispatcher();
		dispatcher.setEndpointMappings(Collections.singletonList(mapping));
		dispatcher.setEndpointAdapters(Collections.singletonList(new ResponseProducingEndpointAdapter()));

		SoapMessage request = loadSaaj11Message("addressing-request.xml");
		MessageContext messageContext = new DefaultMessageContext(request, getSoap11MessageFactory());
		dispatcher.receive(messageContext);

		ArgumentCaptor<WebServiceMessage> sent = ArgumentCaptor.forClass(WebServiceMessage.class);
		verify(connection).send(sent.capture());
		verify(connection).close();

		Document sentDocument = getDocument((SoapMessage) sent.getValue());
		assertXpathExists("WS-Security timestamp not applied to out-of-band reply",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsu:Timestamp", sentDocument);
		assertThat(messageContext.hasResponse()).isFalse();
	}

	private static final class ResponseProducingEndpointAdapter implements EndpointAdapter {

		@Override
		public boolean supports(Object endpoint) {
			return true;
		}

		@Override
		public void invoke(MessageContext messageContext, Object endpoint) {
			messageContext.getResponse();
		}

	}

}
