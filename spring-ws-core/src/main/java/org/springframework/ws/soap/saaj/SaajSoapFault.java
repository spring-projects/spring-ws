/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.saaj;

import jakarta.xml.soap.Detail;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFault;

import javax.xml.namespace.QName;

import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;

/**
 * SAAJ-specific abstract base class of the {@code SoapFault} interface. Wraps a {@link jakarta.xml.soap.SOAPFault}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
abstract class SaajSoapFault extends SaajSoapElement<SOAPFault> implements SoapFault {

	protected SaajSoapFault(SOAPFault fault) {
		super(fault);
	}

	@Override
	public QName getFaultCode() {
		return getSaajFault().getFaultCodeAsQName();
	}

	protected SOAPFault getSaajFault() {
		return getSaajElement();
	}

	@Override
	public SoapFaultDetail getFaultDetail() {
		Detail saajDetail = getSaajFault().getDetail();
		return saajDetail != null ? new SaajSoapFaultDetail(saajDetail) : null;
	}

	@Override
	public SoapFaultDetail addFaultDetail() {
		try {
			Detail saajDetail = getSaajFault().addDetail();
			return new SaajSoapFaultDetail(saajDetail);
		} catch (SOAPException ex) {
			throw new SaajSoapFaultException(ex);
		}
	}
}
