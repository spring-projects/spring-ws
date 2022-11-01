/*
 * Copyright 2005-2022 the original author or authors.
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

import javax.xml.transform.Result;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.springframework.ws.soap.SoapHeaderElement;

/** Axiom-specific version of {@code org.springframework.ws.soap.SoapHeaderHeaderElement}. */
class AxiomSoapHeaderElement extends AxiomSoapElement implements SoapHeaderElement {

	public AxiomSoapHeaderElement(OMElement axiomHeaderBlock, SOAPFactory axiomFactory) {
		super(axiomHeaderBlock, axiomFactory);
	}

	@Override
	public String getActorOrRole() {
		return getAxiomHeaderBlock().getRole();
	}

	@Override
	public void setActorOrRole(String role) {
		getAxiomHeaderBlock().setRole(role);
	}

	@Override
	public boolean getMustUnderstand() {
		return getAxiomHeaderBlock().getMustUnderstand();
	}

	@Override
	public void setMustUnderstand(boolean mustUnderstand) {
		getAxiomHeaderBlock().setMustUnderstand(mustUnderstand);
	}

	@Override
	public Result getResult() {
		try {
			return getAxiomHeaderBlock().getSAXResult();
		} catch (OMException ex) {
			throw new AxiomSoapHeaderException(ex);
		}

	}

	@Override
	public String getText() {
		return getAxiomHeaderBlock().getText();
	}

	@Override
	public void setText(String content) {
		getAxiomHeaderBlock().setText(content);
	}

	protected SOAPHeaderBlock getAxiomHeaderBlock() {
		return (SOAPHeaderBlock) getAxiomElement();
	}

}
