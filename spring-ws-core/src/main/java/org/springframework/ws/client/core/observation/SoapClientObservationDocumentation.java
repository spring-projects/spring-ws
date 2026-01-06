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

package org.springframework.ws.client.core.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

import org.springframework.ws.WebServiceMessage;

/**
 * Documented {@link io.micrometer.common.KeyValue KeyValues} for {@link WebServiceMessage
 * SOAP client} observations.
 *
 * @author Stephane Nicoll
 * @since 5.1.0
 */
public enum SoapClientObservationDocumentation implements ObservationDocumentation {

	/**
	 * SOAP request observations for clients.
	 */
	SOAP_CLIENT_REQUESTS {
		@Override
		public Class<? extends ObservationConvention<? extends Context>> getDefaultConvention() {
			return DefaultSoapClientObservationConvention.class;
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

	public enum LowCardinalityKeyNames implements KeyName {

		/**
		 * Fault code for the SOAP response.
		 */
		FAULT_CODE {
			@Override
			public String asString() {
				return "fault.code";
			}

		},

		/**
		 * Target namespace of the operation.
		 */
		NAMESPACE {
			@Override
			public String asString() {
				return "namespace";
			}
		},

		/**
		 * Name of the operation, as defined by {@code wsdl:operation name}.
		 */
		OPERATION_NAME {
			@Override
			public String asString() {
				return "operation.name";
			}
		},

		/**
		 * Name of the protocol used to perform the operation.
		 */
		PROTOCOL {
			@Override
			public String asString() {
				return "protocol";
			}
		}

	}

	public enum HighCardinalityKeyNames implements KeyName {

		/**
		 * Fault reason for the SOAP response.
		 */
		FAULT_REASON {
			@Override
			public String asString() {
				return "fault.reason";
			}

		},

		/**
		 * Target URI.
		 */
		URL {
			@Override
			public String asString() {
				return "url";
			}
		}

	}

}
