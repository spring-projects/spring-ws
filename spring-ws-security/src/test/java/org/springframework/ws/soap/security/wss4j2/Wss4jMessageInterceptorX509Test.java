/*
 * Copyright 2005-2025 the original author or authors.
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

import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.apache.wss4j.dom.validate.Validator;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public abstract class Wss4jMessageInterceptorX509Test extends Wss4jTest {

	protected Wss4jSecurityInterceptor interceptor;

	@Override
	protected void onSetup() throws Exception {

		this.interceptor = new Wss4jSecurityInterceptor();
		this.interceptor.setSecurementActions("Signature");
		this.interceptor.setValidationActions("Signature");
		CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
		cryptoFactoryBean.setCryptoProvider(Merlin.class);
		cryptoFactoryBean.setKeyStoreType("jceks");
		cryptoFactoryBean.setKeyStorePassword("123456");
		cryptoFactoryBean.setKeyStoreLocation(new ClassPathResource("private.jks"));

		cryptoFactoryBean.afterPropertiesSet();
		this.interceptor.setSecurementSignatureCrypto(cryptoFactoryBean.getObject());
		this.interceptor.setValidationSignatureCrypto(cryptoFactoryBean.getObject());
		this.interceptor.afterPropertiesSet();
	}

	@Test
	public void testAddCertificate() throws Exception {

		this.interceptor.setSecurementPassword("123456");
		this.interceptor.setSecurementUsername("rsaKey");
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext messageContext = getSoap11MessageContext(message);

		this.interceptor.setSecurementSignatureKeyIdentifier("DirectReference");

		this.interceptor.secureMessage(message, messageContext);
		Document document = getDocument(message);

		assertXpathExists("Absent BinarySecurityToken element",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", document);

		// lets verify the signature that we've just generated
		this.interceptor.validateMessage(message, messageContext);
	}

	@Test
	void validateSignatureWithWssConfig() throws Exception {
		this.interceptor.setSecurementPassword("123456");
		this.interceptor.setSecurementUsername("rsaKey");
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext messageContext = getSoap11MessageContext(message);

		this.interceptor.setSecurementSignatureKeyIdentifier("DirectReference");

		this.interceptor.secureMessage(message, messageContext);
		Document document = getDocument(message);
		assertXpathExists("Absent BinarySecurityToken element",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", document);
		WSSConfig wssConfig = WSSConfig.getNewInstance();
		Validator validator = mock(Validator.class);
		wssConfig.setValidator(WSConstants.SIGNATURE, validator);
		this.interceptor.setWssConfig(wssConfig);
		this.interceptor.validateMessage(message, messageContext);
		verify(validator, times(2)).validate(any(), any()); // Also SignatureProcessor
	}

}
