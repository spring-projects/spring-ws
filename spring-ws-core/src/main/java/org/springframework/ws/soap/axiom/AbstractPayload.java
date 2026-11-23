/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.xml.StaxUtils;
import org.springframework.ws.soap.axiom.support.AxiomUtils;

/**
 * Abstract base class for {@link Payload} implementations.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
abstract class AbstractPayload extends Payload {

	private final SOAPBody axiomBody;

	private final SOAPFactory axiomFactory;

	protected AbstractPayload(SOAPBody axiomBody, SOAPFactory axiomFactory) {
		Assert.notNull(axiomBody, "'axiomBody' must not be null");
		Assert.notNull(axiomFactory, "'axiomFactory' must not be null");
		this.axiomBody = axiomBody;
		this.axiomFactory = axiomFactory;
	}

	@Override
	public final @Nullable Source getSource() {
		try {
			OMElement payloadElement = getPayloadElement();
			if (payloadElement != null) {
				XMLStreamReader streamReader = getStreamReader(payloadElement);
				return StaxUtils.createCustomStaxSource(streamReader);
			}
			else {
				return null;
			}
		}
		catch (OMException ex) {
			throw new AxiomSoapBodyException(ex);
		}
	}

	protected abstract XMLStreamReader getStreamReader(OMElement payloadElement);

	@Override
	public final Result getResult() {
		AxiomUtils.removeContents(getAxiomBody());
		return getResultInternal();
	}

	protected abstract Result getResultInternal();

	public SOAPFactory getAxiomFactory() {
		return this.axiomFactory;
	}

	protected SOAPBody getAxiomBody() {
		return this.axiomBody;
	}

	protected @Nullable OMElement getPayloadElement() throws OMException {
		return getAxiomBody().getFirstElement();
	}

}
