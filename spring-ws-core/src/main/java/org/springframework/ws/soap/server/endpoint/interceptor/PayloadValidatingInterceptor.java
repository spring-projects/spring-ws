/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint.interceptor;

import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceMessage;

/**
 * Interceptor that validates the contents of {@code WebServiceMessage}s using a schema.
 * Allows for both W3C XML and RELAX NG schemas.
 * <p>
 * When the payload is invalid, this interceptor stops processing of the interceptor
 * chain. Additionally, if the message is a SOAP request message, a SOAP Fault is created
 * as reply. Invalid SOAP responses do not result in a fault.
 * <p>
 * The schema to validate against is set with the {@code schema} property or
 * {@code schemas} property. By default, only the request message is validated, but this
 * behaviour can be changed using the {@code validateRequest} and {@code validateResponse}
 * properties. Responses that contains faults are not validated.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #setSchema(org.springframework.core.io.Resource)
 * @see #setSchemas(org.springframework.core.io.Resource[])
 * @see #setValidateRequest(boolean)
 * @see #setValidateResponse(boolean)
 */
public class PayloadValidatingInterceptor extends AbstractFaultCreatingValidatingInterceptor {

	/** Returns the payload source of the given message. */
	@Override
	protected @Nullable Source getValidationRequestSource(WebServiceMessage request) {
		return request.getPayloadSource();
	}

	/** Returns the payload source of the given message. */
	@Override
	protected @Nullable Source getValidationResponseSource(WebServiceMessage response) {
		return response.getPayloadSource();
	}

}
