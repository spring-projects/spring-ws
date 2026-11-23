/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.core.observation;

import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;

/**
 * {@link ObservationConvention} for
 * {@link SoapClientObservationDocumentation#SOAP_CLIENT_REQUESTS SOAP client requests}.
 *
 * @author Stephane Nicoll
 * @since 5.1.0
 */
public interface SoapClientObservationConvention extends ObservationConvention<SoapClientObservationContext> {

	@Override
	default boolean supportsContext(Context context) {
		return context instanceof SoapClientObservationContext;
	}

}
