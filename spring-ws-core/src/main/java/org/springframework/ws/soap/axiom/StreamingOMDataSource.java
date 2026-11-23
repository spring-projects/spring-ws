/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.ds.AbstractPushOMDataSource;

import org.springframework.util.Assert;
import org.springframework.ws.stream.StreamingPayload;

/**
 * Implementation of {@link OMDataSource} that wraps a {@link StreamingPayload}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class StreamingOMDataSource extends AbstractPushOMDataSource {

	private final StreamingPayload payload;

	StreamingOMDataSource(StreamingPayload payload) {
		Assert.notNull(payload, "'payload' must not be null");
		this.payload = payload;
	}

	@Override
	public boolean isDestructiveWrite() {
		return false;
	}

	@Override
	public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
		this.payload.writeTo(xmlWriter);
	}

}
