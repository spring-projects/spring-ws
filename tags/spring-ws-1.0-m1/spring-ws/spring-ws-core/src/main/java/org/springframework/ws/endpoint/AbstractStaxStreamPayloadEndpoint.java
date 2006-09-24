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

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;

/**
 * Abstract base class for endpoints that handle the message payload with streaming StAX. Allows subclasses to read the
 * request with a <code>XMLStreamReader</code>, and to create a response using a <code>XMLStreamWriter</code>.
 *
 * @author Arjen Poutsma
 * @see #invokeInternal(javax.xml.stream.XMLStreamReader, javax.xml.stream.XMLStreamWriter)
 * @see XMLStreamReader
 * @see XMLStreamWriter
 */
public abstract class AbstractStaxStreamPayloadEndpoint extends AbstractStaxPayloadEndpoint implements MessageEndpoint {

    public final void invoke(MessageContext messageContext) throws Exception {
        XMLStreamReader streamReader =
                getInputFactory().createXMLStreamReader(messageContext.getRequest().getPayloadSource());
        XMLStreamWriter streamWriter = new ResponseCreatingStreamWriter(messageContext);
        invokeInternal(streamReader, streamWriter);
        streamWriter.flush();
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

        private ResponseCreatingStreamWriter(MessageContext messageContext) {
            this.messageContext = messageContext;
        }

        private void createStreamWriter() throws XMLStreamException {
            if (streamWriter == null) {
                WebServiceMessage response = messageContext.createResponse();
                streamWriter = getOutputFactory().createXMLStreamWriter(response.getPayloadResult());
            }
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

        public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeEmptyElement(namespaceURI, localName);
        }

        public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeEmptyElement(prefix, localName, namespaceURI);
        }

        public void writeEmptyElement(String localName) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeEmptyElement(localName);
        }

        public void writeEndElement() throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeEndElement();
        }

        public void writeEndDocument() throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeEndDocument();
        }

        public void close() throws XMLStreamException {
            if (streamWriter != null) {
                streamWriter.close();
            }
        }

        public void flush() throws XMLStreamException {
            if (streamWriter != null) {
                streamWriter.flush();
            }
        }

        public void writeAttribute(String localName, String value) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeAttribute(localName, value);
        }

        public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
                throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeAttribute(prefix, namespaceURI, localName, value);
        }

        public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeAttribute(namespaceURI, localName, value);
        }

        public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeNamespace(prefix, namespaceURI);
        }

        public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeDefaultNamespace(namespaceURI);
        }

        public void writeComment(String data) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeComment(data);
        }

        public void writeProcessingInstruction(String target) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeProcessingInstruction(target);
        }

        public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeProcessingInstruction(target, data);
        }

        public void writeCData(String data) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeCData(data);
        }

        public void writeDTD(String dtd) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeDTD(dtd);
        }

        public void writeEntityRef(String name) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeEntityRef(name);
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

        public void writeCharacters(String text) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeCharacters(text);
        }

        public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
            createStreamWriter();
            streamWriter.writeCharacters(text, start, len);
        }

        public String getPrefix(String uri) throws XMLStreamException {
            createStreamWriter();
            return streamWriter.getPrefix(uri);
        }

        public void setPrefix(String prefix, String uri) throws XMLStreamException {
            createStreamWriter();
            streamWriter.setPrefix(prefix, uri);
        }

        public void setDefaultNamespace(String uri) throws XMLStreamException {
            createStreamWriter();
            streamWriter.setDefaultNamespace(uri);
        }

        public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
            createStreamWriter();
            streamWriter.setNamespaceContext(context);
        }

        public NamespaceContext getNamespaceContext() {
            return streamWriter.getNamespaceContext();
        }

        public Object getProperty(String name) throws IllegalArgumentException {
            return streamWriter.getProperty(name);
        }
    }
}
