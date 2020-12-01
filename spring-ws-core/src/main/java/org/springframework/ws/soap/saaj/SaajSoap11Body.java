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

package org.springframework.ws.soap.saaj;

import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap11.Soap11Fault;

/**
 * SAAJ-specific implementation of the {@code Soap11Body} interface. Wraps a {@link javax.xml.soap.SOAPBody}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoap11Body extends SaajSoapBody implements Soap11Body {

	private final boolean langAttributeOnSoap11FaultString;

	SaajSoap11Body(SOAPBody body, boolean langAttributeOnSoap11FaultString) {
		super(body);
		this.langAttributeOnSoap11FaultString = langAttributeOnSoap11FaultString;
	}

	@Override
	public Soap11Fault getFault() {
		SOAPFault fault = getSaajBody().getFault();
		return fault != null ? new SaajSoap11Fault(fault) : null;
	}

	@Override
	public Soap11Fault addFault(QName faultCode, String faultString, Locale faultStringLocale) {
		Assert.notNull(faultCode, "No faultCode given");
		Assert.hasLength(faultString, "faultString cannot be empty");
		Assert.hasLength(faultCode.getLocalPart(), "faultCode's localPart cannot be empty");
		Assert.hasLength(faultCode.getNamespaceURI(), "faultCode's namespaceUri cannot be empty");
		if (!langAttributeOnSoap11FaultString) {
			faultStringLocale = null;
		}
		try {
			getSaajBody().removeContents();
			SOAPBody body = getSaajBody();
			SOAPFault result;
			if (faultStringLocale == null) {
				result = body.addFault(faultCode, faultString);
			} else {
				result = body.addFault(faultCode, faultString, faultStringLocale);
			}
			SOAPFault saajFault = result;
			return new SaajSoap11Fault(saajFault);
		} catch (SOAPException ex) {
			throw new SaajSoapFaultException(ex);
		}
	}

	@Override
	public Soap11Fault addClientOrSenderFault(String faultString, Locale locale) {
		return addFault(SoapVersion.SOAP_11.getClientOrSenderFaultName(), faultString, locale);
	}

	@Override
	public Soap11Fault addMustUnderstandFault(String faultString, Locale locale) {
		return addFault(SoapVersion.SOAP_11.getMustUnderstandFaultName(), faultString, locale);
	}

	@Override
	public Soap11Fault addServerOrReceiverFault(String faultString, Locale locale) {
		return addFault(SoapVersion.SOAP_11.getServerOrReceiverFaultName(), faultString, locale);
	}

	@Override
	public Soap11Fault addVersionMismatchFault(String faultString, Locale locale) {
		return addFault(SoapVersion.SOAP_11.getVersionMismatchFaultName(), faultString, locale);
	}

}
