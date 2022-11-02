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

package org.springframework.ws.soap.security.xwss;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class AbstractXwssMessageInterceptorTestCase {

	protected XwsSecurityInterceptor interceptor;

	private MessageFactory messageFactory;

	private Map<String, String> namespaces;

	@BeforeEach
	public final void setUp() throws Exception {

		interceptor = new XwsSecurityInterceptor();
		messageFactory = MessageFactory.newInstance();
		namespaces = new HashMap<String, String>(4);
		namespaces.put("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
		namespaces.put("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		namespaces.put("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		namespaces.put("ds", "http://www.w3.org/2000/09/xmldsig#");
		namespaces.put("xenc", "http://www.w3.org/2001/04/xmlenc#");
		onSetup();
	}

	protected void assertXpathEvaluatesTo(String expectedValue, String xpathExpression, SOAPMessage soapMessage) {

		XPathExpression expression = XPathExpressionFactory.createXPathExpression(xpathExpression, namespaces);
		Document document = soapMessage.getSOAPPart();
		String actualValue = expression.evaluateAsString(document);

		assertThat(actualValue).isEqualTo(expectedValue);
	}

	protected void assertXpathExists(String xpathExpression, SOAPMessage soapMessage) {

		XPathExpression expression = XPathExpressionFactory.createXPathExpression(xpathExpression, namespaces);
		Document document = soapMessage.getSOAPPart();
		Node node = expression.evaluateAsNode(document);

		assertThat(node).isNotNull();
	}

	protected void assertXpathNotExists(String xpathExpression, SOAPMessage soapMessage) {

		XPathExpression expression = XPathExpressionFactory.createXPathExpression(xpathExpression, namespaces);
		Document document = soapMessage.getSOAPPart();
		Node node = expression.evaluateAsNode(document);

		assertThat(node).isNull();
	}

	protected SaajSoapMessage loadSaajMessage(String fileName) throws SOAPException, IOException {

		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", "text/xml");
		Resource resource = new ClassPathResource(fileName, getClass());

		assertThat(resource.exists()).isTrue();

		try (InputStream is = resource.getInputStream()) {
			return new SaajSoapMessage(messageFactory.createMessage(mimeHeaders, is));
		}
	}

	protected void onSetup() throws Exception {}
}
