/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

import org.jspecify.annotations.Nullable;

/**
 * Represents the {@code Envelope} element in a SOAP message. The header contains the
 * optional {@code SoapHeader} and {@code SoapBody}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface SoapEnvelope extends SoapElement {

	/**
	 * Returns the {@code SoapHeader}. Returns {@code null} if no header is present.
	 * @return the {@code SoapHeader}, or {@code null}
	 * @throws SoapHeaderException if the header cannot be returned
	 */
	@Nullable SoapHeader getHeader() throws SoapHeaderException;

	/**
	 * Returns the {@code SoapBody}.
	 * @return the {@code SoapBody}
	 * @throws SoapBodyException if the header cannot be returned
	 */
	SoapBody getBody() throws SoapBodyException;

}
