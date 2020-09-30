/*
 * Copyright 2005-2014 the original author or authors.
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
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.util.xml.StaxUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;

/**
 * Abstract base class for endpoints that handle the message payload with streaming StAX. Allows subclasses to read the
 * request with a {@code XMLStreamReader}, and to create a response using a {@code XMLStreamWriter}.
 *
 * @author Arjen Poutsma
 * @see #invokeInternal(javax.xml.stream.XMLStreamReader,javax.xml.stream.XMLStreamWriter)
 * @see XMLStreamReader
 * @see XMLStreamWriter
 * @since 1.0.0
 * @deprecated as of Spring Web Services 2.0, in favor of annotated endpoints
 */
@Deprecated
@SuppressWarnings("Since15")
public abstract class AbstractStaxStreamPayloadEndpoint extends AbstractStaxPayloadEndpoint implements MessageEndpoint {

	@Override
	public final void invoke(MessageContext messageContext) throws Exception {
		XMLStreamReader streamReader = getStreamReader(messageContext.getRequest().getPayloadSource());
		XMLStreamWriter streamWriter = new ResponseCreatingStreamWriter(messageContext);
		invokeInternal(streamReader, streamWriter);
		streamWriter.close();
	}

	private XMLStreamReader getStreamReader(Source source) throws XMLStreamException, TransformerException {
		if (source == null) {
			return null;
		}
		XMLStreamReader streamReader = null;
		if (StaxUtils.isStaxSource(source)) {
			streamReader = StaxUtils.getXMLStreamReader(source);
			if (streamReader == null) {
				XMLEventReader eventReader = StaxUtils.getXMLEventReader(source);
				if (eventReader != null) {
					try {
						streamReader = StaxUtils.createEventStreamReader(eventReader);
					} catch (XMLStreamException ex) {
						streamReader = null;
					}
				}
			}

		}
		if (streamReader == null) {
			try {
				streamReader = getInputFactory().createXMLStreamReader(source);
			} catch (XMLStreamException ex) {
				streamReader = null;
			} catch (UnsupportedOperationException ex) {
				streamReader = null;
			}
		}
		if (streamReader == null) {
			// as a final resort, transform the source to a stream, and read from that
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			transform(source, new StreamResult(os));
			ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
			streamReader = getInputFactory().createXMLStreamReader(is);
		}
		return streamReader;
	}

	private XMLStreamWriter getStreamWriter(Result result) {
		XMLStreamWriter streamWriter = null;
		if (StaxUtils.isStaxResult(result)) {
			streamWriter = StaxUtils.getXMLStreamWriter(result);
		}
		if (streamWriter == null) {
			try {
				streamWriter = getOutputFactory().createXMLStreamWriter(result);
			} catch (XMLStreamException ex) {
				// ignore
			}
		}
		return streamWriter;
	}

	/**
	 * Template method. Subclasses must implement this. Offers the request payload as a {@code XMLStreamReader}, and a
	 * {@code XMLStreamWriter} to write the response payload to.
	 *
	 * @param streamReader the reader to read the payload from
	 * @param streamWriter the writer to write the payload to
	 */
	protected abstract void invokeInternal(XMLStreamReader streamReader, XMLStreamWriter streamWriter) throws Exception;

	/**
	 * Implementation of the {@code XMLStreamWriter} interface that creates a response {@code WebServiceMessage} as soon
	 * as any method is called, thus lazily creating the response.
	 */
	private class ResponseCreatingStreamWriter implements XMLStreamWriter {

		private MessageContext messageContext;

		private XMLStreamWriter streamWriter;

		private ByteArrayOutputStream os;

		private ResponseCreatingStreamWriter(MessageContext messageContext) {
			this.messageContext = messageContext;
		}

		@Override
		public NamespaceContext getNamespaceContext() {
			return streamWriter.getNamespaceContext();
		}

		@Override
		public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
			createStreamWriter();
			streamWriter.setNamespaceContext(context);
		}

		@Override
		public void close() throws XMLStreamException {
			if (streamWriter != null) {
				streamWriter.close();
				if (os != null) {
					streamWriter.flush();
					// if we used an output stream cache, we have to transform it to the response again
					try {
						ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
						transform(new StreamSource(is), messageContext.getResponse().getPayloadResult());
						os = null;
					} catch (TransformerException ex) {
						throw new XMLStreamException(ex);
					}
				}
				streamWriter = null;
			}

		}

		@Override
		public void flush() throws XMLStreamException {
			if (streamWriter != null) {
				streamWriter.flush();
			}
		}

		@Override
		public String getPrefix(String uri) throws XMLStreamException {
			createStreamWriter();
			return streamWriter.getPrefix(uri);
		}

		@Override
		public Object getProperty(String name) throws IllegalArgumentException {
			return streamWriter.getProperty(name);
		}

		@Override
		public void setDefaultNamespace(String uri) throws XMLStreamException {
			createStreamWriter();
			streamWriter.setDefaultNamespace(uri);
		}

		@Override
		public void setPrefix(String prefix, String uri) throws XMLStreamException {
			createStreamWriter();
			streamWriter.setPrefix(prefix, uri);
		}

		@Override
		public void writeAttribute(String localName, String value) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeAttribute(localName, value);
		}

		@Override
		public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeAttribute(namespaceURI, localName, value);
		}

		@Override
		public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
				throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeAttribute(prefix, namespaceURI, localName, value);
		}

		@Override
		public void writeCData(String data) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeCData(data);
		}

		@Override
		public void writeCharacters(String text) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeCharacters(text);
		}

		@Override
		public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeCharacters(text, start, len);
		}

		@Override
		public void writeComment(String data) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeComment(data);
		}

		@Override
		public void writeDTD(String dtd) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeDTD(dtd);
		}

		@Override
		public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeDefaultNamespace(namespaceURI);
		}

		@Override
		public void writeEmptyElement(String localName) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeEmptyElement(localName);
		}

		@Override
		public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeEmptyElement(namespaceURI, localName);
		}

		@Override
		public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeEmptyElement(prefix, localName, namespaceURI);
		}

		@Override
		public void writeEndDocument() throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeEndDocument();
		}

		@Override
		public void writeEndElement() throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeEndElement();
		}

		@Override
		public void writeEntityRef(String name) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeEntityRef(name);
		}

		@Override
		public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeNamespace(prefix, namespaceURI);
		}

		@Override
		public void writeProcessingInstruction(String target) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeProcessingInstruction(target);
		}

		@Override
		public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeProcessingInstruction(target, data);
		}

		@Override
		public void writeStartDocument() throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeStartDocument();
		}

		@Override
		public void writeStartDocument(String version) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeStartDocument(version);
		}

		@Override
		public void writeStartDocument(String encoding, String version) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeStartDocument(encoding, version);
		}

		@Override
		public void writeStartElement(String localName) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeStartElement(localName);
		}

		@Override
		public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
			createStreamWriter();
			streamWriter.writeStartElement(namespaceURI, localName);
		}

		@Override
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
