package org.springframework.ws.server.endpoint.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * ObservationDocumentation for EndpointAdapter.
 *
 * @author Johan Kindgren
 */
public enum EndpointObservationDocumentation implements ObservationDocumentation {

    WEB_SERVICE_ENDPOINT {
        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return DefaultWebServiceEndpointConvention.class;
        }

        @Override
        public KeyName[] getLowCardinalityKeyNames() {
            return LowCardinalityKeyNames.values();
        }

        @Override
        public KeyName[] getHighCardinalityKeyNames() {
            return super.getHighCardinalityKeyNames();
        }
    };

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

        NAMESPACE {
            @Override
            public String asString() {
                return "namespace";
            }
        },

        LOCALNAME {
            @Override
            public String asString() {
                return "localname";
            }
        },

        SOAPACTION {
            @Override
            public String asString() {
                return "soapaction";
            }
        }
    }
}
