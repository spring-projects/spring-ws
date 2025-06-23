/*
 * Copyright 2005-present the original author or authors.
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

import org.apache.axiom.blob.Blobs;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.ds.BlobOMDataSource;
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

	private final class DelegatingStreamWriter implements XMLStreamWriter {

		private final ByteArrayOutputStream baos = new ByteArrayOutputStream(BUF_SIZE);

		private final XMLStreamWriter delegate;

		private QName name;

		private String encoding = "UTF-8";

		private int elementDepth = 0;

		private boolean payloadAdded = false;

		@SuppressWarnings("deprecation")
		private DelegatingStreamWriter() {
			try {
				this.delegate = StAXUtils.createXMLStreamWriter(this.baos);
			}
			catch (XMLStreamException ex) {
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
			if (this.name == null) {
				this.name = new QName(localName);
			}
			this.elementDepth++;
			this.delegate.writeStartElement(localName);
		}

		@Override
		public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
			if (this.name == null) {
				this.name = new QName(namespaceURI, localName);
			}
			this.elementDepth++;
			this.delegate.writeStartElement(namespaceURI, localName);
		}

		@Override
		public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
			if (this.name == null) {
				this.name = new QName(namespaceURI, localName, prefix);
			}
			this.elementDepth++;
			this.delegate.writeStartElement(prefix, localName, namespaceURI);
		}

		@Override
		public void writeEndElement() throws XMLStreamException {
			this.elementDepth--;
			this.delegate.writeEndElement();
			addPayload();
		}

		private void addPayload() throws XMLStreamException {
			if (this.elementDepth <= 0 && !this.payloadAdded) {
				this.delegate.flush();
				if (this.baos.size() > 0) {
					byte[] buf = this.baos.toByteArray();
					OMDataSource dataSource = new BlobOMDataSource(Blobs.createBlob(buf), this.encoding);
					OMNamespace namespace = getAxiomFactory().createOMNamespace(this.name.getNamespaceURI(),
							this.name.getPrefix());
					OMElement payloadElement = getAxiomFactory().createOMElement(dataSource, this.name.getLocalPart(),
							namespace);
					getAxiomBody().addChild(payloadElement);
					this.payloadAdded = true;
				}
			}
		}

		@Override
		public void writeEmptyElement(String localName) throws XMLStreamException {
			if (this.name == null) {
				this.name = new QName(localName);
			}
			this.delegate.writeEmptyElement(localName);
			addPayload();
		}

		@Override
		public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
			if (this.name == null) {
				this.name = new QName(namespaceURI, localName);
			}
			this.delegate.writeEmptyElement(namespaceURI, localName);
			addPayload();
		}

		@Override
		public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
			if (this.name == null) {
				this.name = new QName(namespaceURI, localName, prefix);
			}
			this.delegate.writeEmptyElement(prefix, localName, namespaceURI);
			addPayload();
		}

		@Override
		public void writeEndDocument() throws XMLStreamException {
			this.elementDepth = 0;
			this.delegate.writeEndDocument();
			addPayload();
		}

		// Delegation

		@Override
		public void close() throws XMLStreamException {
			addPayload();
			this.delegate.close();
		}

		@Override
		public void flush() throws XMLStreamException {
			this.delegate.flush();
		}

		@Override
		public NamespaceContext getNamespaceContext() {
			return this.delegate.getNamespaceContext();
		}

		@Override
		public String getPrefix(String uri) throws XMLStreamException {
			return this.delegate.getPrefix(uri);
		}

		@Override
		public Object getProperty(String name) throws IllegalArgumentException {
			return this.delegate.getProperty(name);
		}

		@Override
		public void setDefaultNamespace(String uri) throws XMLStreamException {
			this.delegate.setDefaultNamespace(uri);
		}

		@Override
		public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
			this.delegate.setNamespaceContext(context);
		}

		@Override
		public void setPrefix(String prefix, String uri) throws XMLStreamException {
			this.delegate.setPrefix(prefix, uri);
		}

		@Override
		public void writeAttribute(String localName, String value) throws XMLStreamException {
			this.delegate.writeAttribute(localName, value);
		}

		@Override
		public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
			this.delegate.writeAttribute(namespaceURI, localName, value);
		}

		@Override
		public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
				throws XMLStreamException {
			this.delegate.writeAttribute(prefix, namespaceURI, localName, value);
		}

		@Override
		public void writeCData(String data) throws XMLStreamException {
			this.delegate.writeCData(data);
		}

		@Override
		public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
			this.delegate.writeCharacters(text, start, len);
		}

		@Override
		public void writeCharacters(String text) throws XMLStreamException {
			this.delegate.writeCharacters(text);
		}

		@Override
		public void writeComment(String data) throws XMLStreamException {
			this.delegate.writeComment(data);
		}

		@Override
		public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
			this.delegate.writeDefaultNamespace(namespaceURI);
		}

		@Override
		public void writeDTD(String dtd) throws XMLStreamException {
			this.delegate.writeDTD(dtd);
		}

		@Override
		public void writeEntityRef(String name) throws XMLStreamException {
			this.delegate.writeEntityRef(name);
		}

		@Override
		public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
			this.delegate.writeNamespace(prefix, namespaceURI);
		}

		@Override
		public void writeProcessingInstruction(String target) throws XMLStreamException {
			this.delegate.writeProcessingInstruction(target);
		}

		@Override
		public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
			this.delegate.writeProcessingInstruction(target, data);
		}

	}

}
