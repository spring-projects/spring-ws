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

package org.springframework.ws.soap.saaj.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.TransportConstants;

/**
 * Collection of generic utility methods to work with SAAJ. Includes conversion from SAAJ {@link Name} objects to
 * {@link QName}s and vice-versa, and SAAJ version checking.
 *
 * @author Arjen Poutsma
 * @see Name
 * @see QName
 * @since 1.0.0
 */
public abstract class SaajUtils {

	public static final int SAAJ_11 = 0;

	public static final int SAAJ_12 = 1;

	public static final int SAAJ_13 = 2;

	private static int saajVersion = SAAJ_13;

	/**
	 * Gets the SAAJ version. Returns {@link #SAAJ_13} as of Spring-WS 2.2.
	 *
	 * @return a code comparable to the SAAJ_XX codes in this class
	 */
	public static int getSaajVersion() {
		return saajVersion;
	}

	/**
	 * Gets the SAAJ version for the specified {@link SOAPMessage}. Returns {@link #SAAJ_13} as of Spring-WS 2.2.
	 *
	 * @return a code comparable to the SAAJ_XX codes in this class
	 * @see #SAAJ_11
	 * @see #SAAJ_12
	 * @see #SAAJ_13
	 */
	public static int getSaajVersion(SOAPMessage soapMessage) throws SOAPException {
		Assert.notNull(soapMessage, "'soapMessage' must not be null");
		SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
		return getSaajVersion(soapEnvelope);
	}

	/**
	 * Gets the SAAJ version for the specified {@link javax.xml.soap.SOAPElement}. Returns {@link #SAAJ_13} as of
	 * Spring-WS 2.2.
	 *
	 * @return a code comparable to the SAAJ_XX codes in this class
	 * @see #SAAJ_11
	 * @see #SAAJ_12
	 * @see #SAAJ_13
	 */
	public static int getSaajVersion(SOAPElement soapElement) {
		return SAAJ_13;
	}

	/**
	 * Returns the SAAJ version as a String. The returned string will be "{@code SAAJ 1.3}", "{@code SAAJ
	 * 1.2}", or "{@code SAAJ 1.1}".
	 *
	 * @return a string representation of the SAAJ version
	 * @see #getSaajVersion()
	 */
	public static String getSaajVersionString() {
		return getSaajVersionString(saajVersion);
	}

	private static String getSaajVersionString(int saajVersion) {
		if (saajVersion >= SaajUtils.SAAJ_13) {
			return "SAAJ 1.3";
		} else if (saajVersion == SaajUtils.SAAJ_12) {
			return "SAAJ 1.2";
		} else if (saajVersion == SaajUtils.SAAJ_11) {
			return "SAAJ 1.1";
		} else {
			return "";
		}
	}

	/**
	 * Converts a {@link QName} to a {@link Name}. A {@link SOAPElement} is required to resolve namespaces.
	 *
	 * @param qName the {@code QName} to convert
	 * @param resolveElement a {@code SOAPElement} used to resolve namespaces to prefixes
	 * @return the converted SAAJ Name
	 * @throws SOAPException if conversion is unsuccessful
	 * @throws IllegalArgumentException if {@code qName} is not fully qualified
	 */
	public static Name toName(QName qName, SOAPElement resolveElement) throws SOAPException {
		String qNamePrefix = qName.getPrefix();
		SOAPEnvelope envelope = getEnvelope(resolveElement);
		if (StringUtils.hasLength(qName.getNamespaceURI()) && StringUtils.hasLength(qNamePrefix)) {
			return envelope.createName(qName.getLocalPart(), qNamePrefix, qName.getNamespaceURI());
		} else if (StringUtils.hasLength(qName.getNamespaceURI())) {
			Iterator<?> prefixes;
			if (getSaajVersion(resolveElement) == SAAJ_11) {
				prefixes = resolveElement.getNamespacePrefixes();
			} else {
				prefixes = resolveElement.getVisibleNamespacePrefixes();
			}
			while (prefixes.hasNext()) {
				String prefix = (String) prefixes.next();
				if (qName.getNamespaceURI().equals(resolveElement.getNamespaceURI(prefix))) {
					return envelope.createName(qName.getLocalPart(), prefix, qName.getNamespaceURI());
				}
			}
			return envelope.createName(qName.getLocalPart(), "", qName.getNamespaceURI());
		} else {
			return envelope.createName(qName.getLocalPart());
		}
	}

	/**
	 * Converts a {@code javax.xml.soap.Name} to a {@code javax.xml.namespace.QName}.
	 *
	 * @param name the {@code Name} to convert
	 * @return the converted {@code QName}
	 */
	public static QName toQName(Name name) {
		if (StringUtils.hasLength(name.getURI()) && StringUtils.hasLength(name.getPrefix())) {
			return new QName(name.getURI(), name.getLocalName(), name.getPrefix());
		} else if (StringUtils.hasLength(name.getURI())) {
			return new QName(name.getURI(), name.getLocalName());
		} else {
			return new QName(name.getLocalName());
		}
	}

	/**
	 * Loads a SAAJ {@code SOAPMessage} from the given resource with a given message factory.
	 *
	 * @param resource the resource to read from
	 * @param messageFactory SAAJ message factory used to construct the message
	 * @return the loaded SAAJ message
	 * @throws SOAPException if the message cannot be constructed
	 * @throws IOException if the input stream resource cannot be loaded
	 */
	public static SOAPMessage loadMessage(Resource resource, MessageFactory messageFactory)
			throws SOAPException, IOException {
		InputStream is = resource.getInputStream();
		try {
			MimeHeaders mimeHeaders = new MimeHeaders();
			mimeHeaders.addHeader(TransportConstants.HEADER_CONTENT_TYPE, "text/xml");
			mimeHeaders.addHeader(TransportConstants.HEADER_CONTENT_LENGTH, Long.toString(resource.getFile().length()));
			return messageFactory.createMessage(mimeHeaders, is);
		} finally {
			is.close();
		}
	}

	/**
	 * Returns the SAAJ {@code SOAPEnvelope} for the given element.
	 *
	 * @param element the element to return the envelope from
	 * @return the envelope, or {@code null} if not found
	 */
	public static SOAPEnvelope getEnvelope(SOAPElement element) {
		Assert.notNull(element, "Element should not be null");
		do {
			if (element instanceof SOAPEnvelope) {
				return (SOAPEnvelope) element;
			}
			element = element.getParentElement();
		} while (element != null);
		return null;
	}

	/**
	 * Returns the first child element of the given body.
	 */
	public static SOAPElement getFirstBodyElement(SOAPBody body) {
		for (Iterator<?> iterator = body.getChildElements(); iterator.hasNext();) {
			Object child = iterator.next();
			if (child instanceof SOAPElement) {
				return (SOAPElement) child;
			}
		}
		return null;
	}

}
