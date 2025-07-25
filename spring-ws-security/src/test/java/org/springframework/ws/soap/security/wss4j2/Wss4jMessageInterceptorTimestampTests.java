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

package org.springframework.ws.soap.security.wss4j2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.WsSecurityValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public abstract class Wss4jMessageInterceptorTimestampTests extends Wss4jTests {

	@Test
	void testAddTimestamp() throws Exception {

		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
		interceptor.setSecurementActions("Timestamp");
		interceptor.afterPropertiesSet();
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext context = getSoap11MessageContext(message);
		interceptor.secureMessage(message, context);
		Document document = getDocument(message);

		assertXpathExists("timestamp header not found",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsu:Timestamp", document);
	}

	@Test
	void testValidateTimestamp() throws Exception {

		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
		interceptor.setValidationActions("Timestamp");
		interceptor.afterPropertiesSet();
		SoapMessage message = getMessageWithTimestamp();

		MessageContext context = new DefaultMessageContext(message, getSoap11MessageFactory());
		interceptor.validateMessage(message, context);

		assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
				getDocument(message));
	}

	@Test
	void testValidateTimestampWithExpiredTtl() {

		assertThatExceptionOfType(WsSecurityValidationException.class).isThrownBy(() -> {

			Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
			interceptor.setValidationActions("Timestamp");
			interceptor.afterPropertiesSet();
			SoapMessage message = loadSoap11Message("expiredTimestamp-soap.xml");
			MessageContext context = new DefaultMessageContext(message, getSoap11MessageFactory());
			interceptor.validateMessage(message, context);
		});
	}

	@Test
	void testValidateTimestampWithExpiredTtlCustomTtl() {

		assertThatExceptionOfType(WsSecurityValidationException.class).isThrownBy(() -> {

			Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
			interceptor.setValidationActions("Timestamp");
			interceptor.setValidationTimeToLive(1);
			interceptor.afterPropertiesSet();
			SoapMessage message = getMessageWithTimestamp();
			Thread.sleep(2000);
			MessageContext context = new DefaultMessageContext(message, getSoap11MessageFactory());
			interceptor.validateMessage(message, context);
		});
	}

	@Test
	void testSecureTimestampWithCustomTtl() throws Exception {

		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
		interceptor.setSecurementActions("Timestamp");
		interceptor.setTimestampStrict(true);
		int ttlInSeconds = 1;
		interceptor.setSecurementTimeToLive(ttlInSeconds);
		interceptor.afterPropertiesSet();
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext context = new DefaultMessageContext(message, getSoap11MessageFactory());
		interceptor.secureMessage(message, context);

		String created = this.xpathTemplate.evaluateAsString(
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsu:Timestamp/wsu:Created/text()",
				message.getEnvelope().getSource());
		String expires = this.xpathTemplate.evaluateAsString(
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsu:Timestamp/wsu:Expires/text()",
				message.getEnvelope().getSource());

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");

		long actualTtl = format.parse(expires).getTime() - format.parse(created).getTime();

		assertThat(actualTtl).isEqualTo(1000 * ttlInSeconds);
	}

	private SoapMessage getMessageWithTimestamp() throws Exception {

		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
		interceptor.setSecurementActions("Timestamp");
		interceptor.afterPropertiesSet();
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext context = getSoap11MessageContext(message);
		interceptor.secureMessage(message, context);

		return message;
	}

}
