/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.soap11;

import static org.assertj.core.api.Assertions.*;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.AbstractSoapMessageFactoryTestCase;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.MockTransportInputStream;
import org.springframework.ws.transport.TransportInputStream;

public abstract class AbstractSoap11MessageFactoryTestCase extends AbstractSoapMessageFactoryTestCase {

	@Test
	public void testCreateEmptySoap11Message() {

		WebServiceMessage message = messageFactory.createWebServiceMessage();

		assertThat(message).isInstanceOf(SoapMessage.class);

		SoapMessage soapMessage = (SoapMessage) message;

		assertThat(soapMessage.getVersion()).isEqualTo(SoapVersion.SOAP_11);
	}

	@Override
	public void testCreateSoapMessageNoAttachment() throws Exception {

		InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11.xml");
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/xml");
		String soapAction = "\"http://springframework.org/spring-ws/Action\"";
		headers.put("SOAPAction", soapAction);
		TransportInputStream tis = new MockTransportInputStream(is, headers);

		WebServiceMessage message = messageFactory.createWebServiceMessage(tis);

		assertThat(message).isInstanceOf(SoapMessage.class);

		SoapMessage soapMessage = (SoapMessage) message;

		assertThat(soapMessage.getVersion()).isEqualTo(SoapVersion.SOAP_11);
		assertThat(soapMessage.getSoapAction()).isEqualTo(soapAction);
		assertThat(soapMessage.isXopPackage()).isFalse();
	}

	@Override
	public void doTestCreateSoapMessageIllFormedXml() throws Exception {

		InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-ill-formed.xml");
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/xml");
		TransportInputStream tis = new MockTransportInputStream(is, headers);

		messageFactory.createWebServiceMessage(tis);
	}

	@Override
	public void testCreateSoapMessageSwA() throws Exception {

		InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-attachment.bin");
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type",
				"multipart/related;" + "type=\"text/xml\";" + "boundary=\"----=_Part_0_11416420.1149699787554\"");
		TransportInputStream tis = new MockTransportInputStream(is, headers);

		WebServiceMessage message = messageFactory.createWebServiceMessage(tis);

		assertThat(message).isInstanceOf(SoapMessage.class);

		SoapMessage soapMessage = (SoapMessage) message;

		assertThat(soapMessage.getVersion()).isEqualTo(SoapVersion.SOAP_11);
		assertThat(soapMessage.isXopPackage()).isFalse();

		Iterator<Attachment> iter = soapMessage.getAttachments();

		assertThat(iter.hasNext()).isTrue();

		Attachment attachment = soapMessage.getAttachment("interface21");

		assertThat(attachment).isNotNull();
		assertThat(attachment.getContentId()).isEqualTo("interface21");
	}

	@Override
	public void testCreateSoapMessageMtom() throws Exception {

		InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-mtom.bin");
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type",
				"multipart/related;" + "start-info=\"text/xml\";" + "type=\"application/xop+xml\";"
						+ "start=\"<0.urn:uuid:492264AB42E57108E01176731445508@apache.org>\";"
						+ "boundary=\"MIMEBoundaryurn_uuid_492264AB42E57108E01176731445507\"");
		TransportInputStream tis = new MockTransportInputStream(is, headers);

		WebServiceMessage message = messageFactory.createWebServiceMessage(tis);

		assertThat(message).isInstanceOf(SoapMessage.class);

		SoapMessage soapMessage = (SoapMessage) message;

		assertThat(soapMessage.getVersion()).isEqualTo(SoapVersion.SOAP_11);
		assertThat(soapMessage.isXopPackage()).isTrue();

		Iterator<Attachment> iter = soapMessage.getAttachments();

		assertThat(iter.hasNext()).isTrue();

		Attachment attachment = soapMessage.getAttachment("<1.urn:uuid:492264AB42E57108E01176731445504@apache.org>");

		assertThat(attachment).isNotNull();
	}

	@Test
	public void testCreateSoapMessageMtomWeirdStartInfo() throws Exception {

		InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-mtom.bin");
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type",
				"multipart/related;" + "startinfo=\"text/xml\";" + "type=\"application/xop+xml\";"
						+ "start=\"<0.urn:uuid:492264AB42E57108E01176731445508@apache.org>\";"
						+ "boundary=\"MIMEBoundaryurn_uuid_492264AB42E57108E01176731445507\"");
		TransportInputStream tis = new MockTransportInputStream(is, headers);

		WebServiceMessage message = messageFactory.createWebServiceMessage(tis);
		assertThat(message).isInstanceOf(SoapMessage.class);
		SoapMessage soapMessage = (SoapMessage) message;
		assertThat(soapMessage.getVersion()).isEqualTo(SoapVersion.SOAP_11);
		assertThat(soapMessage.isXopPackage()).isTrue();
		Iterator<Attachment> iter = soapMessage.getAttachments();

		assertThat(iter.hasNext()).isTrue();

		Attachment attachment = soapMessage.getAttachment("<1.urn:uuid:492264AB42E57108E01176731445504@apache.org>");

		assertThat(attachment).isNotNull();
	}

	@Test
	public void testCreateSoapMessageUtf8ByteOrderMark() throws Exception {

		InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-utf8-bom.xml");
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "text/xml; charset=UTF-8");
		TransportInputStream tis = new MockTransportInputStream(is, headers);

		SoapMessage message = (SoapMessage) messageFactory.createWebServiceMessage(tis);

		assertThat(message.getVersion()).isEqualTo(SoapVersion.SOAP_11);
	}

	@Test
	public void testCreateSoapMessageUtf16BigEndianByteOrderMark() throws Exception {

		InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-utf16-be-bom.xml");
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/xml; charset=UTF-16");
		TransportInputStream tis = new MockTransportInputStream(is, headers);

		SoapMessage message = (SoapMessage) messageFactory.createWebServiceMessage(tis);

		assertThat(message.getVersion()).isEqualTo(SoapVersion.SOAP_11);
	}

	@Test
	public void testCreateSoapMessageUtf16LittleEndianByteOrderMark() throws Exception {

		InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-utf16-le-bom.xml");
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/xml; charset=UTF-16");
		TransportInputStream tis = new MockTransportInputStream(is, headers);

		SoapMessage message = (SoapMessage) messageFactory.createWebServiceMessage(tis);

		assertThat(message.getVersion()).isEqualTo(SoapVersion.SOAP_11);
	}

	@Override
	public void testCreateSoapMessageMissingContentType() throws Exception {

		InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11.xml");
		TransportInputStream tis = new MockTransportInputStream(is, Collections.emptyMap());

		WebServiceMessage message = messageFactory.createWebServiceMessage(tis);

		assertThat(message).isInstanceOf(SoapMessage.class);

		SoapMessage soapMessage = (SoapMessage) message;

		assertThat(soapMessage.getVersion()).isEqualTo(SoapVersion.SOAP_11);
	}
}
