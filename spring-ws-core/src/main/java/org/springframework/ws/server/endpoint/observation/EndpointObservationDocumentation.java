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
import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * ObservationDocumentation for a WebService Endpoint.
 *
 * @author Johan Kindgren
 */
public enum EndpointObservationDocumentation implements ObservationDocumentation {
    /**
     * An enumeration for ObservationDocumentation related to WebService Endpoint.
     *
     * The {@code WEB_SERVICE_ENDPOINT} provides default conventions and low cardinality key names for
     * observing a WebService endpoint.
     *
     * This implementation returns the {@link DefaultWebServiceEndpointConvention} class as the default convention,
     * and an array of {@link LowCardinalityKeyNames} for low cardinality key names.
     */
    WEB_SERVICE_ENDPOINT {
        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return DefaultWebServiceEndpointConvention.class;
        }

        @Override
        public KeyName[] getLowCardinalityKeyNames() {
            return LowCardinalityKeyNames.values();
        }

    };

    /**
     * Enum representing low cardinality key names for observing a WebService endpoint.
     */
    enum LowCardinalityKeyNames implements KeyName {

        /**
         * Name of the exception thrown during the exchange,
         * or {@value KeyValue#NONE_VALUE} if no exception happened.
         */
        EXCEPTION {
            @Override
            public String asString() {
                return "exception";
            }
        },

        /**
         * Outcome of the WebService exchange.
         */
        OUTCOME {
            @Override
            public String asString() {
                return "outcome";
            }
        },
        /**
         * Namespace of the WebService payload.
         */
        NAMESPACE {
            @Override
            public String asString() {
                return "namespace";
            }
        },
        /**
         * Localpart of the WebService payload.
         */
        LOCALPART {
            @Override
            public String asString() {
                return "localpart";
            }
        },

        /**
         * Value from the SoapAction header.
         */
        SOAPACTION {
            @Override
            public String asString() {
                return "soapaction";
            }
        }
    }
}
