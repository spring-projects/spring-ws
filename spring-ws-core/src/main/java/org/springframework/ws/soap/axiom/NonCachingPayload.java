/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.axiom;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.ds.ByteArrayDataSource;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.springframework.util.xml.StaxUtils;

/**
 * Non-caching payload in Axiom.
 *
 * @author Jim Cummings
 * @author Arjen Poutsma
 * @since 1.5.2
 */
class NonCachingPayload extends AbstractPayload {

	private static final int BUF_SIZE = 1024;

	NonCachingPayload(SOAPBody axiomBody, SOAPFactory axiomFactory) {
		super(axiomBody, axiomFactory);
	}

	@Override
	public Result getResultInternal() {
		return StaxUtils.createCustomStaxResult(new DelegatingStreamWriter());
	}

	@Override
	protected XMLStreamReader getStreamReader(OMElement payloadElement) {
		return payloadElement.getXMLStreamReaderWithoutCaching();
	}

	private class DelegatingStreamWriter implements XMLStreamWriter {

		private final ByteArrayOutputStream baos = new ByteArrayOutputStream(BUF_SIZE);

		private final XMLStreamWriter delegate;

		private QName name;

		private String encoding = "UTF-8";

		private int elementDepth = 0;

		private boolean payloadAdded = false;

		private DelegatingStreamWriter() {
			try {
				this.delegate = StAXUtils.createXMLStreamWriter(baos);
			} catch (XMLStreamException ex) {
				throw new AxiomSoapBodyException("Could not determine payload root element", ex);
			}
		}

		@Override
		public void writeStartDocument() throws XMLStreamException {
			// ignored
		}

		@Override
		public void writeStartDocument(String version) throws XMLStreamException {
			// ignored
		}

		@Override
		public void writeStartDocument(String encoding, String version) throws XMLStreamException {
			this.encoding = encoding;
		}

		@Override
		public void writeStartElement(String localName) throws XMLStreamException {
			if (name == null) {
				name = new QName(localName);
			}
			elementDepth++;
			delegate.writeStartElement(localName);
		}

		@Override
		public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
			if (name == null) {
				name = new QName(namespaceURI, localName);
			}
			elementDepth++;
			delegate.writeStartElement(namespaceURI, localName);
		}

		@Override
		public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
			if (name == null) {
				name = new QName(namespaceURI, localName, prefix);
			}
			elementDepth++;
			delegate.writeStartElement(prefix, localName, namespaceURI);
		}

		@Override
		public void writeEndElement() throws XMLStreamException {
			elementDepth--;
			delegate.writeEndElement();
			addPayload();
		}

		private void addPayload() throws XMLStreamException {
			if (elementDepth <= 0 && !payloadAdded) {
				delegate.flush();
				if (baos.size() > 0) {
					byte[] buf = baos.toByteArray();
					OMDataSource dataSource = new ByteArrayDataSource(buf, encoding);
					OMNamespace namespace = getAxiomFactory().createOMNamespace(name.getNamespaceURI(), name.getPrefix());
					OMElement payloadElement = getAxiomFactory().createOMElement(dataSource, name.getLocalPart(), namespace);
					getAxiomBody().addChild(payloadElement);
					payloadAdded = true;
				}
			}
		}

		@Override
		public void writeEmptyElement(String localName) throws XMLStreamException {
			if (name == null) {
				name = new QName(localName);
			}
			delegate.writeEmptyElement(localName);
			addPayload();
		}

		@Override
		public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
			if (name == null) {
				name = new QName(namespaceURI, localName);
			}
			delegate.writeEmptyElement(namespaceURI, localName);
			addPayload();
		}

		@Override
		public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
			if (name == null) {
				name = new QName(namespaceURI, localName, prefix);
			}
			delegate.writeEmptyElement(prefix, localName, namespaceURI);
			addPayload();
		}

		@Override
		public void writeEndDocument() throws XMLStreamException {
			elementDepth = 0;
			delegate.writeEndDocument();
			addPayload();
		}

		// Delegation

		@Override
		public void close() throws XMLStreamException {
			addPayload();
			delegate.close();
		}

		@Override
		public void flush() throws XMLStreamException {
			delegate.flush();
		}

		@Override
		public NamespaceContext getNamespaceContext() {
			return delegate.getNamespaceContext();
		}

		@Override
		public String getPrefix(String uri) throws XMLStreamException {
			return delegate.getPrefix(uri);
		}

		@Override
		public Object getProperty(String name) throws IllegalArgumentException {
			return delegate.getProperty(name);
		}

		@Override
		public void setDefaultNamespace(String uri) throws XMLStreamException {
			delegate.setDefaultNamespace(uri);
		}

		@Override
		public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
			delegate.setNamespaceContext(context);
		}

		@Override
		public void setPrefix(String prefix, String uri) throws XMLStreamException {
			delegate.setPrefix(prefix, uri);
		}

		@Override
		public void writeAttribute(String localName, String value) throws XMLStreamException {
			delegate.writeAttribute(localName, value);
		}

		@Override
		public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
			delegate.writeAttribute(namespaceURI, localName, value);
		}

		@Override
		public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
				throws XMLStreamException {
			delegate.writeAttribute(prefix, namespaceURI, localName, value);
		}

		@Override
		public void writeCData(String data) throws XMLStreamException {
			delegate.writeCData(data);
		}

		@Override
		public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
			delegate.writeCharacters(text, start, len);
		}

		@Override
		public void writeCharacters(String text) throws XMLStreamException {
			delegate.writeCharacters(text);
		}

		@Override
		public void writeComment(String data) throws XMLStreamException {
			delegate.writeComment(data);
		}

		@Override
		public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
			delegate.writeDefaultNamespace(namespaceURI);
		}

		@Override
		public void writeDTD(String dtd) throws XMLStreamException {
			delegate.writeDTD(dtd);
		}

		@Override
		public void writeEntityRef(String name) throws XMLStreamException {
			delegate.writeEntityRef(name);
		}

		@Override
		public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
			delegate.writeNamespace(prefix, namespaceURI);
		}

		@Override
		public void writeProcessingInstruction(String target) throws XMLStreamException {
			delegate.writeProcessingInstruction(target);
		}

		@Override
		public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
			delegate.writeProcessingInstruction(target, data);
		}

	}
}
