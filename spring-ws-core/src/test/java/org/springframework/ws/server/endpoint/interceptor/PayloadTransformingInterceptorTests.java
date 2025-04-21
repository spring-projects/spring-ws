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

package org.springframework.ws.server.endpoint.interceptor;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.pox.dom.DomPoxMessage;
import org.springframework.ws.pox.dom.DomPoxMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PayloadTransformingInterceptorTests {

	private PayloadTransformingInterceptor interceptor;

	private Transformer transformer;

	private Resource input;

	private Resource output;

	private Resource xslt;

	@BeforeEach
	void setUp() throws Exception {

		this.interceptor = new PayloadTransformingInterceptor();
		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		this.transformer = transformerFactory.newTransformer();
		this.input = new ClassPathResource("transformInput.xml", getClass());
		this.output = new ClassPathResource("transformOutput.xml", getClass());
		this.xslt = new ClassPathResource("transformation.xslt", getClass());
	}

	@Test
	void testHandleRequest() throws Exception {

		this.interceptor.setRequestXslt(this.xslt);
		this.interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(this.input);
		MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		boolean result = this.interceptor.handleRequest(context, null);

		assertThat(result).isTrue();

		StringResult expected = new StringResult();
		this.transformer.transform(new SAXSource(SaxUtils.createInputSource(this.output)), expected);

		XmlAssert.assertThat(request.getPayloadAsString()).and(expected.toString()).ignoreWhitespace().areSimilar();
	}

	@Test
	void testHandleRequestNoXslt() throws Exception {

		this.interceptor.setResponseXslt(this.xslt);
		this.interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(this.input);
		MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		boolean result = this.interceptor.handleRequest(context, null);

		assertThat(result).isTrue();

		StringResult expected = new StringResult();
		this.transformer.transform(new SAXSource(SaxUtils.createInputSource(this.input)), expected);
		XmlAssert.assertThat(request.getPayloadAsString()).and(expected.toString()).ignoreWhitespace().areIdentical();
	}

	@Test
	void testHandleResponse() throws Exception {

		this.interceptor.setResponseXslt(this.xslt);
		this.interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(this.input);
		MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
		response.setPayload(this.input);

		boolean result = this.interceptor.handleResponse(context, null);

		assertThat(result).isTrue();

		StringResult expected = new StringResult();
		this.transformer.transform(new SAXSource(SaxUtils.createInputSource(this.output)), expected);

		XmlAssert.assertThat(response.getPayloadAsString()).and(expected.toString()).ignoreWhitespace().areSimilar();
	}

	@Test
	void testHandleResponseNoXslt() throws Exception {

		this.interceptor.setRequestXslt(this.xslt);
		this.interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(this.input);
		MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
		response.setPayload(this.input);

		boolean result = this.interceptor.handleResponse(context, null);

		assertThat(result).isTrue();

		StringResult expected = new StringResult();
		this.transformer.transform(new SAXSource(SaxUtils.createInputSource(this.input)), expected);

		XmlAssert.assertThat(response.getPayloadAsString()).and(expected.toString()).ignoreWhitespace().areIdentical();
	}

	@Test
	void testSaaj() throws Exception {

		this.interceptor.setRequestXslt(this.xslt);
		this.interceptor.afterPropertiesSet();
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage saajMessage = messageFactory.createMessage();
		SaajSoapMessage message = new SaajSoapMessage(saajMessage);
		this.transformer.transform(new ResourceSource(this.input), message.getPayloadResult());
		MessageContext context = new DefaultMessageContext(message, new SaajSoapMessageFactory(messageFactory));

		assertThat(this.interceptor.handleRequest(context, null)).isTrue();

		StringResult expected = new StringResult();
		this.transformer.transform(new SAXSource(SaxUtils.createInputSource(this.output)), expected);
		StringResult result = new StringResult();
		this.transformer.transform(message.getPayloadSource(), result);

		XmlAssert.assertThat(result.toString()).and(expected.toString()).ignoreWhitespace().areSimilar();
	}

	@Test
	void testPox() throws Exception {

		this.interceptor.setRequestXslt(this.xslt);
		this.interceptor.afterPropertiesSet();
		DomPoxMessageFactory factory = new DomPoxMessageFactory();
		DomPoxMessage message = factory.createWebServiceMessage();
		this.transformer.transform(new ResourceSource(this.input), message.getPayloadResult());
		MessageContext context = new DefaultMessageContext(message, factory);

		assertThat(this.interceptor.handleRequest(context, null)).isTrue();

		StringResult expected = new StringResult();
		this.transformer.transform(new SAXSource(SaxUtils.createInputSource(this.output)), expected);
		StringResult result = new StringResult();
		this.transformer.transform(message.getPayloadSource(), result);

		XmlAssert.assertThat(result.toString()).and(expected.toString()).ignoreWhitespace().areSimilar();

	}

	@Test
	void testNoStylesheetsSet() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.interceptor.afterPropertiesSet());
	}

}
