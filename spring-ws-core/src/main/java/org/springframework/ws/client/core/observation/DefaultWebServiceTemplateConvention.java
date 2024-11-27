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
import org.springframework.ws.client.core.observation.WebServiceTemplateObservationDocumentation.LowCardinalityKeyNames;

/**
 * ObservationConvention that describes how a WebServiceTemplate is observed.
 * @author Johan Kindgren
 */
public class DefaultWebServiceTemplateConvention implements WebServiceTemplateConvention {

    private static final KeyValue EXCEPTION_NONE = KeyValue.of(LowCardinalityKeyNames.EXCEPTION,
            KeyValue.NONE_VALUE);
    private static final String NAME = "webservice.client";

    @Override
    public KeyValues getHighCardinalityKeyValues(WebServiceTemplateObservationContext context) {
        if (context.getPath() != null) {
            return KeyValues.of(path(context));
        }
        return KeyValues.empty();
    }

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

    private KeyValue path(WebServiceTemplateObservationContext context) {

        return WebServiceTemplateObservationDocumentation.HighCardinalityKeyNames
                .PATH
                .withValue(context.getPath());
    }

    private KeyValue localname(WebServiceTemplateObservationContext context) {
        return LowCardinalityKeyNames
                .LOCALPART
                .withValue(context.getLocalPart());
    }

    private KeyValue namespace(WebServiceTemplateObservationContext context) {
        return LowCardinalityKeyNames
                .NAMESPACE
                .withValue(context.getNamespace());
    }
    private KeyValue host(WebServiceTemplateObservationContext context) {
        return LowCardinalityKeyNames
                .HOST
                .withValue(context.getHost());
    }


    private KeyValue outcome(WebServiceTemplateObservationContext context) {
        return LowCardinalityKeyNames
                .OUTCOME
                .withValue(context.getOutcome());
    }

    private KeyValue soapAction(WebServiceTemplateObservationContext context) {
        return LowCardinalityKeyNames
                .SOAPACTION
                .withValue(context.getSoapAction());
    }

    private KeyValue exception(WebServiceTemplateObservationContext context) {
        if (context.getError() != null) {
            return LowCardinalityKeyNames
                    .EXCEPTION
                    .withValue(context.getError().getClass().getSimpleName());
        }
        return EXCEPTION_NONE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getContextualName(WebServiceTemplateObservationContext context) {
        return context.getContextualName();
    }
}
