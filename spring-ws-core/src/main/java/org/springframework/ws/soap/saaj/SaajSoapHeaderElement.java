/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import jakarta.xml.soap.SOAPHeaderElement;

import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;

/**
 * SAAJ-specific implementation of the {@code SoapHeaderElement} interface. Wraps a
 * {@link jakarta.xml.soap.SOAPHeaderElement}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoapHeaderElement extends SaajSoapElement<SOAPHeaderElement> implements SoapHeaderElement {

	SaajSoapHeaderElement(SOAPHeaderElement headerElement) {
		super(headerElement);
	}

	@Override
	public Result getResult() throws SoapHeaderException {
		return new DOMResult(getSaajElement());
	}

	@Override
	public String getActorOrRole() throws SoapHeaderException {
		return getSaajHeaderElement().getActor();
	}

	@Override
	public void setActorOrRole(String actorOrRole) throws SoapHeaderException {
		getSaajHeaderElement().setActor(actorOrRole);
	}

	@Override
	public boolean getMustUnderstand() throws SoapHeaderException {
		return getSaajHeaderElement().getMustUnderstand();
	}

	@Override
	public void setMustUnderstand(boolean mustUnderstand) throws SoapHeaderException {
		getSaajHeaderElement().setMustUnderstand(mustUnderstand);
	}

	@Override
	public String getText() {
		return getSaajHeaderElement().getValue();
	}

	@Override
	public void setText(String content) {
		getSaajHeaderElement().setValue(content);
	}

	protected SOAPHeaderElement getSaajHeaderElement() {
		return getSaajElement();
	}

}
