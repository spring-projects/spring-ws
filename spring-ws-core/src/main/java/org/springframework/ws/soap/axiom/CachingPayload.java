/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLStreamReaderConfiguration;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;

/**
 * Caching payload in Axiom.
 *
 * @author Arjen Poutsma
 * @since 1.5.2
 */
@SuppressWarnings("Since15")
class CachingPayload extends AbstractPayload {

	CachingPayload(SOAPBody axiomBody, SOAPFactory axiomFactory) {
		super(axiomBody, axiomFactory);
	}

	@Override
	protected XMLStreamReader getStreamReader(OMElement payloadElement) {
		OMXMLStreamReaderConfiguration config = new OMXMLStreamReaderConfiguration();
		config.setPreserveNamespaceContext(true);
		return payloadElement.getXMLStreamReader(true, config);
	}

	@Override
	public Result getResultInternal() {
		return getAxiomBody().getSAXResult();
	}

}
