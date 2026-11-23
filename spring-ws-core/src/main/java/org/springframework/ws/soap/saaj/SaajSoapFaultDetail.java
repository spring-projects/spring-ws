/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import jakarta.xml.soap.Detail;
import jakarta.xml.soap.DetailEntry;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFaultElement;

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

	SaajSoapFaultDetail(SOAPFaultElement faultElement) {
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
		}
		catch (SOAPException ex) {
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

	private static final class SaajSoapFaultDetailElementIterator implements Iterator<SoapFaultDetailElement> {

		private final Iterator<DetailEntry> iterator;

		private SaajSoapFaultDetailElementIterator(Iterator<DetailEntry> iterator) {
			Assert.notNull(iterator, "No iterator given");
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public SoapFaultDetailElement next() {
			DetailEntry saajDetailEntry = this.iterator.next();
			return new SaajSoapFaultDetailElement(saajDetailEntry);
		}

		@Override
		public void remove() {
			this.iterator.remove();
		}

	}

}
