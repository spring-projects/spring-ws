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

import javax.xml.soap.MessageFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.junit.Test;

import static org.xmlunit.assertj.XmlAssert.assertThat;
import static org.junit.Assert.*;

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
				assertNotNull("No StreamReader passed", streamReader);
			}
		};
	}

	@Override
	protected MessageEndpoint createNoRequestPayloadEndpoint() {
		return new AbstractStaxStreamPayloadEndpoint() {
			@Override
			protected void invokeInternal(XMLStreamReader streamReader, XMLStreamWriter streamWriter) throws Exception {
				assertNull("StreamReader passed", streamReader);
			}
		};
	}

	@Override
	protected MessageEndpoint createResponseEndpoint() {
		return new AbstractStaxStreamPayloadEndpoint() {
			@Override
			protected void invokeInternal(XMLStreamReader streamReader, XMLStreamWriter streamWriter) throws Exception {
				assertNotNull("No streamReader passed", streamReader);
				assertNotNull("No streamWriter passed", streamReader);
				assertEquals("Not a start element", XMLStreamConstants.START_ELEMENT, streamReader.next());
				assertEquals("Invalid start event local name", REQUEST_ELEMENT, streamReader.getLocalName());
				assertEquals("Invalid start event namespace", NAMESPACE_URI, streamReader.getNamespaceURI());
				assertTrue("streamReader has no next element", streamReader.hasNext());
				assertEquals("Not a end element", XMLStreamConstants.END_ELEMENT, streamReader.next());
				assertEquals("Invalid end event local name", REQUEST_ELEMENT, streamReader.getLocalName());
				assertEquals("Invalid end event namespace", NAMESPACE_URI, streamReader.getNamespaceURI());
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
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		MessageFactory messageFactory = MessageFactory.newInstance();
		SaajSoapMessage request = new SaajSoapMessage(messageFactory.createMessage());
		transformer.transform(new StringSource(REQUEST), request.getPayloadResult());
		SaajSoapMessageFactory soapMessageFactory = new SaajSoapMessageFactory();
		soapMessageFactory.afterPropertiesSet();
		MessageContext context = new DefaultMessageContext(request, soapMessageFactory);

		MessageEndpoint endpoint = createResponseEndpoint();
		endpoint.invoke(context);
		assertTrue("context has not response", context.hasResponse());
		StringResult stringResult = new StringResult();
		transformer.transform(context.getResponse().getPayloadSource(), stringResult);
		assertThat(stringResult.toString()).and(RESPONSE).areSimilar();
	}

	@Test
	public void testAxiomResponse() throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
		AxiomSoapMessage request = new AxiomSoapMessage(axiomFactory);
		transformer.transform(new StringSource(REQUEST), request.getPayloadResult());
		AxiomSoapMessageFactory soapMessageFactory = new AxiomSoapMessageFactory();
		soapMessageFactory.afterPropertiesSet();
		MessageContext context = new DefaultMessageContext(request, soapMessageFactory);

		MessageEndpoint endpoint = createResponseEndpoint();
		endpoint.invoke(context);
		assertTrue("context has not response", context.hasResponse());
		StringResult stringResult = new StringResult();
		transformer.transform(context.getResponse().getPayloadSource(), stringResult);
		assertThat(stringResult.toString()).and(RESPONSE).areSimilar();
	}

	@Test
	public void testAxiomNoResponse() throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
		AxiomSoapMessage request = new AxiomSoapMessage(axiomFactory);
		transformer.transform(new StringSource(REQUEST), request.getPayloadResult());
		AxiomSoapMessageFactory soapMessageFactory = new AxiomSoapMessageFactory();
		soapMessageFactory.afterPropertiesSet();
		MessageContext context = new DefaultMessageContext(request, soapMessageFactory);

		MessageEndpoint endpoint = createNoResponseEndpoint();
		endpoint.invoke(context);
		assertFalse("context has response", context.hasResponse());
	}

	@Test
	public void testAxiomResponseNoPayloadCaching() throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
		AxiomSoapMessage request = new AxiomSoapMessage(axiomFactory);
		transformer.transform(new StringSource(REQUEST), request.getPayloadResult());
		AxiomSoapMessageFactory soapMessageFactory = new AxiomSoapMessageFactory();
		soapMessageFactory.setPayloadCaching(false);
		soapMessageFactory.afterPropertiesSet();
		MessageContext context = new DefaultMessageContext(request, soapMessageFactory);

		MessageEndpoint endpoint = createResponseEndpoint();
		endpoint.invoke(context);
		assertTrue("context has not response", context.hasResponse());

		StringResult stringResult = new StringResult();
		transformer.transform(context.getResponse().getPayloadSource(), stringResult);
		assertThat(stringResult.toString()).and(RESPONSE).areSimilar();
	}

	@Test
	public void testAxiomNoResponseNoPayloadCaching() throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
		AxiomSoapMessage request = new AxiomSoapMessage(axiomFactory);
		transformer.transform(new StringSource(REQUEST), request.getPayloadResult());
		AxiomSoapMessageFactory soapMessageFactory = new AxiomSoapMessageFactory();
		soapMessageFactory.setPayloadCaching(false);
		soapMessageFactory.afterPropertiesSet();
		MessageContext context = new DefaultMessageContext(request, soapMessageFactory);

		MessageEndpoint endpoint = createNoResponseEndpoint();
		endpoint.invoke(context);
		assertFalse("context has response", context.hasResponse());
	}


}