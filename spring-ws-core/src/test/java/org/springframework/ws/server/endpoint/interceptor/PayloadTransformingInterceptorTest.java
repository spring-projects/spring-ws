/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.server.endpoint.interceptor;

import static org.assertj.core.api.Assertions.*;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPMessage;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.xmlunit.assertj.XmlAssert;

public class PayloadTransformingInterceptorTest {

	private PayloadTransformingInterceptor interceptor;

	private Transformer transformer;

	private Resource input;

	private Resource output;

	private Resource xslt;

	@BeforeEach
	public void setUp() throws Exception {

		interceptor = new PayloadTransformingInterceptor();
		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		transformer = transformerFactory.newTransformer();
		input = new ClassPathResource("transformInput.xml", getClass());
		output = new ClassPathResource("transformOutput.xml", getClass());
		xslt = new ClassPathResource("transformation.xslt", getClass());
	}

	@Test
	public void testHandleRequest() throws Exception {

		interceptor.setRequestXslt(xslt);
		interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(input);
		MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		boolean result = interceptor.handleRequest(context, null);

		assertThat(result).isTrue();

		StringResult expected = new StringResult();
		transformer.transform(new SAXSource(SaxUtils.createInputSource(output)), expected);

		XmlAssert.assertThat(request.getPayloadAsString()).and(expected.toString()).ignoreWhitespace().areSimilar();
	}

	@Test
	public void testHandleRequestNoXslt() throws Exception {

		interceptor.setResponseXslt(xslt);
		interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(input);
		MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		boolean result = interceptor.handleRequest(context, null);

		assertThat(result).isTrue();

		StringResult expected = new StringResult();
		transformer.transform(new SAXSource(SaxUtils.createInputSource(input)), expected);
		XmlAssert.assertThat(request.getPayloadAsString()).and(expected.toString()).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testHandleResponse() throws Exception {

		interceptor.setResponseXslt(xslt);
		interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(input);
		MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
		response.setPayload(input);

		boolean result = interceptor.handleResponse(context, null);

		assertThat(result).isTrue();

		StringResult expected = new StringResult();
		transformer.transform(new SAXSource(SaxUtils.createInputSource(output)), expected);

		XmlAssert.assertThat(response.getPayloadAsString()).and(expected.toString()).ignoreWhitespace().areSimilar();
	}

	@Test
	public void testHandleResponseNoXslt() throws Exception {

		interceptor.setRequestXslt(xslt);
		interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(input);
		MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
		response.setPayload(input);

		boolean result = interceptor.handleResponse(context, null);

		assertThat(result).isTrue();

		StringResult expected = new StringResult();
		transformer.transform(new SAXSource(SaxUtils.createInputSource(input)), expected);

		XmlAssert.assertThat(response.getPayloadAsString()).and(expected.toString()).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testSaaj() throws Exception {

		interceptor.setRequestXslt(xslt);
		interceptor.afterPropertiesSet();
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage saajMessage = messageFactory.createMessage();
		SaajSoapMessage message = new SaajSoapMessage(saajMessage);
		transformer.transform(new ResourceSource(input), message.getPayloadResult());
		MessageContext context = new DefaultMessageContext(message, new SaajSoapMessageFactory(messageFactory));

		assertThat(interceptor.handleRequest(context, null)).isTrue();

		StringResult expected = new StringResult();
		transformer.transform(new SAXSource(SaxUtils.createInputSource(output)), expected);
		StringResult result = new StringResult();
		transformer.transform(message.getPayloadSource(), result);

		XmlAssert.assertThat(result.toString()).and(expected.toString()).ignoreWhitespace().areSimilar();
	}

	@Test
	public void testPox() throws Exception {

		interceptor.setRequestXslt(xslt);
		interceptor.afterPropertiesSet();
		DomPoxMessageFactory factory = new DomPoxMessageFactory();
		DomPoxMessage message = factory.createWebServiceMessage();
		transformer.transform(new ResourceSource(input), message.getPayloadResult());
		MessageContext context = new DefaultMessageContext(message, factory);

		assertThat(interceptor.handleRequest(context, null)).isTrue();

		StringResult expected = new StringResult();
		transformer.transform(new SAXSource(SaxUtils.createInputSource(output)), expected);
		StringResult result = new StringResult();
		transformer.transform(message.getPayloadSource(), result);

		XmlAssert.assertThat(result.toString()).and(expected.toString()).ignoreWhitespace().areSimilar();

	}

	@Test
	public void testNoStylesheetsSet() {
		assertThatIllegalArgumentException().isThrownBy(() -> interceptor.afterPropertiesSet());
	}
}
