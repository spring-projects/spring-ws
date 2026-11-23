/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import jakarta.xml.soap.DetailEntry;
import jakarta.xml.soap.SOAPException;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapFaultDetailElement;

/**
 * SAAJ-specific implementation of the {@code SoapFaultDetailElement} interface. Wraps a
 * {@link jakarta.xml.soap.DetailEntry}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoapFaultDetailElement extends SaajSoapElement<DetailEntry> implements SoapFaultDetailElement {

	SaajSoapFaultDetailElement(DetailEntry entry) {
		super(entry);
	}

	@Override
	public Result getResult() {
		return new DOMResult(getSaajDetailEntry());
	}

	@Override
	public void addText(@Nullable String text) {
		try {
			getSaajDetailEntry().addTextNode(text);
		}
		catch (SOAPException ex) {
			throw new SaajSoapFaultException(ex);
		}
	}

	protected DetailEntry getSaajDetailEntry() {
		return getSaajElement();
	}

}
