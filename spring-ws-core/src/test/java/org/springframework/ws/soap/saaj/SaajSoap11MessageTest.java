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

package org.springframework.ws.soap.saaj;

import java.util.Iterator;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPBodyElement;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageTest;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;

public class SaajSoap11MessageTest extends AbstractSoap11MessageTest {

	private SOAPMessage saajMessage;

	@Override
	protected String getNS() {
		return "SOAP-ENV";
	}

	@Override
	protected String getHeader() {
		return "";
	}

	@Override
	protected final SoapMessage createSoapMessage() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		saajMessage = messageFactory.createMessage();
		saajMessage.getSOAPHeader().detachNode();

		return new SaajSoapMessage(saajMessage, true, messageFactory);
	}

	@Test
	public void testGetPayloadSource() throws Exception {

		saajMessage.getSOAPPart().getEnvelope().getBody().addChildElement("child");
		Source source = soapMessage.getPayloadSource();
		StringResult result = new StringResult();
		transformer.transform(source, result);

		XmlAssert.assertThat(result.toString()).and("<child/>").ignoreWhitespace().areIdentical();
	}

	@Test
	public void testGetPayloadSourceText() throws Exception {

		SOAPBody body = saajMessage.getSOAPPart().getEnvelope().getBody();
		body.addTextNode(" ");
		body.addChildElement("child");
		Source source = soapMessage.getPayloadSource();
		StringResult result = new StringResult();
		transformer.transform(source, result);

		XmlAssert.assertThat(result.toString()).and("<child/>").ignoreWhitespace().areIdentical();
	}

	@Test
	public void testGetPayloadResult() throws Exception {

		StringSource source = new StringSource("<child/>");
		Result result = soapMessage.getPayloadResult();
		transformer.transform(source, result);
		SOAPBody body = saajMessage.getSOAPPart().getEnvelope().getBody();
		Iterator<?> iterator = body.getChildElements();

		assertThat(iterator.hasNext()).isTrue();

		SOAPBodyElement bodyElement = (SOAPBodyElement) iterator.next();

		assertThat(bodyElement.getElementName().getLocalName()).isEqualTo("child");
	}

}
