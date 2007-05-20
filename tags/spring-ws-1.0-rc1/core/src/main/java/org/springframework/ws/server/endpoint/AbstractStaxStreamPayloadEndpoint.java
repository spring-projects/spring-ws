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

package org.springframework.ws.server.endpoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.stream.XmlEventStreamReader;
import org.springframework.xml.transform.StaxResult;
import org.springframework.xml.transform.StaxSource;

/**
 * Abstract base class for endpoints that handle the message payload with streaming StAX. Allows subclasses to read the
 * request with a <code>XMLStreamReader</code>, and to create a response using a <code>XMLStreamWriter</code>.
 *
 * @author Arjen Poutsma
 * @see #invokeInternal(javax.xml.stream.XMLStreamReader,javax.xml.stream.XMLStreamWriter)
 * @see XMLStreamReader
 * @see XMLStreamWriter
 */
public abstract class AbstractStaxStreamPayloadEndpoint extends AbstractStaxPayloadEndpoint implements MessageEndpoint {

    public final void invoke(MessageContext messageContext) throws Exception {
        XMLStreamReader streamReader = getStreamReader(messageContext.getRequest().getPayloadSource());
        XMLStreamWriter streamWriter = new ResponseCreatingStreamWriter(messageContext);
        invokeInternal(streamReader, streamWriter);
        streamWriter.close();
    }

    private XMLStreamReader getStreamReader(Source source) throws XMLStreamException, TransformerException {
        XMLStreamReader streamReader = null;
        if (source instanceof StaxSource) {
            streamReader = ((StaxSource) source).getXMLStreamReader();
            StaxSource staxSource = (StaxSource) source;
            streamReader = staxSource.getXMLStreamReader();
            if (streamReader == null && staxSource.getXMLEventReader() != null) {
                try {
                    streamReader = new XmlEventStreamReader(staxSource.getXMLEventReader());
                }
                catch (XMLStreamException ex) {
                    // ignore
                }
            }

        }
        if (streamReader == null) {
            try {
                streamReader = getInputFactory().createXMLStreamReader(source);
            }
            catch (XMLStreamException ex) {
                // ignore
            }
        }
        if (streamReader == null) {
            // as a final resort, transform the source to a stream, and read from that
            Transformer transformer = createTransformer();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            transformer.transform(source, new StreamResult(os));
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            streamReader = getInputFactory().createXMLStreamReader(is);
        }
        return streamReader;
    }

    private XMLStreamWriter getStreamWriter(Result result) {
        XMLStreamWriter streamWriter = null;
        if (result instanceof StaxResult) {
            StaxResult staxResult = (StaxResult) result;
            streamWriter = staxResult.getXMLStreamWriter();
        }
        if (streamWriter == null) {
            try {
                streamWriter = getOutputFactory().createXMLStreamWriter(result);
            }
            catch (XMLStreamException ex) {
                // ignore
            }
        }
        return streamWriter;
    }

    /**
     * Template method. Subclasses must implement this. Offers the request payload as a <code>XMLStreamReader</code>,
     * and a <code>XMLStreamWriter</code> to write the response payload to.
     *
     * @param streamReader the reader to read the payload from
     * @param streamWriter the writer to write the payload to
     */
    protected abstract void invokeInternal(XMLStreamReader streamReader, XMLStreamWriter streamWriter) throws Exception;

    /**
     * Implementation of the <code>XMLStreamWriter</code> interface that creates a response
     * <code>WebServiceMessage</code> as soon as any method is called, thus lazily creating the response.
     */
    private class ResponseCreatingStreamWriter implements XMLStreamWriter {

        private MessageContext messageContext;

        private XMLStreamWriter streamWriter;

        private ByteArrayOutputStream os;

        private ResponseCreatingStreamWriter(MessageContext messageContext) {
            this.messageContext = messageContext;
        }

        public NamespaceContext getNamespaceContext() {
            return streamWriter.getNamespaceContext();
        }

        public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
            createStreamWriter();
            streamWriter.setNamespaceContext(context);
        }

        public void close() throws XMLStreamException {
            if (streamWriter != null) {
                streamWriter.close();
                if (os != null) {
                    streamWriter.flush();
                    // if we used an output stream cache, we have to transform it to the response again
                    try {
                        Transformer transformer = createTransformer();
                        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
                        transformer.transform(new StreamSource(is), messageContext.getResponse().getPayloadResult());
                        os = null;
                    }
                    catch (TransformerException ex) {
                        throw new XMLStreamException(ex);
                    }
                }
                streamWriter = null;
            }

        }

        public void flush() throws XMLStreamException {
            if (streamWriter != null) {
                streamWriter.flush();
            }
        }

        public String getPrefix(String uri) throws XMLStreamException {
            createStreamWriter();
            return streamWriter.getPrefix(uri);
        }

        public Object getProperty(String name) throws IllegalArgumentException {
            return streamWriter.getProperty(name);
        }

        public void setDefaultNamespace(String uri) throws XMLStreamException {
            createStreamWriter();
            streamWriter.setDefaultNamespace(uri);
        }

        public void setPrefix(String prefix, String uri) throws XMLStreamException {
            createStreamWriter();
            streamWriter.setPrefix(prefix, uri);
        }

        public void writeAttribute(String localName, String value) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeAttribute(localName, value);
        }

        public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeAttribute(namespaceURI, localName, value);
        }

        public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
                throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeAttribute(prefix, namespaceURI, localName, value);
        }

        public void writeCData(String data) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeCData(data);
        }

        public void writeCharacters(String text) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeCharacters(text);
        }

        public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeCharacters(text, start, len);
        }

        public void writeComment(String data) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeComment(data);
        }

        public void writeDTD(String dtd) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeDTD(dtd);
        }

        public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeDefaultNamespace(namespaceURI);
        }

        public void writeEmptyElement(String localName) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeEmptyElement(localName);
        }

        public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeEmptyElement(namespaceURI, localName);
        }

        public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeEmptyElement(prefix, localName, namespaceURI);
        }

        public void writeEndDocument() throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeEndDocument();
        }

        public void writeEndElement() throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeEndElement();
        }

        public void writeEntityRef(String name) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeEntityRef(name);
        }

        public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeNamespace(prefix, namespaceURI);
        }

        public void writeProcessingInstruction(String target) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeProcessingInstruction(target);
        }

        public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeProcessingInstruction(target, data);
        }

        public void writeStartDocument() throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeStartDocument();
        }

        public void writeStartDocument(String version) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeStartDocument(version);
        }

        public void writeStartDocument(String encoding, String version) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeStartDocument(encoding, version);
        }

        public void writeStartElement(String localName) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeStartElement(localName);
        }

        public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeStartElement(namespaceURI, localName);
        }

        public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeStartElement(prefix, localName, namespaceURI);
        }

        private void createStreamWriter() throws XMLStreamException {
            if (streamWriter == null) {
                WebServiceMessage response = messageContext.getResponse();
                streamWriter = getStreamWriter(response.getPayloadResult());
                if (streamWriter == null) {
                    // as a final resort, use a stream, and transform that at endDocument()
                    os = new ByteArrayOutputStream();
                    streamWriter = getOutputFactory().createXMLStreamWriter(os);
                }
            }
        }
    }
}
