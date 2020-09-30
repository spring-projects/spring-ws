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

package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;

/**
 * Axiom-Specific version of {@code org.springframework.ws.soap.SoapEnvelope}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomSoapEnvelope extends AxiomSoapElement implements SoapEnvelope {

	boolean payloadCaching;

	private AxiomSoapBody body;

	private final boolean langAttributeOnSoap11FaultString;

	AxiomSoapEnvelope(SOAPEnvelope axiomEnvelope, SOAPFactory axiomFactory, boolean payloadCaching,
			boolean langAttributeOnSoap11FaultString) {
		super(axiomEnvelope, axiomFactory);
		this.payloadCaching = payloadCaching;
		this.langAttributeOnSoap11FaultString = langAttributeOnSoap11FaultString;
	}

	@Override
	public SoapHeader getHeader() {
		try {
			if (getAxiomEnvelope().getHeader() == null) {
				return null;
			} else {
				SOAPHeader axiomHeader = getAxiomEnvelope().getHeader();
				String namespaceURI = getAxiomEnvelope().getNamespace().getNamespaceURI();
				if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
					return new AxiomSoap11Header(axiomHeader, getAxiomFactory());
				} else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
					return new AxiomSoap12Header(axiomHeader, getAxiomFactory());
				} else {
					throw new AxiomSoapEnvelopeException("Unknown SOAP namespace \"" + namespaceURI + "\"");
				}
			}
		} catch (OMException ex) {
			throw new AxiomSoapHeaderException(ex);
		}
	}

	@Override
	public SoapBody getBody() {
		if (body == null) {
			try {
				SOAPBody axiomBody = getAxiomEnvelope().getBody();
				String namespaceURI = getAxiomEnvelope().getNamespace().getNamespaceURI();
				if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
					body = new AxiomSoap11Body(axiomBody, getAxiomFactory(), payloadCaching, langAttributeOnSoap11FaultString);
				} else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
					body = new AxiomSoap12Body(axiomBody, getAxiomFactory(), payloadCaching);
				} else {
					throw new AxiomSoapEnvelopeException("Unknown SOAP namespace \"" + namespaceURI + "\"");
				}
			} catch (OMException ex) {
				throw new AxiomSoapBodyException(ex);
			}
		}
		return body;
	}

	protected SOAPEnvelope getAxiomEnvelope() {
		return (SOAPEnvelope) getAxiomElement();
	}

}
