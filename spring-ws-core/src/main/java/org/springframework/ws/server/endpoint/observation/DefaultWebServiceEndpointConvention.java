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
    private static final String NAME = "webservice.server";


    @Override
    public KeyValues getLowCardinalityKeyValues(WebServiceEndpointContext context) {
        return KeyValues.of(
                exception(context),
                localPart(context),
                namespace(context),
                outcome(context),
                path(context),
                soapAction(context));
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(WebServiceEndpointContext context) {
        if (context.getPathInfo() != null) {
            return KeyValues.of(pathInfo(context));
        }
        return KeyValues.empty();
    }

    private KeyValue localPart(WebServiceEndpointContext context) {
        return EndpointObservationDocumentation
                .LowCardinalityKeyNames
                .LOCALPART
                .withValue(context.getLocalPart());
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

    private KeyValue path(WebServiceEndpointContext context) {
        return EndpointObservationDocumentation
                .LowCardinalityKeyNames
                .PATH
                .withValue(context.getPath());
    }

    private KeyValue pathInfo(WebServiceEndpointContext context) {
        if (context.getPathInfo() != null) {
            return EndpointObservationDocumentation
                    .HighCardinalityKeyNames
                    .PATH_INFO
                    .withValue(context.getPathInfo());
        }
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getContextualName(WebServiceEndpointContext context) {
        return context.getContextualName();
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof WebServiceEndpointContext;
    }

}
