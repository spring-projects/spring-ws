/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.jms;

import java.io.InputStream;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Test;

import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.WebServiceMessageReceiver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebServiceMessageListener}.
 *
 * @author Stephane Nicoll
 */
class WebServiceMessageListenerTests {

	private final WebServiceMessageFactory messageFactory = mock();

	private final TextMessage requestMessage = mock();

	private final Session session = mock();

	private final WebServiceMessageReceiver messageReceiver = mock();

	@Test
	void testHandleInvalidXmlExceptionDefault() throws Exception {
		WebServiceMessageListener listener = new WebServiceMessageListener();
		listener.setMessageFactory(this.messageFactory);
		listener.setMessageReceiver(this.messageReceiver);
		when(this.requestMessage.getText()).thenReturn("invalid-xml");
		InvalidXmlException originalException = new InvalidXmlException(null);
		when(this.messageFactory.createWebServiceMessage(any(InputStream.class))).thenThrow(originalException);
		assertThatExceptionOfType(JMSException.class)
			.isThrownBy(() -> listener.onMessage(this.requestMessage, this.session))
			.satisfies(ex -> assertThat(ex.getLinkedException()).isSameAs(originalException));
	}

	@Test
	void testHandleInvalidXmlExceptionOverride() throws Exception {
		InvalidXmlException originalException = new InvalidXmlException(null);
		WebServiceMessageListener listener = new WebServiceMessageListener() {
			@Override
			protected void handleInvalidXmlException(Message message, Session session, InvalidXmlException ex) {
				throw new IllegalStateException("Test", ex);
			}
		};
		listener.setMessageFactory(this.messageFactory);
		listener.setMessageReceiver(this.messageReceiver);
		when(this.requestMessage.getText()).thenReturn("invalid-xml");
		when(this.messageFactory.createWebServiceMessage(any(InputStream.class))).thenThrow(originalException);
		assertThatIllegalStateException().isThrownBy(() -> listener.onMessage(this.requestMessage, this.session))
			.withMessage("Test")
			.havingCause()
			.isSameAs(originalException);
	}

}
