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

package org.springframework.ws.server.endpoint.adapter.method;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arjen Poutsma
 */
@SuppressWarnings("Since15")
class StaxPayloadMethodArgumentResolverTests extends AbstractMethodArgumentResolverTests {

	private StaxPayloadMethodArgumentResolver resolver;

	private MethodParameter streamParameter;

	private MethodParameter eventParameter;

	private MethodParameter invalidParameter;

	@BeforeEach
	void setUp() throws Exception {

		this.resolver = new StaxPayloadMethodArgumentResolver();
		this.streamParameter = new MethodParameter(getClass().getMethod("streamReader", XMLStreamReader.class), 0);
		this.eventParameter = new MethodParameter(getClass().getMethod("eventReader", XMLEventReader.class), 0);
		this.invalidParameter = new MethodParameter(getClass().getMethod("invalid", XMLStreamReader.class), 0);
	}

	@Test
	void supportsParameter() {

		assertThat(this.resolver.supportsParameter(this.streamParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.eventParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.invalidParameter)).isFalse();
	}

	@Test
	void resolveStreamReaderSaaj() throws Exception {

		MessageContext messageContext = createSaajMessageContext();

		Object result = this.resolver.resolveArgument(messageContext, this.streamParameter);

		testStreamReader(result);
	}

	@Test
	void resolveStreamReaderAxiomCaching() throws Exception {

		MessageContext messageContext = createCachingAxiomMessageContext();

		Object result = this.resolver.resolveArgument(messageContext, this.streamParameter);

		testStreamReader(result);
	}

	@Test
	void resolveStreamReaderAxiomNonCaching() throws Exception {

		MessageContext messageContext = createNonCachingAxiomMessageContext();

		Object result = this.resolver.resolveArgument(messageContext, this.streamParameter);

		testStreamReader(result);
	}

	@Test
	void resolveStreamReaderStream() throws Exception {

		MessageContext messageContext = createMockMessageContext();

		Object result = this.resolver.resolveArgument(messageContext, this.streamParameter);

		testStreamReader(result);
	}

	@Test
	void resolveEventReaderSaaj() throws Exception {

		MessageContext messageContext = createSaajMessageContext();

		Object result = this.resolver.resolveArgument(messageContext, this.eventParameter);

		testEventReader(result);
	}

	@Test
	void resolveEventReaderAxiomCaching() throws Exception {

		MessageContext messageContext = createCachingAxiomMessageContext();

		Object result = this.resolver.resolveArgument(messageContext, this.eventParameter);

		testEventReader(result);
	}

	@Test
	void resolveEventReaderAxiomNonCaching() throws Exception {

		MessageContext messageContext = createNonCachingAxiomMessageContext();

		Object result = this.resolver.resolveArgument(messageContext, this.eventParameter);

		testEventReader(result);
	}

	@Test
	void resolveEventReaderStream() throws Exception {

		MessageContext messageContext = createMockMessageContext();

		Object result = this.resolver.resolveArgument(messageContext, this.eventParameter);

		testEventReader(result);
	}

	private void testStreamReader(Object result) throws XMLStreamException {

		assertThat(result).isInstanceOf(XMLStreamReader.class);

		XMLStreamReader streamReader = (XMLStreamReader) result;

		assertThat(streamReader.hasNext()).isTrue();
		assertThat(streamReader.nextTag()).isEqualTo(XMLStreamConstants.START_ELEMENT);
		assertThat(streamReader.getNamespaceURI()).isEqualTo(NAMESPACE_URI);
		assertThat(streamReader.getLocalName()).isEqualTo(LOCAL_NAME);
	}

	private void testEventReader(Object result) throws XMLStreamException {

		assertThat(result).isInstanceOf(XMLEventReader.class);

		XMLEventReader eventReader = (XMLEventReader) result;

		assertThat(eventReader.hasNext()).isTrue();

		XMLEvent event = eventReader.nextTag();

		assertThat(event.getEventType()).isEqualTo(XMLStreamConstants.START_ELEMENT);

		StartElement startElement = (StartElement) event;

		assertThat(startElement.getName().getNamespaceURI()).isEqualTo(NAMESPACE_URI);
		assertThat(startElement.getName().getLocalPart()).isEqualTo(LOCAL_NAME);
	}

	public void invalid(XMLStreamReader streamReader) {
	}

	public void streamReader(@RequestPayload XMLStreamReader streamReader) {
	}

	public void eventReader(@RequestPayload XMLEventReader streamReader) {
	}

}
