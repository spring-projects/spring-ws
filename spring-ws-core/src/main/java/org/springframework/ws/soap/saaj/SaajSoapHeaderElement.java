/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.saaj;

import javax.xml.soap.SOAPHeaderElement;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;

/**
 * SAAJ-specific implementation of the {@code SoapHeaderElement} interface. Wraps a
 * {@link javax.xml.soap.SOAPHeaderElement}.
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
