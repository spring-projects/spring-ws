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

package org.springframework.ws.soap.stroap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.mime.AttachmentException;
import org.springframework.ws.soap.AbstractSoapMessage;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapEnvelopeException;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.support.SoapUtils;
import org.springframework.ws.stream.StreamingPayload;
import org.springframework.ws.stream.StreamingWebServiceMessage;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.xml.stream.AbstractXMLEventWriter;

/**
 * @author Arjen Poutsma
 */
public class StroapMessage extends AbstractSoapMessage implements StreamingWebServiceMessage {

    private final MultiValueMap<String, String> mimeHeaders = new LinkedMultiValueMap<String, String>();

    private StroapEnvelope envelope;

    private final StroapMessageFactory messageFactory;

    public StroapMessage(StroapMessageFactory messageFactory) {
        this(null, null, messageFactory);
    }

    public StroapMessage(MultiValueMap<String, String> mimeHeaders,
                         StroapEnvelope envelope,
                         StroapMessageFactory messageFactory) {
        Assert.notNull(messageFactory, "'messageFactory' must not be null");
        this.messageFactory = messageFactory;
        if (mimeHeaders != null) {
            this.mimeHeaders.putAll(mimeHeaders);
        }
        this.envelope = envelope != null ? envelope : new StroapEnvelope(messageFactory);
        if (!this.mimeHeaders.containsKey(TransportConstants.HEADER_CONTENT_TYPE)) {
            this.mimeHeaders
                    .set(TransportConstants.HEADER_CONTENT_TYPE, messageFactory.getSoapVersion().getContentType());
        }
        if (!this.mimeHeaders.containsKey(TransportConstants.HEADER_ACCEPT)) {
            this.mimeHeaders.set(TransportConstants.HEADER_ACCEPT, messageFactory.getSoapVersion().getContentType());
        }
    }

    static StroapMessage build(InputStream inputStream, StroapMessageFactory messageFactory)
            throws XMLStreamException, IOException {
        MultiValueMap<String, String> mimeHeaders = parseMimeHeaders(inputStream);
        XMLEventReader eventReader = messageFactory.getInputFactory().createXMLEventReader(inputStream);
        StroapEnvelope envelope = StroapEnvelope.build(eventReader, messageFactory);
        return new StroapMessage(mimeHeaders, envelope, messageFactory);
    }

    private static MultiValueMap<String, String> parseMimeHeaders(InputStream inputStream) throws IOException {
        MultiValueMap<String, String> mimeHeaders = new LinkedMultiValueMap<String, String>();
        if (inputStream instanceof TransportInputStream) {
            TransportInputStream transportInputStream = (TransportInputStream) inputStream;
            for (Iterator<String> headerNames = transportInputStream.getHeaderNames(); headerNames.hasNext();) {
                String headerName = headerNames.next();
                for (Iterator<String> headerValues = transportInputStream.getHeaders(headerName);
                     headerValues.hasNext();) {
                    String headerValue = headerValues.next();
                    StringTokenizer tokenizer = new StringTokenizer(headerValue, ",");
                    while (tokenizer.hasMoreTokens()) {
                        mimeHeaders.add(headerName, tokenizer.nextToken().trim());
                    }
                }
            }
        }
        return mimeHeaders;
    }

    public SoapEnvelope getEnvelope() throws SoapEnvelopeException {
        return envelope;
    }

    public void setStreamingPayload(StreamingPayload payload) {
        StroapBody soapBody = (StroapBody) getSoapBody();
        soapBody.setStreamingPayload(payload);
    }

    public String getSoapAction() {
        String soapAction = mimeHeaders.getFirst(TransportConstants.HEADER_SOAP_ACTION);
        return StringUtils.hasLength(soapAction) ? soapAction : TransportConstants.EMPTY_SOAP_ACTION;
    }

    public void setSoapAction(String soapAction) {
        soapAction = SoapUtils.escapeAction(soapAction);
        mimeHeaders.set(TransportConstants.HEADER_SOAP_ACTION, soapAction);
    }

    @Override
    public SoapVersion getVersion() {
        return messageFactory.getSoapVersion();
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        if (outputStream instanceof TransportOutputStream) {
            TransportOutputStream tos = (TransportOutputStream) outputStream;
            for (Map.Entry<String, List<String>> entry : mimeHeaders.entrySet()) {
                String name = entry.getKey();
                for (String value : entry.getValue()) {
                    tos.addHeader(name, value);
                }
            }
        }
        try {
            XMLEventWriter eventWriter = messageFactory.getOutputFactory().createXMLEventWriter(outputStream);
            eventWriter.add(messageFactory.getEventFactory().createStartDocument());
            envelope.writeTo(new NoStartEndDocumentWriter(eventWriter));
            eventWriter.add(messageFactory.getEventFactory().createEndDocument());
            eventWriter.flush();
        }
        catch (XMLStreamException ex) {
            throw new StroapMessageException("Could not write message to OutputStream: " + ex.getMessage(), ex);
        }
    }

    public boolean isXopPackage() {
        return false;
    }

    public boolean convertToXopPackage() {
        return false;
    }

    public Attachment getAttachment(String contentId) throws AttachmentException {
        throw new UnsupportedOperationException();
    }

    public Iterator<Attachment> getAttachments() throws AttachmentException {
        return Collections.<Attachment>emptyList().iterator();
    }

    public Attachment addAttachment(String contentId, DataHandler dataHandler) {
        throw new UnsupportedOperationException();
    }

    private static class NoStartEndDocumentWriter extends AbstractXMLEventWriter {

        private final XMLEventWriter delegate;

        private NoStartEndDocumentWriter(XMLEventWriter delegate) {
            this.delegate = delegate;
        }

        public void add(XMLEvent event) throws XMLStreamException {
            if (!event.isStartDocument() && !event.isEndDocument()) {
                delegate.add(event);
            }
        }
    }

}
