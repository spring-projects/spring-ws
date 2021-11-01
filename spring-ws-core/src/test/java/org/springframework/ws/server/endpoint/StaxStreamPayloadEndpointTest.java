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

package org.springframework.ws.server.endpoint;

import static org.assertj.core.api.Assertions.*;

import jakarta.xml.soap.MessageFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;

import org.junit.jupiter.api.Test;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.xmlunit.assertj.XmlAssert;

/**
 * Test case for AbstractStaxStreamPayloadEndpoint.
 *
 * @see AbstractStaxStreamPayloadEndpoint
 */
@SuppressWarnings("Since15")
public class StaxStreamPayloadEndpointTest extends AbstractMessageEndpointTestCase {

	@Override
	protected MessageEndpoint createNoResponseEndpoint() {

		return new AbstractStaxStreamPayloadEndpoint() {
			@Override
			protected void invokeInternal(XMLStreamReader streamReader, XMLStreamWriter streamWriter) throws Exception {
				assertThat(streamReader).isNotNull();
			}
		};
	}

	@Override
	protected MessageEndpoint createNoRequestPayloadEndpoint() {

		return new AbstractStaxStreamPayloadEndpoint() {
			@Override
			protected void invokeInternal(XMLStreamReader streamReader, XMLStreamWriter streamWriter) throws Exception {
				assertThat(streamReader).isNull();
			}
		};
	}

	@Override
	protected MessageEndpoint createResponseEndpoint() {

		return new AbstractStaxStreamPayloadEndpoint() {
			@Override
			protected void invokeInternal(XMLStreamReader streamReader, XMLStreamWriter streamWriter) throws Exception {

				assertThat(streamReader).isNotNull();
				assertThat(streamWriter).isNotNull();
				assertThat(streamReader.next()).isEqualTo(XMLStreamConstants.START_ELEMENT);
				assertThat(streamReader.getLocalName()).isEqualTo(REQUEST_ELEMENT);
				assertThat(streamReader.getNamespaceURI()).isEqualTo(NAMESPACE_URI);
				assertThat(streamReader.hasNext()).isTrue();
				assertThat(streamReader.next()).isEqualTo(XMLStreamConstants.END_ELEMENT);
				assertThat(streamReader.getLocalName()).isEqualTo(REQUEST_ELEMENT);
				assertThat(streamReader.getNamespaceURI()).isEqualTo(NAMESPACE_URI);

				streamWriter.setDefaultNamespace(NAMESPACE_URI);
				streamWriter.writeStartElement(NAMESPACE_URI, RESPONSE_ELEMENT);
				streamWriter.writeDefaultNamespace(NAMESPACE_URI);
				streamWriter.writeEndElement();
				streamWriter.flush();
				streamWriter.close();
			}

			@Override
			protected XMLOutputFactory createXmlOutputFactory() {

				XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
				outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);

				return outputFactory;
			}
		};
	}

	@Test
	public void testSaajResponse() throws Exception {

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		MessageFactory messageFactory = MessageFactory.newInstance();
		SaajSoapMessage request = new SaajSoapMessage(messageFactory.createMessage());
		transformer.transform(new StringSource(REQUEST), request.getPayloadResult());
		SaajSoapMessageFactory soapMessageFactory = new SaajSoapMessageFactory();
		soapMessageFactory.afterPropertiesSet();
		MessageContext context = new DefaultMessageContext(request, soapMessageFactory);

		MessageEndpoint endpoint = createResponseEndpoint();
		endpoint.invoke(context);

		assertThat(context.hasResponse()).isTrue();

		StringResult stringResult = new StringResult();
		transformer.transform(context.getResponse().getPayloadSource(), stringResult);

		XmlAssert.assertThat(stringResult.toString()).and(RESPONSE).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testAxiomResponse() throws Exception {

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		MessageFactory messageFactory = MessageFactory.newInstance();
		SaajSoapMessage request = new SaajSoapMessage(messageFactory.createMessage(), true, messageFactory);
		transformer.transform(new StringSource(REQUEST), request.getPayloadResult());
		SaajSoapMessageFactory soapMessageFactory = new SaajSoapMessageFactory();
		soapMessageFactory.afterPropertiesSet();
		MessageContext context = new DefaultMessageContext(request, soapMessageFactory);

		MessageEndpoint endpoint = createResponseEndpoint();
		endpoint.invoke(context);

		assertThat(context.hasResponse()).isTrue();

		StringResult stringResult = new StringResult();
		transformer.transform(context.getResponse().getPayloadSource(), stringResult);

		XmlAssert.assertThat(stringResult.toString()).and(RESPONSE).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testAxiomNoResponse() throws Exception {

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		MessageFactory messageFactory = MessageFactory.newInstance();
		SaajSoapMessage request = new SaajSoapMessage(messageFactory.createMessage(), true, messageFactory);
		transformer.transform(new StringSource(REQUEST), request.getPayloadResult());
		SaajSoapMessageFactory soapMessageFactory = new SaajSoapMessageFactory();
		soapMessageFactory.afterPropertiesSet();
		MessageContext context = new DefaultMessageContext(request, soapMessageFactory);

		MessageEndpoint endpoint = createNoResponseEndpoint();
		endpoint.invoke(context);

		assertThat(context.hasResponse()).isFalse();
	}

}
