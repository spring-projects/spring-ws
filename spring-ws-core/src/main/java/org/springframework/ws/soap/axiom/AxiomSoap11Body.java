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

import java.util.Locale;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPProcessingException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap11.Soap11Fault;

/**
 * Axiom-specific version of {@code org.springframework.ws.soap.Soap11Body}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomSoap11Body extends AxiomSoapBody implements Soap11Body {

	private final boolean langAttributeOnSoap11FaultString;

	AxiomSoap11Body(SOAPBody axiomBody, SOAPFactory axiomFactory, boolean payloadCaching,
			boolean langAttributeOnSoap11FaultString) {
		super(axiomBody, axiomFactory, payloadCaching);
		this.langAttributeOnSoap11FaultString = langAttributeOnSoap11FaultString;
	}

	@Override
	public Soap11Fault addMustUnderstandFault(String faultString, Locale locale) {
		SOAPFault fault = addStandardFault(SOAP11Constants.FAULT_CODE_MUST_UNDERSTAND, faultString, locale);
		return new AxiomSoap11Fault(fault, getAxiomFactory());
	}

	@Override
	public Soap11Fault addClientOrSenderFault(String faultString, Locale locale) {
		SOAPFault fault = addStandardFault(SOAP11Constants.FAULT_CODE_SENDER, faultString, locale);
		return new AxiomSoap11Fault(fault, getAxiomFactory());
	}

	@Override
	public Soap11Fault addServerOrReceiverFault(String faultString, Locale locale) {
		SOAPFault fault = addStandardFault(SOAP11Constants.FAULT_CODE_RECEIVER, faultString, locale);
		return new AxiomSoap11Fault(fault, getAxiomFactory());
	}

	@Override
	public Soap11Fault addVersionMismatchFault(String faultString, Locale locale) {
		SOAPFault fault = addStandardFault(SOAP11Constants.FAULT_CODE_VERSION_MISMATCH, faultString, locale);
		return new AxiomSoap11Fault(fault, getAxiomFactory());
	}

	@Override
	public Soap11Fault addFault(QName code, String faultString, Locale faultStringLocale) {
		Assert.notNull(code, "No faultCode given");
		Assert.hasLength(faultString, "faultString cannot be empty");
		if (!StringUtils.hasLength(code.getNamespaceURI())) {
			throw new IllegalArgumentException(
					"A fault code with namespace and local part must be specific for a custom fault code");
		}
		if (!langAttributeOnSoap11FaultString) {
			faultStringLocale = null;
		}
		try {
			AxiomUtils.removeContents(getAxiomBody());
			SOAPFault fault = getAxiomFactory().createSOAPFault(getAxiomBody());
			SOAPFaultCode faultCode = getAxiomFactory().createSOAPFaultCode(fault);
			setValueText(code, fault, faultCode);
			SOAPFaultReason faultReason = getAxiomFactory().createSOAPFaultReason(fault);
			if (faultStringLocale != null) {
				addLangAttribute(faultStringLocale, faultReason);
			}
			faultReason.setText(faultString);
			return new AxiomSoap11Fault(fault, getAxiomFactory());

		} catch (SOAPProcessingException ex) {
			throw new AxiomSoapFaultException(ex);
		}
	}

	private void setValueText(QName code, SOAPFault fault, SOAPFaultCode faultCode) {
		String prefix = code.getPrefix();
		if (StringUtils.hasLength(code.getNamespaceURI()) && StringUtils.hasLength(prefix)) {
			OMNamespace namespace = fault.findNamespaceURI(prefix);
			if (namespace == null) {
				fault.declareNamespace(code.getNamespaceURI(), prefix);
			}
		} else if (StringUtils.hasLength(code.getNamespaceURI())) {
			OMNamespace namespace = fault.findNamespace(code.getNamespaceURI(), null);
			if (namespace == null) {
				namespace = fault.declareNamespace(code.getNamespaceURI(), "");
			}
			code = new QName(code.getNamespaceURI(), code.getLocalPart(), namespace.getPrefix());
		}
		faultCode.setText(code);
	}

	private SOAPFault addStandardFault(String localName, String faultString, Locale locale) {
		Assert.notNull(faultString, "No faultString given");
		try {
			AxiomUtils.removeContents(getAxiomBody());
			SOAPFault fault = getAxiomFactory().createSOAPFault(getAxiomBody());
			SOAPFaultCode faultCode = getAxiomFactory().createSOAPFaultCode(fault);
			faultCode.setText(new QName(fault.getNamespace().getNamespaceURI(), localName, fault.getNamespace().getPrefix()));
			SOAPFaultReason faultReason = getAxiomFactory().createSOAPFaultReason(fault);
			if (locale != null) {
				addLangAttribute(locale, faultReason);
			}
			faultReason.setText(faultString);
			return fault;
		} catch (SOAPProcessingException ex) {
			throw new AxiomSoapFaultException(ex);
		}
	}

	private void addLangAttribute(Locale locale, SOAPFaultReason faultReason) {
		OMNamespace xmlNamespace = getAxiomFactory().createOMNamespace("http://www.w3.org/XML/1998/namespace", "xml");
		OMAttribute langAttribute = getAxiomFactory().createOMAttribute("lang", xmlNamespace,
				AxiomUtils.toLanguage(locale));
		faultReason.addAttribute(langAttribute);
	}

	@Override
	public Soap11Fault getFault() {
		SOAPFault axiomFault = getAxiomBody().getFault();
		return axiomFault != null ? new AxiomSoap11Fault(axiomFault, getAxiomFactory()) : null;
	}

}
