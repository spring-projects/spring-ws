/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.test.support.AssertionErrors;

/**
 * Implementation of {@link ResponseCreator} that responds with a SOAP fault.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
abstract class SoapFaultResponseCreator extends AbstractResponseCreator {

	@Override
	protected void doWithResponse(URI uri, WebServiceMessage request, WebServiceMessage response) throws IOException {
		if (!(response instanceof SoapMessage soapResponse)) {
			AssertionErrors.fail("Response is not a SOAP message");
			return;
		}
		SoapBody responseBody = soapResponse.getSoapBody();
		if (responseBody == null) {
			AssertionErrors.fail("SOAP message [" + response + "] does not contain SOAP body");
		}
		addSoapFault(responseBody);
	}

	/**
	 * Abstract template method that allows subclasses to add a SOAP Fault to the given
	 * Body.
	 * @param soapBody the body to attach a fault to
	 */
	protected abstract void addSoapFault(SoapBody soapBody);

}
