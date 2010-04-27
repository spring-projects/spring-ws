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

package org.springframework.ws.server.endpoint.adapter.method;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** @author Arjen Poutsma */
@SuppressWarnings("Since15")
public class StaxPayloadMethodArgumentResolverTest extends AbstractMethodArgumentResolverTest {

    private StaxPayloadMethodArgumentResolver resolver;

    private MethodParameter streamParameter;

    private MethodParameter eventParameter;

    @Before
    public void setUp() throws Exception {
        resolver = new StaxPayloadMethodArgumentResolver();
        streamParameter = new MethodParameter(getClass().getMethod("streamReader", XMLStreamReader.class), 0);
        eventParameter = new MethodParameter(getClass().getMethod("eventReader", XMLEventReader.class), 0);
    }

    @Test
    public void resolveStreamReaderSaaj() throws Exception {
        MessageContext messageContext = createSaajMessageContext();

        Object result = resolver.resolveArgument(messageContext, streamParameter);

        testStreamReader(result);
    }

    @Test
    public void resolveStreamReaderAxiomCaching() throws Exception {
        MessageContext messageContext = createCachingAxiomMessageContext();

        Object result = resolver.resolveArgument(messageContext, streamParameter);

        testStreamReader(result);
    }

    @Test
    public void resolveStreamReaderAxiomNonCaching() throws Exception {
        MessageContext messageContext = createNonCachingAxiomMessageContext();

        Object result = resolver.resolveArgument(messageContext, streamParameter);

        testStreamReader(result);
    }

    @Test
    public void resolveStreamReaderStream() throws Exception {
        MessageContext messageContext = createMockMessageContext();

        Object result = resolver.resolveArgument(messageContext, streamParameter);

        testStreamReader(result);
    }

    @Test
    public void resolveEventReaderSaaj() throws Exception {
        MessageContext messageContext = createSaajMessageContext();

        Object result = resolver.resolveArgument(messageContext, eventParameter);

        testEventReader(result);
    }

    @Test
    public void resolveEventReaderAxiomCaching() throws Exception {
        MessageContext messageContext = createCachingAxiomMessageContext();

        Object result = resolver.resolveArgument(messageContext, eventParameter);

        testEventReader(result);
    }

    @Test
    public void resolveEventReaderAxiomNonCaching() throws Exception {
        MessageContext messageContext = createNonCachingAxiomMessageContext();

        Object result = resolver.resolveArgument(messageContext, eventParameter);

        testEventReader(result);
    }

    @Test
    public void resolveEventReaderStream() throws Exception {
        MessageContext messageContext = createMockMessageContext();

        Object result = resolver.resolveArgument(messageContext, eventParameter);

        testEventReader(result);
    }


    private void testStreamReader(Object result) throws XMLStreamException {
        assertTrue("resolver does not return XMLStreamReader", result instanceof XMLStreamReader);
        XMLStreamReader streamReader = (XMLStreamReader) result;
        assertTrue("streamReader has no next element", streamReader.hasNext());
        assertEquals(XMLStreamConstants.START_ELEMENT, streamReader.nextTag());
        assertEquals("Invalid namespace", NAMESPACE_URI, streamReader.getNamespaceURI());
        assertEquals("Invalid local name", LOCAL_NAME, streamReader.getLocalName());
    }

    private void testEventReader(Object result) throws XMLStreamException {
        assertTrue("resolver does not return XMLEventReader", result instanceof XMLEventReader);
        XMLEventReader eventReader = (XMLEventReader) result;
        assertTrue("eventReader has no next element", eventReader.hasNext());
        XMLEvent event = eventReader.nextTag();
        assertEquals(XMLStreamConstants.START_ELEMENT, event.getEventType());
        StartElement startElement = (StartElement) event;
        assertEquals("Invalid namespace", NAMESPACE_URI, startElement.getName().getNamespaceURI());
        assertEquals("Invalid local name", LOCAL_NAME, startElement.getName().getLocalPart());
    }


    public void invalid(XMLStreamReader streamReader) {
    }

    public void streamReader(@RequestPayload XMLStreamReader streamReader) {
    }

    public void eventReader(@RequestPayload XMLEventReader streamReader) {
    }
}
