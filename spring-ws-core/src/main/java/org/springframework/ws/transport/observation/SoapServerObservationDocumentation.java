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

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * Documented {@link io.micrometer.common.KeyValue KeyValues} for the SOAP server
 * observations.
 *
 * @author Brian Clozel
 * @since 5.1.0
 */
public enum SoapServerObservationDocumentation implements ObservationDocumentation {

	/**
	 * SOAP request observations for servers.
	 */
	SOAP_SERVER_DURATION {
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

	public enum LowCardinalityKeyNames implements KeyName {

		/**
		 * Fault code for the SOAP response.
		 */
		FAULT_CODE {
			@Override
			public String asString() {
				return "soap.fault.code";
			}

		},

		/**
		 * Name of the protocol used for reaching out to the server.
		 */
		PROTOCOL {
			@Override
			public String asString() {
				return "soap.server.address.protocol";
			}

		},

		/**
		 * Name of the address used by the client to reach out to the server.
		 */
		ADDRESS {
			@Override
			public String asString() {
				return "soap.server.address.name";
			}
		},

		/**
		 * Name of the SOAP method called by the current request.
		 */
		METHOD {
			@Override
			public String asString() {
				return "soap.method";
			}
		},

		/**
		 * Target namespace URI for the SOAP service.
		 */
		SERVICE {
			@Override
			public String asString() {
				return "soap.service";
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
				return "soap.fault.reason";
			}

		},

		/**
		 * The URL of the server.
		 */
		URL {
			@Override
			public String asString() {
				return "soap.server.address.url";
			}
		}

	}

}
