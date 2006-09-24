/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.endpoint;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLTestCase;

/**
 * Test case for AbstractStaxStreamingPayloadEndpoint.
 */
public class StaxStreamingPayloadEndpointTest extends XMLTestCase {

    public void testInvokeInternalNullResponse() throws Exception {

        AbstractStaxStreamingPayloadEndpoint endpoint = new AbstractStaxStreamingPayloadEndpoint() {

            protected void invokeInternal(XMLStreamReader streamReader, XMLStreamWriter streamWriter) {
            }

        };
        endpoint.afterPropertiesSet();
        Source responseSource = endpoint.invoke(new StreamSource(new StringReader("<element/>")));
        assertNull("Invalid response", responseSource);
    }

    public void testInvokeInternal() throws Exception {
        AbstractStaxStreamingPayloadEndpoint endpoint = new AbstractStaxStreamingPayloadEndpoint() {

            protected void invokeInternal(XMLStreamReader streamReader, XMLStreamWriter streamWriter)
                    throws XMLStreamException {
                assertNotNull("Invalid null stream reader", streamReader);
                assertNotNull("Invalid null stream writer", streamWriter);
                assertEquals("Invalid request", XMLStreamReader.START_ELEMENT, streamReader.next());
                streamWriter.writeStartElement("response");
                streamWriter.writeEndElement();
            }

        };
        endpoint.afterPropertiesSet();
        Source requestSource = new StreamSource(new StringReader("<request/>"));

        Source resultSource = endpoint.invoke(requestSource);
        assertNotNull("Invalid null response", resultSource);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(resultSource, new StreamResult(writer));
        assertXMLEqual("Invalid response", "<response/>", writer.toString());
    }

}