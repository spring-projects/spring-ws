/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.mime.AbstractMimeMessage;

/**
 * Abstract implementation of the {@link SoapMessage} interface. Contains convenient
 * default implementations.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractSoapMessage extends AbstractMimeMessage implements SoapMessage {

	private @Nullable SoapVersion version;

	/** Returns {@code getEnvelope().getBody()}. */
	@Override
	public final SoapBody getSoapBody() {
		return getEnvelope().getBody();
	}

	/** Returns {@code getEnvelope().getHeader()}. */
	@Override
	public final @Nullable SoapHeader getSoapHeader() {
		return getEnvelope().getHeader();
	}

	/** Returns {@code getSoapBody().getPayloadSource()}. */
	@Override
	public final @Nullable Source getPayloadSource() {
		return getSoapBody().getPayloadSource();
	}

	/** Returns {@code getSoapBody().getPayloadResult()}. */
	@Override
	public final Result getPayloadResult() {
		return getSoapBody().getPayloadResult();
	}

	/** Returns {@code getSoapBody().hasFault()}. */
	@Override
	public final boolean hasFault() {
		return getSoapBody().hasFault();
	}

	/** Returns {@code getSoapBody().getFault().getFaultCode()}. */
	@Override
	public final @Nullable QName getFaultCode() {
		if (hasFault() && getSoapBody().getFault() != null) {
			return getSoapBody().getFault().getFaultCode();
		}
		else {
			return null;
		}
	}

	/** Returns {@code getSoapBody().getFault().getFaultStringOrReason()}. */
	@Override
	public final @Nullable String getFaultReason() {
		if (hasFault() && getSoapBody().getFault() != null) {
			return getSoapBody().getFault().getFaultStringOrReason();
		}
		else {
			return null;
		}
	}

	@Override
	public SoapVersion getVersion() {
		if (this.version == null) {
			String envelopeNamespace = getEnvelope().getName().getNamespaceURI();
			if (SoapVersion.SOAP_11.getEnvelopeNamespaceUri().equals(envelopeNamespace)) {
				this.version = SoapVersion.SOAP_11;
			}
			else if (SoapVersion.SOAP_12.getEnvelopeNamespaceUri().equals(envelopeNamespace)) {
				this.version = SoapVersion.SOAP_12;
			}
			else {
				throw new IllegalStateException(
						"Unknown Envelope namespace uri '" + envelopeNamespace + "'. " + "Cannot deduce SoapVersion.");
			}
		}
		return this.version;
	}

}
