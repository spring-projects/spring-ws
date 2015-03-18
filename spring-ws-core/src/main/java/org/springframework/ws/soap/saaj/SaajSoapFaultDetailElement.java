/*
 * Copyright 2005-2014 the original author or authors.
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

import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.springframework.ws.soap.SoapFaultDetailElement;

/**
 * SAAJ-specific implementation of the {@code SoapFaultDetailElement} interface. Wraps a {@link
 * javax.xml.soap.DetailEntry}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoapFaultDetailElement extends SaajSoapElement<DetailEntry> implements SoapFaultDetailElement {

	SaajSoapFaultDetailElement(DetailEntry entry) {
		super(entry);
	}

	@Override
	public Result getResult() {
		return new DOMResult(getSaajDetailEntry());
	}

	@Override
	public void addText(String text) {
		try {
			getSaajDetailEntry().addTextNode(text);
		}
		catch (SOAPException ex) {
			throw new SaajSoapFaultException(ex);
		}
	}

	protected DetailEntry getSaajDetailEntry() {
		return getSaajElement();
	}

}
