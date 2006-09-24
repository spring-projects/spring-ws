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

package org.springframework.ws.endpoint;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;

/**
 * Abstract base class for endpoints that handle the message payload with event-based StAX. Allows subclasses to read
 * the request with a <code>XMLEventReader</code>, and to create a response using a <code>XMLEventWriter</code>.
 *
 * @author Arjen Poutsma
 * @see #invokeInternal(javax.xml.stream.XMLEventReader, javax.xml.stream.util.XMLEventConsumer,
 *      javax.xml.stream.XMLEventFactory)
 * @see XMLEventReader
 * @see XMLEventWriter
 */
public abstract class AbstractStaxEventPayloadEndpoint extends AbstractStaxPayloadEndpoint implements MessageEndpoint {

    private XMLEventFactory eventFactory;

    /**
     * Returns an <code>XMLEventFactory</code> to read XML from.
     */
    private XMLEventFactory getEventFactory() {
        if (eventFactory == null) {
            eventFactory = createXmlEventFactory();
        }
        return eventFactory;
    }

    /**
     * Create a <code>XMLEventFactory</code> that this endpoint will use to create <code>XMLEvent</code>s. Can be
     * overridden in subclasses, adding further initialization of the factory. The resulting
     * <code>XMLEventFactory</code> is cached, so this method will only be called once.
     *
     * @return the created <code>XMLEventFactory</code>
     */
    protected XMLEventFactory createXmlEventFactory() {
        return XMLEventFactory.newInstance();
    }

    public final void invoke(MessageContext messageContext) throws Exception {
        XMLEventReader eventReader =
                getInputFactory().createXMLEventReader(messageContext.getRequest().getPayloadSource());
        XMLEventWriter streamWriter = new ResponseCreatingEventWriter(messageContext);
        invokeInternal(eventReader, streamWriter, getEventFactory());
        streamWriter.flush();
    }

    /**
     * Template method. Subclasses must implement this. Offers the request payload as a <code>XMLEventReader</code>, and
     * a <code>XMLEventWriter</code> to write the response payload to.
     *
     * @param eventReader  the reader to read the payload events from
     * @param eventWriter  the writer to write payload events to
     * @param eventFactory an <code>XMLEventFactory</code> that can be used to create events
     */
    protected abstract void invokeInternal(XMLEventReader eventReader,
                                           XMLEventConsumer eventWriter,
                                           XMLEventFactory eventFactory) throws Exception;

    /**
     * Implementation of the <code>XMLEventWriter</code> interface that creates a response
     * <code>WebServiceMessage</code> as soon as any method is called, thus lazily creating the response.
     */
    private class ResponseCreatingEventWriter implements XMLEventWriter {

        private XMLEventWriter eventWriter;

        private MessageContext messageContext;

        public ResponseCreatingEventWriter(MessageContext messageContext) {
            this.messageContext = messageContext;
        }

        private void createEventWriter() throws XMLStreamException {
            if (eventWriter == null) {
                WebServiceMessage response = messageContext.createResponse();
                eventWriter = getOutputFactory().createXMLEventWriter(response.getPayloadResult());
            }
        }

        public void flush() throws XMLStreamException {
            if (eventWriter != null) {
                eventWriter.flush();
            }
        }

        public void close() throws XMLStreamException {
            if (eventWriter != null) {
                eventWriter.close();
            }
        }

        public void add(XMLEvent event) throws XMLStreamException {
            createEventWriter();
            eventWriter.add(event);
        }

        public void add(XMLEventReader reader) throws XMLStreamException {
            createEventWriter();
            eventWriter.add(reader);
        }

        public String getPrefix(String uri) throws XMLStreamException {
            createEventWriter();
            return eventWriter.getPrefix(uri);
        }

        public void setPrefix(String prefix, String uri) throws XMLStreamException {
            createEventWriter();
            eventWriter.setPrefix(prefix, uri);
        }

        public void setDefaultNamespace(String uri) throws XMLStreamException {
            createEventWriter();
            eventWriter.setDefaultNamespace(uri);
        }

        public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
            createEventWriter();
            eventWriter.setNamespaceContext(context);
        }

        public NamespaceContext getNamespaceContext() {
            return eventWriter.getNamespaceContext();
        }
    }

}
