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

import jakarta.xml.soap.Node;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;

/**
 * SAAJ-specific implementation of the {@code SoapHeader} interface. Wraps a
 * {@link jakarta.xml.soap.SOAPHeader}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
abstract class SaajSoapHeader extends SaajSoapElement<SOAPHeader> implements SoapHeader {

	SaajSoapHeader(SOAPHeader header) {
		super(header);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<SoapHeaderElement> examineAllHeaderElements() throws SoapHeaderException {
		Iterator<SOAPHeaderElement> iterator = getSaajHeader().examineAllHeaderElements();
		return new SaajSoapHeaderElementIterator(iterator);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<SoapHeaderElement> examineHeaderElements(QName name) throws SoapHeaderException {
		Iterator<Node> iterator = getSaajHeader().getChildElements(name);
		return new SaajSoapHeaderElementIterator(iterator);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<SoapHeaderElement> examineMustUnderstandHeaderElements(String actorOrRole)
			throws SoapHeaderException {
		Iterator<SOAPHeaderElement> iterator = getSaajHeader().examineMustUnderstandHeaderElements(actorOrRole);
		return new SaajSoapHeaderElementIterator(iterator);
	}

	@Override
	public SoapHeaderElement addHeaderElement(QName name) throws SoapHeaderException {
		try {
			SOAPHeaderElement headerElement = getSaajHeader().addHeaderElement(name);
			return new SaajSoapHeaderElement(headerElement);
		}
		catch (SOAPException ex) {
			throw new SaajSoapHeaderException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removeHeaderElement(QName name) throws SoapHeaderException {
		Iterator<Node> iterator = getSaajHeader().getChildElements(name);
		if (iterator.hasNext()) {
			Node element = iterator.next();
			element.detachNode();
		}
	}

	protected SOAPHeader getSaajHeader() {
		return getSaajElement();
	}

	@Override
	public Result getResult() {
		return new DOMResult(getSaajHeader());
	}

	protected static class SaajSoapHeaderElementIterator implements Iterator<SoapHeaderElement> {

		private final Iterator<? extends Node> iterator;

		protected SaajSoapHeaderElementIterator(Iterator<? extends Node> iterator) {
			Assert.notNull(iterator, "iterator must not be null");
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public SoapHeaderElement next() {
			Node saajHeaderElement = this.iterator.next();
			if (saajHeaderElement instanceof SOAPHeaderElement) {
				return new SaajSoapHeaderElement((SOAPHeaderElement) saajHeaderElement);
			}
			else {
				throw new RuntimeException("saajHeaderElement is not an instance of SOAPHeaderElement");
			}
		}

		@Override
		public void remove() {
			this.iterator.remove();
		}

	}

}
