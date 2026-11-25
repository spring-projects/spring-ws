/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.support.creator;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.support.AssertionErrors;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Implementation of {@link WebServiceMessageCreator} that creates a request based on a
 * {@link Source}.
 *
 * @author Arjen Poutsma
 * @author Sharang Gupta
 * @since 2.0
 */
public class PayloadMessageCreator extends AbstractMessageCreator {

	private final Source payload;

	private final TransformerHelper transformerHelper = new TransformerHelper();

	private @Nullable Document payloadDocument;

	/**
	 * Creates a new instance of the {@code PayloadMessageCreator} with the given payload
	 * source.
	 * @param payload the payload source
	 */
	public PayloadMessageCreator(Source payload) {
		Assert.notNull(payload, "'payload' must not be null");
		this.payload = payload;
	}

	@Override
	protected void doWithMessage(WebServiceMessage message) throws IOException {
		try {
			this.transformerHelper.transform(new DOMSource(getPayloadDocument()), message.getPayloadResult());
		}
		catch (TransformerException ex) {
			AssertionErrors.fail("Could not transform request payload to message: " + ex.getMessage());
		}
	}

	/**
	 * Return the payload as a document. The payload source is transformed only once, so
	 * that a source that can be read only once can create more than one message.
	 * @return the payload document
	 * @throws TransformerException in case of transformation errors
	 */
	private Document getPayloadDocument() throws TransformerException {
		if (this.payloadDocument == null) {
			DOMResult result = new DOMResult();
			this.transformerHelper.transform(this.payload, result);
			this.payloadDocument = (Document) result.getNode();
		}
		return this.payloadDocument;
	}

}
