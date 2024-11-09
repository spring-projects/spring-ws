package org.springframework.ws.client.core.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;

/**
 * ObservationConvention that describes how a WebServiceTemplate is observed.
 * @author Johan Kindgren
 */
public class DefaultWebServiceTemplateConvention implements WebServiceTemplateConvention {

    private static final KeyValue EXCEPTION_NONE = KeyValue.of(WebServiceTemplateObservationDocumentation.LowCardinalityKeyNames.EXCEPTION, KeyValue.NONE_VALUE);
    private String name = "webservice.client";


    @Override
    public KeyValues getLowCardinalityKeyValues(WebServiceTemplateObservationContext context) {
        return KeyValues.of(
                exception(context),
                host(context),
                localname(context),
                namespace(context),
                outcome(context),
                soapAction(context));
    }

    private KeyValue localname(WebServiceTemplateObservationContext context) {
        return WebServiceTemplateObservationDocumentation
                .LowCardinalityKeyNames
                .LOCALNAME
                .withValue(context.getLocalname());
    }

    private KeyValue namespace(WebServiceTemplateObservationContext context) {
        return WebServiceTemplateObservationDocumentation
                .LowCardinalityKeyNames
                .NAMESPACE
                .withValue(context.getNamespace());
    }
    private KeyValue host(WebServiceTemplateObservationContext context) {
        return WebServiceTemplateObservationDocumentation
                .LowCardinalityKeyNames
                .HOST
                .withValue(context.getHost());
    }


    private KeyValue outcome(WebServiceTemplateObservationContext context) {
        return WebServiceTemplateObservationDocumentation
                .LowCardinalityKeyNames
                .OUTCOME
                .withValue(context.getOutcome());
    }

    private KeyValue soapAction(WebServiceTemplateObservationContext context) {
        return WebServiceTemplateObservationDocumentation
                .LowCardinalityKeyNames
                .SOAPACTION
                .withValue(context.getSoapAction());
    }

    private KeyValue exception(WebServiceTemplateObservationContext context) {
        if (context.getError() != null) {
            return WebServiceTemplateObservationDocumentation
                    .LowCardinalityKeyNames
                    .EXCEPTION
                    .withValue(context.getError().getClass().getSimpleName());
        } else {
            return EXCEPTION_NONE;
        }
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(WebServiceTemplateObservationContext context) {
        return KeyValues.empty();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContextualName(WebServiceTemplateObservationContext context) {
        return "WebServiceTemplate " + context.getHost();
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof WebServiceTemplateObservationContext;
    }
}
