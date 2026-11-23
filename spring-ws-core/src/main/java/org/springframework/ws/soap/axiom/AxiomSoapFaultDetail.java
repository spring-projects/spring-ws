/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFaultDetail;

import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;

/**
 * Axiom-specific version of {@code org.springframework.ws.soap.SoapFaultDetail}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomSoapFaultDetail extends AxiomSoapElement implements SoapFaultDetail {

	AxiomSoapFaultDetail(SOAPFaultDetail axiomFaultDetail, SOAPFactory axiomFactory) {
		super(axiomFaultDetail, axiomFactory);
	}

	@Override
	public SoapFaultDetailElement addFaultDetailElement(QName name) {
		try {
			OMElement element = getAxiomFactory().createOMElement(name, getAxiomFaultDetail());
			return new AxiomSoapFaultDetailElement(element, getAxiomFactory());
		}
		catch (OMException ex) {
			throw new AxiomSoapFaultException(ex);
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<SoapFaultDetailElement> getDetailEntries() {
		return new AxiomSoapFaultDetailElementIterator(getAxiomFaultDetail().getChildElements());
	}

	@Override
	public Result getResult() {
		return getAxiomFaultDetail().getSAXResult();
	}

	protected SOAPFaultDetail getAxiomFaultDetail() {
		return (SOAPFaultDetail) getAxiomElement();
	}

	private final class AxiomSoapFaultDetailElementIterator implements Iterator<SoapFaultDetailElement> {

		private final Iterator<OMElement> axiomIterator;

		private AxiomSoapFaultDetailElementIterator(Iterator<OMElement> axiomIterator) {
			this.axiomIterator = axiomIterator;
		}

		@Override
		public boolean hasNext() {
			return this.axiomIterator.hasNext();
		}

		@Override
		public SoapFaultDetailElement next() {
			try {
				OMElement axiomElement = this.axiomIterator.next();
				return new AxiomSoapFaultDetailElement(axiomElement, getAxiomFactory());
			}
			catch (OMException ex) {
				throw new AxiomSoapFaultException(ex);
			}

		}

		@Override
		public void remove() {
			this.axiomIterator.remove();
		}

	}

}
