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

package org.springframework.ws.soap.axiom;

import java.util.Iterator;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.springframework.util.Assert;
import org.springframework.util.xml.StaxUtils;

/**
 * Abstract base class for Axiom 1.4 {@link Payload} implementations.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 3.1
 */
abstract class AxiomAbstractPayload implements Payload {

	private final SOAPBody axiomBody;

	private final SOAPFactory axiomFactory;

	protected AxiomAbstractPayload(SOAPBody axiomBody, SOAPFactory axiomFactory) {
		Assert.notNull(axiomBody, "'axiomBody' must not be null");
		Assert.notNull(axiomFactory, "'axiomFactory' must not be null");
		this.axiomBody = axiomBody;
		this.axiomFactory = axiomFactory;
	}

	@Override
	public final Source getSource() {
		try {
			OMElement payloadElement = getPayloadElement();
			if (payloadElement != null) {
				XMLStreamReader streamReader = getStreamReader(payloadElement);
				return StaxUtils.createCustomStaxSource(streamReader);
			} else {
				return null;
			}
		} catch (OMException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected abstract XMLStreamReader getStreamReader(OMElement payloadElement);

	@Override
	public final Result getResult() {
		for (Iterator<?> iterator = getAxiomBody().getChildren(); iterator.hasNext();) {
			iterator.next();
			iterator.remove();
		}
		return getResultInternal();
	}

	protected abstract Result getResultInternal();

	public SOAPFactory getAxiomFactory() {
		return axiomFactory;
	}

	protected SOAPBody getAxiomBody() {
		return axiomBody;
	}

	protected OMElement getPayloadElement() throws OMException {
		return getAxiomBody().getFirstElement();
	}
}
