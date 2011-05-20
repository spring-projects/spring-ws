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

package org.springframework.ws.soap.axiom.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.QNameUtils;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * Collection of generic utility methods to work with Axiom. Includes conversion from <code>OMNamespace</code>s to
 * <code>QName</code>s.
 *
 * @author Arjen Poutsma
 * @author Tareq Abed Rabbo
 * @see org.apache.axiom.om.OMNamespace
 * @see javax.xml.namespace.QName
 * @since 1.0.0
 */
@SuppressWarnings("Since15")
public abstract class AxiomUtils {

    /**
     * Converts a <code>javax.xml.namespace.QName</code> to a <code>org.apache.axiom.om.OMNamespace</code>. A
     * <code>OMElement</code> is used to resolve the namespace, or to declare a new one.
     *
     * @param qName          the <code>QName</code> to convert
     * @param resolveElement the element used to resolve the Q
     * @return the converted SAAJ Name
     * @throws OMException              if conversion is unsuccessful
     * @throws IllegalArgumentException if <code>qName</code> is not fully qualified
     */
    public static OMNamespace toNamespace(QName qName, OMElement resolveElement) throws OMException {
        String prefix = QNameUtils.getPrefix(qName);
        if (StringUtils.hasLength(qName.getNamespaceURI()) && StringUtils.hasLength(prefix)) {
            return resolveElement.declareNamespace(qName.getNamespaceURI(), prefix);
        }
        else if (StringUtils.hasLength(qName.getNamespaceURI())) {
            // check for existing namespace, and declare if necessary
            return resolveElement.declareNamespace(qName.getNamespaceURI(), "");
        }
        else {
            throw new IllegalArgumentException("qName [" + qName + "] does not contain a namespace");
        }
    }

    /**
     * Converts the given locale to a <code>xml:lang</code> string, as used in Axiom Faults.
     *
     * @param locale the locale
     * @return the language string
     */
    public static String toLanguage(Locale locale) {
        return locale.toString().replace('_', '-');
    }

    /**
     * Converts the given locale to a <code>xml:lang</code> string, as used in Axiom Faults.
     *
     * @param language the language string
     * @return the locale
     */
    public static Locale toLocale(String language) {
        language = language.replace('-', '_');
        return StringUtils.parseLocaleString(language);
    }

    /** Removes the contents (i.e. children) of the container. */
    public static void removeContents(OMContainer container) {
        for (Iterator<?> iterator = container.getChildren(); iterator.hasNext();) {
            OMNode child = (OMNode) iterator.next();
            child.detach();
        }
    }

    /**
     * Converts a given AXIOM {@link org.apache.axiom.soap.SOAPEnvelope} to a {@link Document}.
     *
     * @param envelope the SOAP envelope to be converted
     * @return the converted document
     * @throws IllegalArgumentException in case of errors
     * @see org.apache.rampart.util.Axis2Util.getDocumentFromSOAPEnvelope(SOAPEnvelope, boolean)
     */
    public static Document toDocument(SOAPEnvelope envelope) {
        try {
            if (envelope instanceof Element) {
                return ((Element) envelope).getOwnerDocument();
            }
            else {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                envelope.build();
                envelope.serialize(bos);

                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);
                return documentBuilderFactory.newDocumentBuilder().parse(bis);
            }
        }
        catch (Exception ex) {
            throw new IllegalArgumentException("Error in converting SOAP Envelope to Document", ex);
        }
    }

    /**
     * Converts a given {@link Document} to an AXIOM {@link org.apache.axiom.soap.SOAPEnvelope}.
     *
     * @param document the document to be converted
     * @return the converted envelope
     * @throws IllegalArgumentException in case of errors
     * @see org.apache.rampart.util.Axis2Util.getSOAPEnvelopeFromDOMDocument(Document, boolean)
     */
    public static SOAPEnvelope toEnvelope(Document document) {
        try {
            DOMImplementation implementation = document.getImplementation();
            Assert.isInstanceOf(DOMImplementationLS.class, implementation);

            DOMImplementationLS loadSaveImplementation = (DOMImplementationLS) implementation;
            LSOutput output = loadSaveImplementation.createLSOutput();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            output.setByteStream(bos);

            LSSerializer serializer = loadSaveImplementation.createLSSerializer();
            serializer.write(document, output);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

            StAXSOAPModelBuilder stAXSOAPModelBuilder =
                    new StAXSOAPModelBuilder(XMLInputFactory.newInstance().createXMLStreamReader(bis), null);
            SOAPEnvelope envelope = stAXSOAPModelBuilder.getSOAPEnvelope();

            // Necessary to build a correct Axiom tree, see SWS-483
            envelope.serialize(new NullOutputStream());

            return envelope;
        }
        catch (Exception ex) {
            IllegalArgumentException iaex =
                    new IllegalArgumentException("Error in converting Document to SOAP Envelope");
            iaex.initCause(ex);
            throw iaex;
        }
    }

    /** OutputStream that does nothing. */
    private static class NullOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }
    }

}
