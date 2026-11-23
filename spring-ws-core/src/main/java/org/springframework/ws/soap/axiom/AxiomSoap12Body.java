/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import java.util.Locale;

import javax.xml.namespace.QName;

import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPProcessingException;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;

/**
 * Axiom-specific version of {@code org.springframework.ws.soap.Soap12Body}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomSoap12Body extends AxiomSoapBody implements Soap12Body {

	AxiomSoap12Body(SOAPBody axiomBody, SOAPFactory axiomFactory, boolean payloadCaching) {
		super(axiomBody, axiomFactory, payloadCaching);
	}

	@Override
	public Soap12Fault addMustUnderstandFault(String reason, @Nullable Locale locale) {
		Assert.notNull(locale, "No locale given");
		SOAPFault fault = addStandardFault(SOAP12Constants.FAULT_CODE_MUST_UNDERSTAND, reason, locale);
		return new AxiomSoap12Fault(fault, getAxiomFactory());
	}

	@Override
	public Soap12Fault addClientOrSenderFault(String reason, @Nullable Locale locale) {
		Assert.notNull(locale, "No locale given");
		SOAPFault fault = addStandardFault(SOAP12Constants.FAULT_CODE_SENDER, reason, locale);
		return new AxiomSoap12Fault(fault, getAxiomFactory());
	}

	@Override
	public Soap12Fault addServerOrReceiverFault(String reason, @Nullable Locale locale) {
		Assert.notNull(locale, "No locale given");
		SOAPFault fault = addStandardFault(SOAP12Constants.FAULT_CODE_RECEIVER, reason, locale);
		return new AxiomSoap12Fault(fault, getAxiomFactory());
	}

	@Override
	public Soap12Fault addVersionMismatchFault(String reason, @Nullable Locale locale) {
		Assert.notNull(locale, "No locale given");
		SOAPFault fault = addStandardFault(SOAP12Constants.FAULT_CODE_VERSION_MISMATCH, reason, locale);
		return new AxiomSoap12Fault(fault, getAxiomFactory());
	}

	@Override
	public Soap12Fault addDataEncodingUnknownFault(QName[] subcodes, String reason, Locale locale) {
		Assert.notNull(locale, "No locale given");
		SOAPFault fault = addStandardFault(SOAP12Constants.FAULT_CODE_DATA_ENCODING_UNKNOWN, reason, locale);
		return new AxiomSoap12Fault(fault, getAxiomFactory());
	}

	private SOAPFault addStandardFault(String localName, String faultStringOrReason, @Nullable Locale locale) {
		Assert.notNull(faultStringOrReason, "No faultStringOrReason given");
		try {
			AxiomUtils.removeContents(getAxiomBody());
			SOAPFault fault = getAxiomFactory().createSOAPFault(getAxiomBody());
			SOAPFaultCode code = getAxiomFactory().createSOAPFaultCode(fault);
			SOAPFaultValue value = getAxiomFactory().createSOAPFaultValue(code);
			value.setText(fault.getNamespace().getPrefix() + ":" + localName);
			SOAPFaultReason reason = getAxiomFactory().createSOAPFaultReason(fault);
			SOAPFaultText text = getAxiomFactory().createSOAPFaultText(reason);
			if (locale != null) {
				text.setLang(AxiomUtils.toLanguage(locale));
			}
			text.setText(faultStringOrReason);
			return fault;
		}
		catch (SOAPProcessingException ex) {
			throw new AxiomSoapFaultException(ex);
		}
	}

	@Override
	public @Nullable Soap12Fault getFault() {
		SOAPFault axiomFault = getAxiomBody().getFault();
		return (axiomFault != null) ? new AxiomSoap12Fault(axiomFault, getAxiomFactory()) : null;
	}

}
