/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import java.util.Locale;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.ws.soap.soap11.Soap11Fault;

/**
 * Axiom-specific version of {@code org.springframework.ws.soap.Soap11Fault}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomSoap11Fault extends AxiomSoapFault implements Soap11Fault {

	AxiomSoap11Fault(SOAPFault axiomFault, SOAPFactory axiomFactory) {
		super(axiomFault, axiomFactory);
	}

	@Override
	public QName getFaultCode() {
		return getAxiomFault().getCode().getTextAsQName();
	}

	@Override
	public @Nullable String getFaultStringOrReason() {
		if (getAxiomFault().getReason() != null) {
			return getAxiomFault().getReason().getText();
		}
		return null;
	}

	@Override
	public @Nullable Locale getFaultStringLocale() {
		if (getAxiomFault().getReason() != null) {
			OMAttribute langAttribute = getAxiomFault().getReason()
				.getAttribute(new QName("http://www.w3.org/XML/1998/namespace", "lang"));
			if (langAttribute != null) {
				String xmlLangString = langAttribute.getAttributeValue();
				if (xmlLangString != null) {
					return AxiomUtils.toLocale(xmlLangString);
				}

			}
		}
		return null;
	}

}
