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

import jakarta.xml.soap.Node;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;

/**
 * SAAJ-specific implementation of the {@code SoapHeader} interface. Wraps a {@link jakarta.xml.soap.SOAPHeader}.
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
		} catch (SOAPException ex) {
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
			return iterator.hasNext();
		}

		@Override
		public SoapHeaderElement next() {
			Node saajHeaderElement = iterator.next();
			if (saajHeaderElement instanceof SOAPHeaderElement) {
				return new SaajSoapHeaderElement((SOAPHeaderElement) saajHeaderElement);
			} else {
				throw new RuntimeException("saajHeaderElement is not an instance of SOAPHeaderElement");
			}
		}

		@Override
		public void remove() {
			iterator.remove();
		}
	}

}
