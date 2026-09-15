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
