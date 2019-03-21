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

import javax.xml.transform.Result;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPFactory;

import org.springframework.ws.soap.SoapFaultDetailElement;

/**
 * Axiom-specific version of {@code org.springframework.ws.soap.SoapFaultDetailElement}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomSoapFaultDetailElement extends AxiomSoapElement implements SoapFaultDetailElement {

	public AxiomSoapFaultDetailElement(OMElement axiomElement, SOAPFactory soapFactory) {
		super(axiomElement, soapFactory);
	}

	@Override
	public Result getResult() {
		try {
			return new AxiomResult(getAxiomElement(), getAxiomFactory());
		}
		catch (OMException ex) {
			throw new AxiomSoapFaultException(ex);
		}

	}

	@Override
	public void addText(String text) {
		try {
			getAxiomElement().setText(text);
		}
		catch (OMException ex) {
			throw new AxiomSoapFaultException(ex);
		}
	}

}
