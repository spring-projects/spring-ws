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
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;

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
	public String getFaultStringOrReason() {
		if (getAxiomFault().getReason() != null) {
			return getAxiomFault().getReason().getText();
		}
		return null;
	}

	@Override
	public Locale getFaultStringLocale() {
		if (getAxiomFault().getReason() != null) {
			OMAttribute langAttribute =
					getAxiomFault().getReason().getAttribute(new QName("http://www.w3.org/XML/1998/namespace", "lang"));
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
