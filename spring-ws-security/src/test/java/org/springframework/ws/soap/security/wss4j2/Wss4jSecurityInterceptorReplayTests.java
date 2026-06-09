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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import jakarta.xml.soap.MessageFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.wss4j.common.cache.ReplayCache;
import org.apache.wss4j.dom.WSConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.security.WsSecurityValidationException;
import org.springframework.ws.soap.security.wss4j2.cache.ConcurrentMapReplayCache;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests WSS4J security interceptor with {@link ReplayCache}.
 *
 * @author Stephane Nicoll
 */
class Wss4jSecurityInterceptorReplayTests {

	private SoapMessageFactory messageFactory;

	private SimplePasswordValidationCallbackHandler callbackHandler;

	@BeforeEach
	void setup() throws Exception {
		MessageFactory saajFactory = MessageFactory.newInstance();
		this.messageFactory = new SaajSoapMessageFactory(saajFactory);
		this.callbackHandler = new SimplePasswordValidationCallbackHandler();
		Properties users = new Properties();
		users.setProperty("Bert", "Ernie");
		this.callbackHandler.setUsers(users);
	}

	@Test
	void replayedUsernameTokenDigestIsRejectedWhenNonceReplayCacheConfigured() throws Exception {
		Wss4jSecurityInterceptor secure = new Wss4jSecurityInterceptor();
		secure.setSecurementActions("UsernameToken");
		secure.setSecurementUsername("Bert");
		secure.setSecurementPassword("Ernie");
		secure.setSecurementPasswordType(WSConstants.PW_DIGEST);
		secure.setBspCompliant(false);
		secure.afterPropertiesSet();

		SoapMessage secured = loadEmptySoap();
		MessageContext secureContext = new DefaultMessageContext(secured, this.messageFactory);
		secure.handleRequest(secureContext);

		SoapMessage first = cloneSoapMessage((SaajSoapMessage) secured);
		SoapMessage second = cloneSoapMessage((SaajSoapMessage) secured);

		Wss4jSecurityInterceptor validate = new Wss4jSecurityInterceptor();
		validate.setValidationActions("UsernameToken");
		validate.setValidationCallbackHandler(this.callbackHandler);
		validate.setSecurementPasswordType(WSConstants.PW_DIGEST);
		validate.setValidationReplayCache(new ConcurrentMapReplayCache());
		validate.setRemoveSecurityHeader(false);
		validate.setBspCompliant(false);
		validate.afterPropertiesSet();

		validate.validateMessage(first, new DefaultMessageContext(first, this.messageFactory));
		assertThatThrownBy(
				() -> validate.validateMessage(second, new DefaultMessageContext(second, this.messageFactory)))
			.isInstanceOf(WsSecurityValidationException.class)
			.hasMessageContaining("replay");
	}

	private SoapMessage loadEmptySoap() throws Exception {
		ClassPathResource resource = new ClassPathResource("empty-soap.xml",
				Wss4jMessageInterceptorUsernameTokenTests.class);
		return this.messageFactory.createWebServiceMessage(resource.getInputStream());
	}

	private SoapMessage cloneSoapMessage(SaajSoapMessage original) throws Exception {
		Document doc = original.getSaajMessage().getSOAPPart();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		transformer.transform(new DOMSource(doc), new StreamResult(bos));
		return this.messageFactory.createWebServiceMessage(new ByteArrayInputStream(bos.toByteArray()));
	}

}
