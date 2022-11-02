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

package org.springframework.ws.soap.saaj;

import jakarta.xml.soap.Detail;
import jakarta.xml.soap.DetailEntry;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFaultElement;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;

/**
 * SAAJ-specific implementation of the {@code SoapFaultDetail} interface. Wraps a
 * {@link jakarta.xml.soap.SOAPFaultElement}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoapFaultDetail extends SaajSoapElement<SOAPFaultElement> implements SoapFaultDetail {

	public SaajSoapFaultDetail(SOAPFaultElement faultElement) {
		super(faultElement);
	}

	@Override
	public Result getResult() {
		return new DOMResult(getSaajDetail());
	}

	@Override
	public SoapFaultDetailElement addFaultDetailElement(QName name) {
		try {
			DetailEntry detailEntry = getSaajDetail().addDetailEntry(name);
			return new SaajSoapFaultDetailElement(detailEntry);
		} catch (SOAPException ex) {
			throw new SaajSoapFaultException(ex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<SoapFaultDetailElement> getDetailEntries() {
		Iterator<DetailEntry> iterator = getSaajDetail().getDetailEntries();
		return new SaajSoapFaultDetailElementIterator(iterator);
	}

	protected Detail getSaajDetail() {
		return (Detail) getSaajElement();
	}

	private static class SaajSoapFaultDetailElementIterator implements Iterator<SoapFaultDetailElement> {

		private final Iterator<DetailEntry> iterator;

		private SaajSoapFaultDetailElementIterator(Iterator<DetailEntry> iterator) {
			Assert.notNull(iterator, "No iterator given");
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public SoapFaultDetailElement next() {
			DetailEntry saajDetailEntry = iterator.next();
			return new SaajSoapFaultDetailElement(saajDetailEntry);
		}

		@Override
		public void remove() {
			iterator.remove();
		}
	}

}
