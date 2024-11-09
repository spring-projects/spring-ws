package org.springframework.ws.client.core.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * ObservationDocumentation for WebSeviceTemplate.
 *
 * @author Johan Kindgren
 */
public enum WebServiceTemplateObservationDocumentation implements ObservationDocumentation {

    WEB_SERVICE_TEMPLATE {

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return DefaultWebServiceTemplateConvention.class;
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
        HOST {
            @Override
            public String asString() {
                return "host";
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
