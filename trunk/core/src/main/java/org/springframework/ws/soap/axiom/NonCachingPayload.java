/*
 * Copyright 2008 the original author or authors.
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

import org.springframework.xml.transform.StaxResult;

/**
 * Non-caching payload in Axiom.
 *
 * @author Jim Cummings
 * @author Arjen Poutsma
 * @since 1.5.2
 */
class NonCachingPayload extends Payload {

    private static final int BUF_SIZE = 1024;

    NonCachingPayload(SOAPBody axiomBody, SOAPFactory axiomFactory) {
        super(axiomBody, axiomFactory);
    }

    public Result getResultInternal() {
        return new StaxResult(new DelegatingStreamWriter());
    }

    protected XMLStreamReader getStreamReader(OMElement payloadElement) {
        return payloadElement.getXMLStreamReaderWithoutCaching();
    }

    private class DelegatingStreamWriter implements XMLStreamWriter {

        private final ByteArrayOutputStream baos = new ByteArrayOutputStream(BUF_SIZE);

        private final XMLStreamWriter delegate;

        private QName name;

        private String encoding = "UTF-8";

        private int elementDepth = 0;

        private DelegatingStreamWriter() {
            try {
                this.delegate = StAXUtils.createXMLStreamWriter(baos);
            }
            catch (XMLStreamException ex) {
                throw new AxiomSoapBodyException("Could not determine payload root element", ex);
            }
        }

        public void writeStartDocument() throws XMLStreamException {
            // ignored
        }

        public void writeStartDocument(String version) throws XMLStreamException {
            // ignored
        }

        public void writeStartDocument(String encoding, String version) throws XMLStreamException {
            this.encoding = encoding;
        }

        public void writeEndDocument() throws XMLStreamException {
            // ignored
        }

        public void writeStartElement(String localName) throws XMLStreamException {
            if (name == null) {
                name = new QName(localName);
            }
            elementDepth++;
            delegate.writeStartElement(localName);
        }

        public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
            if (name == null) {
                name = new QName(namespaceURI, localName);
            }
            elementDepth++;
            delegate.writeStartElement(namespaceURI, localName);
        }

        public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            if (name == null) {
                name = new QName(namespaceURI, localName, prefix);
            }
            elementDepth++;
            delegate.writeStartElement(prefix, localName, namespaceURI);
        }

        public void writeEndElement() throws XMLStreamException {
            elementDepth--;
            delegate.writeEndElement();
            if (elementDepth <= 0) {
                addPayload();
            }
        }

        private void addPayload() throws XMLStreamException {
            delegate.flush();
            if (baos.size() > 0) {
                byte[] buf = baos.toByteArray();
                OMDataSource dataSource = new ByteArrayDataSource(buf, encoding);
                OMNamespace namespace = getAxiomFactory().createOMNamespace(name.getNamespaceURI(), name.getPrefix());
                OMElement payloadElement =
                        getAxiomFactory().createOMElement(dataSource, name.getLocalPart(), namespace);
                getAxiomBody().addChild(payloadElement);
            }
        }

        public void writeEmptyElement(String localName) throws XMLStreamException {
            if (name == null) {
                name = new QName(localName);
            }
            delegate.writeEmptyElement(localName);
        }

        public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
            if (name == null) {
                name = new QName(namespaceURI, localName);
            }
            delegate.writeEmptyElement(namespaceURI, localName);
        }

        public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            if (name == null) {
                name = new QName(namespaceURI, localName, prefix);
            }
            delegate.writeEmptyElement(prefix, localName, namespaceURI);
        }

        // Delegation

        public void close() throws XMLStreamException {
            delegate.close();
        }

        public void flush() throws XMLStreamException {
            delegate.flush();
        }

        public NamespaceContext getNamespaceContext() {
            return delegate.getNamespaceContext();
        }

        public String getPrefix(String uri) throws XMLStreamException {
            return delegate.getPrefix(uri);
        }

        public Object getProperty(String name) throws IllegalArgumentException {
            return delegate.getProperty(name);
        }

        public void setDefaultNamespace(String uri) throws XMLStreamException {
            delegate.setDefaultNamespace(uri);
        }

        public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
            delegate.setNamespaceContext(context);
        }

        public void setPrefix(String prefix, String uri) throws XMLStreamException {
            delegate.setPrefix(prefix, uri);
        }

        public void writeAttribute(String localName, String value) throws XMLStreamException {
            delegate.writeAttribute(localName, value);
        }

        public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
            delegate.writeAttribute(namespaceURI, localName, value);
        }

        public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
                throws XMLStreamException {
            delegate.writeAttribute(prefix, namespaceURI, localName, value);
        }

        public void writeCData(String data) throws XMLStreamException {
            delegate.writeCData(data);
        }

        public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
            delegate.writeCharacters(text, start, len);
        }

        public void writeCharacters(String text) throws XMLStreamException {
            delegate.writeCharacters(text);
        }

        public void writeComment(String data) throws XMLStreamException {
            delegate.writeComment(data);
        }

        public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
            delegate.writeDefaultNamespace(namespaceURI);
        }

        public void writeDTD(String dtd) throws XMLStreamException {
            delegate.writeDTD(dtd);
        }

        public void writeEntityRef(String name) throws XMLStreamException {
            delegate.writeEntityRef(name);
        }

        public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
            delegate.writeNamespace(prefix, namespaceURI);
        }

        public void writeProcessingInstruction(String target) throws XMLStreamException {
            delegate.writeProcessingInstruction(target);
        }

        public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
            delegate.writeProcessingInstruction(target, data);
        }

    }
}
