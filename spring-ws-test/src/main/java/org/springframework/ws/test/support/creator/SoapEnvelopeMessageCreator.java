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
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.test.support.AssertionErrors;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Implementation of {@link WebServiceMessageCreator} that creates a request based on a
 * SOAP envelope {@link Source}.
 *
 * @author Alexander Shutyaev
 * @author Sharang Gupta
 * @since 2.1.1
 */
public class SoapEnvelopeMessageCreator extends AbstractMessageCreator {

	private final Source soapEnvelope;

	private final TransformerHelper transformerHelper = new TransformerHelper();

	private @Nullable Document soapEnvelopeDocument;

	/**
	 * Creates a new instance of the {@code SoapEnvelopeMessageCreator} with the given
	 * SOAP envelope source.
	 * @param soapEnvelope the SOAP envelope source
	 */
	public SoapEnvelopeMessageCreator(Source soapEnvelope) {
		Assert.notNull(soapEnvelope, "'soapEnvelope' must not be null");
		this.soapEnvelope = soapEnvelope;
	}

	@Override
	protected void doWithMessage(WebServiceMessage message) throws IOException {
		AssertionErrors.assertTrue("Message created with factory is not a SOAP message",
				message instanceof SoapMessage);
		SoapMessage soapMessage = (SoapMessage) message;
		try {
			DOMResult result = new DOMResult();
			this.transformerHelper.transform(new DOMSource(getSoapEnvelopeDocument()), result);
			soapMessage.setDocument((Document) result.getNode());
		}
		catch (TransformerException ex) {
			AssertionErrors.fail("Could not transform request SOAP envelope to message: " + ex.getMessage());
		}
	}

	/**
	 * Return the SOAP envelope as a document. The SOAP envelope source is transformed
	 * only once, so that a source that can be read only once can create more than one
	 * message.
	 * @return the SOAP envelope document
	 * @throws TransformerException in case of transformation errors
	 */
	private Document getSoapEnvelopeDocument() throws TransformerException {
		if (this.soapEnvelopeDocument == null) {
			DOMResult result = new DOMResult();
			this.transformerHelper.transform(this.soapEnvelope, result);
			this.soapEnvelopeDocument = (Document) result.getNode();
		}
		return this.soapEnvelopeDocument;
	}

}
