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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.transform.TraxUtils;

/**
 * Abstract base class for endpoints that handle the message payload with event-based StAX. Allows subclasses to read
 * the request with a <code>XMLEventReader</code>, and to create a response using a <code>XMLEventWriter</code>.
 *
 * @author Arjen Poutsma
 * @see #invokeInternal(javax.xml.stream.XMLEventReader,javax.xml.stream.util.XMLEventConsumer,
 *      javax.xml.stream.XMLEventFactory)
 * @see XMLEventReader
 * @see XMLEventWriter
 * @since 1.0.0
 * @deprecated as of Spring Web Services 2.0, in favor of annotated endpoints
 */
@Deprecated
@SuppressWarnings("Since15")
public abstract class AbstractStaxEventPayloadEndpoint extends AbstractStaxPayloadEndpoint implements MessageEndpoint {

    private XMLEventFactory eventFactory;

    public final void invoke(MessageContext messageContext) throws Exception {
        XMLEventReader eventReader = getEventReader(messageContext.getRequest().getPayloadSource());
        XMLEventWriter streamWriter = new ResponseCreatingEventWriter(messageContext);
        invokeInternal(eventReader, streamWriter, getEventFactory());
        streamWriter.flush();
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

    /** Returns an <code>XMLEventFactory</code> to read XML from. */
    private XMLEventFactory getEventFactory() {
        if (eventFactory == null) {
            eventFactory = createXmlEventFactory();
        }
        return eventFactory;
    }

    private XMLEventReader getEventReader(Source source) throws XMLStreamException, TransformerException {
        if (source == null) {
            return null;
        }
        XMLEventReader eventReader = null;
        if (TraxUtils.isStaxSource(source)) {
            eventReader = TraxUtils.getXMLEventReader(source);
            if (eventReader == null) {
                XMLStreamReader streamReader = TraxUtils.getXMLStreamReader(source);
                if (streamReader != null) {
                    try {
                        eventReader = getInputFactory().createXMLEventReader(streamReader);
                    }
                    catch (XMLStreamException ex) {
                        eventReader = null;
                    }
                }
            }
        }
        if (eventReader == null) {
            try {
                eventReader = getInputFactory().createXMLEventReader(source);
            }
            catch (XMLStreamException ex) {
                eventReader = null;
            }
            catch (UnsupportedOperationException ex) {
                eventReader = null;
            }
        }
        if (eventReader == null) {
            // as a final resort, transform the source to a stream, and read from that
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            transform(source, new StreamResult(os));
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            eventReader = getInputFactory().createXMLEventReader(is);
        }
        return eventReader;
    }

    private XMLEventWriter getEventWriter(Result result) {
        XMLEventWriter eventWriter = null;
        if (TraxUtils.isStaxResult(result)) {
            eventWriter = TraxUtils.getXMLEventWriter(result);
        }
        if (eventWriter == null) {
            try {
                eventWriter = getOutputFactory().createXMLEventWriter(result);
            }
            catch (XMLStreamException ex) {
                // ignore
            }
        }
        return eventWriter;
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

        private ByteArrayOutputStream os;

        public ResponseCreatingEventWriter(MessageContext messageContext) {
            this.messageContext = messageContext;
        }

        public NamespaceContext getNamespaceContext() {
            return eventWriter.getNamespaceContext();
        }

        public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
            createEventWriter();
            eventWriter.setNamespaceContext(context);
        }

        public void add(XMLEventReader reader) throws XMLStreamException {
            createEventWriter();
            while (reader.hasNext()) {
                add(reader.nextEvent());
            }
        }

        public void add(XMLEvent event) throws XMLStreamException {
            createEventWriter();
            eventWriter.add(event);
            if (event.isEndDocument()) {
                if (os != null) {
                    eventWriter.flush();
                    // if we used an output stream cache, we have to transform it to the response again
                    try {
                        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
                        transform(new StreamSource(is), messageContext.getResponse().getPayloadResult());
                    }
                    catch (TransformerException ex) {
                        throw new XMLStreamException(ex);
                    }
                }
            }
        }

        public void close() throws XMLStreamException {
            if (eventWriter != null) {
                eventWriter.close();
            }
        }

        public void flush() throws XMLStreamException {
            if (eventWriter != null) {
                eventWriter.flush();
            }
        }

        public String getPrefix(String uri) throws XMLStreamException {
            createEventWriter();
            return eventWriter.getPrefix(uri);
        }

        public void setDefaultNamespace(String uri) throws XMLStreamException {
            createEventWriter();
            eventWriter.setDefaultNamespace(uri);
        }

        public void setPrefix(String prefix, String uri) throws XMLStreamException {
            createEventWriter();
            eventWriter.setPrefix(prefix, uri);
        }

        private void createEventWriter() throws XMLStreamException {
            if (eventWriter == null) {
                WebServiceMessage response = messageContext.getResponse();
                eventWriter = getEventWriter(response.getPayloadResult());
                if (eventWriter == null) {
                    // as a final resort, use a stream, and transform that at endDocument()
                    os = new ByteArrayOutputStream();
                    eventWriter = getOutputFactory().createXMLEventWriter(os);
                }
            }
        }
    }
}
