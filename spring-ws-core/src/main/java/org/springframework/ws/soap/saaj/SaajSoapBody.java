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

import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPElement;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.saaj.support.SaajUtils;

/**
 * SAAJ-specific abstract base class of the {@code SoapBody} interface. Wraps a {@link jakarta.xml.soap.SOAPBody}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
abstract class SaajSoapBody extends SaajSoapElement<SOAPBody> implements SoapBody {

	public SaajSoapBody(SOAPBody body) {
		super(body);
	}

	@Override
	public Source getPayloadSource() {
		SOAPElement bodyElement = SaajUtils.getFirstBodyElement(getSaajBody());
		return bodyElement != null ? new DOMSource(bodyElement) : null;
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
