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

package org.springframework.ws.soap.saaj;

import static org.assertj.core.api.Assertions.*;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap12.AbstractSoap12MessageTestCase;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.xmlunit.assertj.XmlAssert;

public class SaajSoap12MessageTest extends AbstractSoap12MessageTestCase {

	@Override
	protected String getNS() {
		return "env";
	}

	@Override
	protected String getHeader() {
		return "";
	}

	private SOAPMessage saajMessage;

	@Override
	protected SoapMessage createSoapMessage() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		saajMessage = messageFactory.createMessage();
		saajMessage.getSOAPHeader().detachNode();
		return new SaajSoapMessage(saajMessage, true, messageFactory);
	}

	public void testGetPayloadSource() throws Exception {

		saajMessage.getSOAPBody().addChildElement("child");
		Source source = soapMessage.getPayloadSource();
		StringResult result = new StringResult();
		transformer.transform(source, result);

		XmlAssert.assertThat(result.toString()).and("<child/>").ignoreWhitespace().areIdentical();
	}

	public void testGetPayloadSourceText() throws Exception {

		saajMessage.getSOAPBody().addTextNode(" ");
		saajMessage.getSOAPBody().addChildElement("child");
		Source source = soapMessage.getPayloadSource();
		StringResult result = new StringResult();
		transformer.transform(source, result);

		XmlAssert.assertThat(result.toString()).and("<child/>").ignoreWhitespace().areIdentical();
	}

	public void testGetPayloadResult() throws Exception {

		StringSource source = new StringSource("<child/>");
		Result result = soapMessage.getPayloadResult();
		transformer.transform(source, result);

		assertThat(saajMessage.getSOAPBody().hasChildNodes()).isTrue();
		assertThat(saajMessage.getSOAPBody().getFirstChild().getLocalName()).isEqualTo("child");
	}

}
