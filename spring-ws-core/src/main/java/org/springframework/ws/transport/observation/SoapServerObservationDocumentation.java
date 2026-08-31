/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * Documented {@link KeyValue KeyValues} for the observations of the web service requests
 * that a server handles.
 *
 * @author Brian Clozel
 * @since 5.1.0
 */
public enum SoapServerObservationDocumentation implements ObservationDocumentation {

	/**
	 * Web service request observations for servers.
	 */
	SOAP_SERVER_REQUESTS {
		@Override
		public Class<? extends ObservationConvention<? extends Context>> getDefaultConvention() {
			return SoapServerObservationConvention.class;
		}

		@Override
		public KeyName[] getLowCardinalityKeyNames() {
			return LowCardinalityKeyNames.values();
		}

		@Override
		public KeyName[] getHighCardinalityKeyNames() {
			return HighCardinalityKeyNames.values();
		}
	};

	/**
	 * Low cardinality key names, suitable for use as metric dimensions.
	 */
	public enum LowCardinalityKeyNames implements KeyName {

		/**
		 * Simple class name of the exception that terminated the exchange, or
		 * {@value KeyValue#NONE_VALUE} if the exchange completed normally.
		 */
		EXCEPTION {
			@Override
			public String asString() {
				return "exception";
			}
		},

		/**
		 * Fault code of the SOAP response, or {@value KeyValue#NONE_VALUE} if the
		 * response is not a fault.
		 */
		FAULT_CODE {
			@Override
			public String asString() {
				return "fault.code";
			}
		},

		/**
		 * Target namespace of the operation, or {@value KeyValue#NONE_VALUE} if the
		 * request could not be mapped to an endpoint.
		 */
		NAMESPACE {
			@Override
			public String asString() {
				return "namespace";
			}
		},

		/**
		 * Name of the operation, as defined by {@code wsdl:operation name}, or
		 * {@value KeyValue#NONE_VALUE} if the request could not be mapped to an endpoint.
		 */
		OPERATION_NAME {
			@Override
			public String asString() {
				return "operation.name";
			}
		},

		/**
		 * Outcome of the exchange: {@code SUCCESS} for a regular response, {@code FAULT}
		 * for a SOAP fault, {@code ERROR} if an exception was thrown, and {@code UNKNOWN}
		 * if no response was produced.
		 */
		OUTCOME {
			@Override
			public String asString() {
				return "outcome";
			}
		},

		/**
		 * Scheme of the transport used to perform the operation, such as {@code http},
		 * {@code jms}, or {@code mailto}.
		 */
		PROTOCOL {
			@Override
			public String asString() {
				return "protocol";
			}
		},

		/**
		 * SOAP action of the operation, or {@value KeyValue#NONE_VALUE} if the request is
		 * not a SOAP message or does not define one.
		 */
		SOAP_ACTION {
			@Override
			public String asString() {
				return "soap.action";
			}
		}

	}

	/**
	 * High cardinality key names, suitable for use as span attributes.
	 */
	public enum HighCardinalityKeyNames implements KeyName {

		/**
		 * Fault reason of the SOAP response, or {@value KeyValue#NONE_VALUE} if the
		 * response is not a fault.
		 */
		FAULT_REASON {
			@Override
			public String asString() {
				return "fault.reason";
			}
		},

		/**
		 * URI of the service that the request was received on.
		 */
		URI {
			@Override
			public String asString() {
				return "uri";
			}
		}

	}

}
