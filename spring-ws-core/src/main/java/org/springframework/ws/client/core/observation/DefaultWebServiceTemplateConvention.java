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
