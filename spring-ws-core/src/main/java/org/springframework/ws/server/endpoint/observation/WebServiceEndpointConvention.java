package org.springframework.ws.server.endpoint.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

/**
 * ObservationConvention that describes how a WebServiceTemplate is observed.
 * @author Johan Kindgren
 */
public interface WebServiceEndpointConvention extends ObservationConvention<WebServiceEndpointContext> {

    @Override
    default boolean supportsContext(Observation.Context context) {
        return context instanceof WebServiceEndpointContext;
    }

}
