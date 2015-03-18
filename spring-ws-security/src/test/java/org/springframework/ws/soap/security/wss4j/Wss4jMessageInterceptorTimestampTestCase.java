/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.security.wss4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.WsSecurityValidationException;

import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;

public abstract class Wss4jMessageInterceptorTimestampTestCase extends Wss4jTestCase {

	@Test
	public void testAddTimestamp() throws Exception {
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
	public void testValidateTimestamp() throws Exception {
		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
		interceptor.setValidationActions("Timestamp");
		interceptor.afterPropertiesSet();
		SoapMessage message = getMessageWithTimestamp();

		MessageContext context = new DefaultMessageContext(message, getSoap11MessageFactory());
		interceptor.validateMessage(message, context);
		assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
				getDocument(message));
	}

	@Test(expected = WsSecurityValidationException.class)
	public void testValidateTimestampWithExpiredTtl() throws Exception {
		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
		interceptor.setValidationActions("Timestamp");
		interceptor.afterPropertiesSet();
		SoapMessage message = loadSoap11Message("expiredTimestamp-soap.xml");
		MessageContext context = new DefaultMessageContext(message, getSoap11MessageFactory());
		interceptor.validateMessage(message, context);
	}


	@Test
	public void testSecureTimestampWithCustomTtl() throws Exception {
		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
		interceptor.setSecurementActions("Timestamp");
		interceptor.setTimestampStrict(true);
		int ttlInSeconds = 1;
		interceptor.setSecurementTimeToLive(ttlInSeconds);
		interceptor.afterPropertiesSet();
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext context = new DefaultMessageContext(message, getSoap11MessageFactory());
		interceptor.secureMessage(message, context);
		
		String created = xpathTemplate.evaluateAsString("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsu:Timestamp/wsu:Created/text()",
				message.getEnvelope().getSource());
		String expires = xpathTemplate.evaluateAsString("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsu:Timestamp/wsu:Expires/text()",
				message.getEnvelope().getSource());

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");

		long actualTtl = format.parse(expires).getTime() - format.parse(created).getTime();
		assertEquals("invalid ttl", 1000 * ttlInSeconds, actualTtl);
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
