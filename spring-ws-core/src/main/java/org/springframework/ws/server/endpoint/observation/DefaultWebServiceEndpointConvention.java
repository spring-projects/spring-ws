/*
 * Copyright 2005-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ws.server.endpoint.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;

/**
 * Default ObservationConvention for a WebService Endpoint.
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