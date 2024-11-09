package org.springframework.ws.client.core.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

/**
 * ObservationConvention that describes how a WebServiceTemplate is observed.
 * @author Johan Kindgren
 */
public interface WebServiceTemplateConvention extends ObservationConvention<WebServiceTemplateObservationContext> {

    @Override
    default boolean supportsContext(Observation.Context context) {
        return context instanceof WebServiceTemplateObservationContext;
    }
}
