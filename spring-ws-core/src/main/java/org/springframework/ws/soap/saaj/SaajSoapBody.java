/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPElement;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.saaj.support.SaajUtils;

/**
 * SAAJ-specific abstract base class of the {@code SoapBody} interface. Wraps a
 * {@link jakarta.xml.soap.SOAPBody}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
abstract class SaajSoapBody extends SaajSoapElement<SOAPBody> implements SoapBody {

	SaajSoapBody(SOAPBody body) {
		super(body);
	}

	@Override
	public @Nullable Source getPayloadSource() {
		SOAPElement bodyElement = SaajUtils.getFirstBodyElement(getSaajBody());
		return (bodyElement != null) ? new DOMSource(bodyElement) : null;
	}

	@Override
	public Result getPayloadResult() {
		getSaajBody().removeContents();
		return new DOMResult(getSaajBody());
	}

	@Override
	public boolean hasFault() {
		return getSaajBody().hasFault();
	}

	protected SOAPBody getSaajBody() {
		return getSaajElement();
	}

}
