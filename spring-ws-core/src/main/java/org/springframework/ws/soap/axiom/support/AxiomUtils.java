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

package org.springframework.ws.soap.axiom.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.springframework.util.StringUtils;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Collection of generic utility methods to work with Axiom. Includes conversion from {@code OMNamespace}s to
 * {@code QName}s.
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
	 * Converts a {@code javax.xml.namespace.QName} to a {@code org.apache.axiom.om.OMNamespace}. A {@code OMElement} is
	 * used to resolve the namespace, or to declare a new one.
	 *
	 * @param qName the {@code QName} to convert
	 * @param resolveElement the element used to resolve the Q
	 * @return the converted SAAJ Name
	 * @throws OMException if conversion is unsuccessful
	 * @throws IllegalArgumentException if {@code qName} is not fully qualified
	 */
	public static OMNamespace toNamespace(QName qName, OMElement resolveElement) throws OMException {
		String prefix = qName.getPrefix();
		if (StringUtils.hasLength(qName.getNamespaceURI()) && StringUtils.hasLength(prefix)) {
			return resolveElement.declareNamespace(qName.getNamespaceURI(), prefix);
		} else if (StringUtils.hasLength(qName.getNamespaceURI())) {
			// check for existing namespace, and declare if necessary
			return resolveElement.declareNamespace(qName.getNamespaceURI(), "");
		} else {
			throw new IllegalArgumentException("qName [" + qName + "] does not contain a namespace");
		}
	}

	/**
	 * Converts the given locale to a {@code xml:lang} string, as used in Axiom Faults.
	 *
	 * @param locale the locale
	 * @return the language string
	 */
	public static String toLanguage(Locale locale) {
		return locale.toString().replace('_', '-');
	}

	/**
	 * Converts the given locale to a {@code xml:lang} string, as used in Axiom Faults.
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
			iterator.next();
			iterator.remove();
		}
	}

	/**
	 * Converts a given AXIOM {@link org.apache.axiom.soap.SOAPEnvelope} to a {@link Document}.
	 *
	 * @param envelope the SOAP envelope to be converted
	 * @return the converted document
	 * @throws IllegalArgumentException in case of errors
	 */
	public static Document toDocument(SOAPEnvelope envelope) {
		try {
			if (envelope instanceof Element) {
				return ((Element) envelope).getOwnerDocument();
			} else {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				envelope.build();
				envelope.serialize(bos);

				ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
				documentBuilderFactory.setNamespaceAware(true);
				return documentBuilderFactory.newDocumentBuilder().parse(bis);
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException("Error in converting SOAP Envelope to Document", ex);
		}
	}

	/**
	 * Converts a given {@link Document} to an AXIOM {@link org.apache.axiom.soap.SOAPEnvelope}.
	 *
	 * @param document the document to be converted
	 * @return the converted envelope
	 * @throws IllegalArgumentException in case of errors
	 */
	public static SOAPEnvelope toEnvelope(Document document) {
		return OMXMLBuilderFactory.createSOAPModelBuilder(new DOMSource(document)).getSOAPEnvelope();
	}

}
