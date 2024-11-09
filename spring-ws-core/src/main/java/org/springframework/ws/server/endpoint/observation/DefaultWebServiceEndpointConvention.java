package org.springframework.ws.server.endpoint.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;

/**
 * ObservationConvention that describes how a WebServiceTemplate is observed.
 * @author Johan Kindgren
 */
public class DefaultWebServiceEndpointConvention implements WebServiceEndpointConvention {

    private static final KeyValue EXCEPTION_NONE = KeyValue.of(EndpointObservationDocumentation.LowCardinalityKeyNames.EXCEPTION, KeyValue.NONE_VALUE);
    private String name = "webservice.server";


    @Override
    public KeyValues getLowCardinalityKeyValues(WebServiceEndpointContext context) {
        return KeyValues.of(
                exception(context),
                localname(context),
                namespace(context),
                outcome(context),
                soapAction(context));

    }

    private KeyValue localname(WebServiceEndpointContext context) {
        return EndpointObservationDocumentation
                .LowCardinalityKeyNames
                .LOCALNAME
                .withValue(context.getLocalname());
    }

    private KeyValue namespace(WebServiceEndpointContext context) {
        return EndpointObservationDocumentation
                .LowCardinalityKeyNames
                .NAMESPACE
                .withValue(context.getNamespace());
    }


    private KeyValue outcome(WebServiceEndpointContext context) {
        return EndpointObservationDocumentation
                .LowCardinalityKeyNames
                .OUTCOME
                .withValue(context.getOutcome());
    }

    private KeyValue soapAction(WebServiceEndpointContext context) {
        return EndpointObservationDocumentation
                .LowCardinalityKeyNames
                .SOAPACTION
                .withValue(context.getSoapAction());
    }

    private KeyValue exception(WebServiceEndpointContext context) {
        if (context.getError() != null) {
            return EndpointObservationDocumentation
                    .LowCardinalityKeyNames
                    .EXCEPTION
                    .withValue(context.getError().getClass().getSimpleName());
        } else {
            return EXCEPTION_NONE;
        }
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(WebServiceEndpointContext context) {
        return KeyValues.empty();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContextualName(WebServiceEndpointContext context) {
        return "WebServiceEndpoint " + context.getNamespace() + ':' + context.getLocalname();
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof WebServiceEndpointContext;
    }

}
