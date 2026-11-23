/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.observation;

import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;

/**
 * {@link ObservationConvention} for
 * {@link SoapServerObservationDocumentation#SOAP_SERVER_REQUESTS SOAP server requests}.
 *
 * @author Brian Clozel
 * @since 5.1.0
 */
public interface SoapServerObservationConvention extends ObservationConvention<SoapServerObservationContext> {

	@Override
	default boolean supportsContext(Context context) {
		return context instanceof SoapServerObservationContext;
	}

}
