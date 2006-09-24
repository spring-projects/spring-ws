/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.oxm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Testcase for <code>AbstractMarshaller</code>.
 *
 * @author Arjen Poutsma
 */
public class MarshallerTest extends TestCase {

    private MockControl control;

    private AbstractMarshaller mock;

    protected void setUp() throws Exception {
        control = MockClassControl.createControl(AbstractMarshaller.class);
        mock = (AbstractMarshaller) control.getMock();
    }

    public void testMarshalDomResult() throws IOException {
        DOMResult domResult = new DOMResult();
        Object graph = new Object();
        mock.marshalDomResult(graph, domResult);

        control.replay();
        mock.marshal(graph, domResult);
        control.verify();
    }

    public void testMarshalEmptyStreamResult() throws IOException {
        StreamResult streamResult = new StreamResult();
        AbstractMarshaller marshaller = new FailingMarshaller();
        try {
            marshaller.marshalStreamResult(null, streamResult);
            fail("marshalStreamResult with empty StreamResult failed to throw exception");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testMarshalSaxResult() throws IOException {
        SAXResult saxResult = new SAXResult();
        Object graph = new Object();
        mock.marshalSaxResult(graph, saxResult);

        control.replay();
        mock.marshal(graph, saxResult);
        control.verify();
    }

    public void testMarshalSaxResultImpl() {
        final DefaultHandler handler = new DefaultHandler();
        final Object object = new Object();
        AbstractMarshaller marshaller = new FailingMarshaller() {

            protected void marshalSaxHandlers(Object graph,
                                              ContentHandler contentHandler,
                                              LexicalHandler lexicalHandler) {
                assertEquals("Invalid graph", object, graph);
                assertEquals("Invalid content handler", handler, contentHandler);
                assertNull("Not expected lexical handler", lexicalHandler);
            }

        };
        SAXResult saxResult = new SAXResult(handler);
        marshaller.marshalSaxResult(object, saxResult);
    }

    public void testMarshalStreamResult() throws IOException {
        StreamResult streamResult = new StreamResult();
        Object graph = new Object();
        mock.marshalStreamResult(graph, streamResult);

        control.replay();
        mock.marshal(graph, streamResult);
        control.verify();
    }

    public void testMarshalInvalidResult() throws IOException {
        Result result = new Result() {
            public void setSystemId(String systemId) {
            }

            public String getSystemId() {
                return null;
            }
        };

        try {
            mock.marshal(null, result);
            fail("marshal with invalid Result failed to throw exception");
        }
        catch (IllegalArgumentException e) {
            // expected
        }

    }

    public void testMarshalStreamResultOutputStream() throws IOException {
        final OutputStream stream = new ByteArrayOutputStream();
        final Object object = new Object();
        AbstractMarshaller marshaller = new FailingMarshaller() {

            protected void marshalOutputStream(Object graph, OutputStream givenStream) throws XmlMappingException {
                assertEquals("Invalid graph", object, graph);
                assertEquals("Invalid stream", stream, givenStream);
            }
        };
        StreamResult streamResult = new StreamResult(stream);
        marshaller.marshalStreamResult(object, streamResult);
    }

    public void testMarshalStreamResultWriter() throws IOException {
        final Writer writer = new StringWriter();
        final Object object = new Object();
        AbstractMarshaller marshaller = new FailingMarshaller() {

            protected void marshalWriter(Object graph, Writer givenWriter) throws XmlMappingException {
                assertEquals("Invalid graph", object, graph);
                assertEquals("Invalid writer", writer, givenWriter);
            }

        };
        StreamResult streamResult = new StreamResult(writer);
        marshaller.marshalStreamResult(object, streamResult);
    }

    public void testUnmarshalDomSource() throws IOException {
        DOMSource domSource = new DOMSource();
        Object object = new Object();
        control.expectAndReturn(mock.unmarshalDomSource(domSource), object);

        control.replay();
        Object result = mock.unmarshal(domSource);
        assertEquals("Invalid result object", object, result);
        control.verify();
    }

    public void testUnmarshalInvalidSource() throws IOException {
        Source source = new Source() {
            public void setSystemId(String systemId) {
            }

            public String getSystemId() {
                return null;
            }
        };
        try {
            mock.unmarshal(source);
            fail("unmarshal with invalid Result failed to throw exception");
        }
        catch (IllegalArgumentException e) {
            // expected
        }


    }

    public void testUnmarshalEmptyStreamSource() throws IOException {
        AbstractMarshaller marshaller = new FailingMarshaller();
        StreamSource streamSource = new StreamSource();
        try {
            marshaller.unmarshalStreamSource(streamSource);
            fail("unmarshalStreamSource with empty StreamSource failed to throw exception");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testUnmarshalSaxSource() throws IOException {
        SAXSource saxSource = new SAXSource();
        Object object = new Object();
        control.expectAndReturn(mock.unmarshalSaxSource(saxSource), object);

        control.replay();
        Object result = mock.unmarshal(saxSource);
        assertEquals("Invalid result object", object, result);
        control.verify();
    }

    public void testUnmarshalSaxSourceXmlReader() throws IOException, SAXException {
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        final InputSource inputSource = new InputSource();
        final Object object = new Object();
        AbstractMarshaller marshaller = new FailingMarshaller() {
            protected Object unmarshalSaxReader(XMLReader givenXmlReader, InputSource givenInputSource)
                    throws XmlMappingException {
                assertEquals("Invalid xmlReader", xmlReader, givenXmlReader);
                assertEquals("Invalid inputSource", inputSource, givenInputSource);
                return object;
            }

        };
        SAXSource saxSource = new SAXSource(xmlReader, inputSource);
        Object result = marshaller.unmarshalSaxSource(saxSource);
        assertEquals("Invalid result returned", object, result);
    }

    public void testUnmarshalStreamSource() throws IOException {
        StreamSource streamSource = new StreamSource();
        Object object = new Object();
        control.expectAndReturn(mock.unmarshalStreamSource(streamSource), object);

        control.replay();
        Object result = mock.unmarshal(streamSource);
        assertEquals("Invalid result object", object, result);
        control.verify();
    }

    public void testUnmarshalStreamSourceInputStream() throws IOException {
        final InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        final Object object = new Object();
        AbstractMarshaller marshaller = new FailingMarshaller() {

            protected Object unmarshalInputStream(InputStream givenInputStream) {
                assertEquals("Invalid inputStream", inputStream, givenInputStream);
                return object;
            }

        };
        StreamSource streamSource = new StreamSource(inputStream);
        Object result = marshaller.unmarshalStreamSource(streamSource);
        assertEquals("Invalid result returned", object, result);
    }

    public void testUnmarshalStreamSourceReader() throws IOException {
        final Reader reader = new StringReader("");
        final Object object = new Object();
        AbstractMarshaller marshaller = new FailingMarshaller() {

            protected Object unmarshalReader(Reader givenReader) {
                assertEquals("Invalid reader", reader, givenReader);
                return object;
            }
        };
        StreamSource streamSource = new StreamSource(reader);
        Object result = marshaller.unmarshalStreamSource(streamSource);
        assertEquals("Invalid result returned", object, result);
    }

    private class FailingMarshaller extends AbstractMarshaller {

        protected void marshalDomNode(Object graph, Node node) throws XmlMappingException {
            fail("Not expected");
        }

        protected void marshalOutputStream(Object graph, OutputStream outputStream) {
            fail("Not expected");
        }

        protected void marshalSaxHandlers(Object graph, ContentHandler contentHandler, LexicalHandler lexicalHandler)
                throws XmlMappingException {
            fail("Not expected");
        }

        protected void marshalWriter(Object graph, Writer writer) throws XmlMappingException, IOException {
            fail("Not expected");
        }

        protected Object unmarshalDomNode(Node node) throws XmlMappingException {
            fail("Not expected");
            return null;
        }

        protected Object unmarshalSaxReader(XMLReader xmlReader, InputSource inputSource)
                throws XmlMappingException, IOException {
            fail("Not expected");
            return null;
        }

        protected Object unmarshalInputStream(InputStream inputStream) throws XmlMappingException, IOException {
            fail("Not expected");
            return null;
        }

        protected Object unmarshalReader(Reader reader) throws XmlMappingException, IOException {
            fail("Not expected");
            return null;
        }
    }
}