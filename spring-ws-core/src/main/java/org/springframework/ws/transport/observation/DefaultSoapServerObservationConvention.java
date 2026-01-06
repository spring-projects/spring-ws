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

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.observation.SoapServerObservationDocumentation.HighCardinalityKeyNames;
import org.springframework.ws.transport.observation.SoapServerObservationDocumentation.LowCardinalityKeyNames;

/**
 * Default {@link SoapServerObservationConvention}.
 *
 * @author Brian Clozel
 * @since 5.1.0
 */
public class DefaultSoapServerObservationConvention extends SoapServerObservationConvention {

	private static final String DEFAULT_NAME = "soap.server.duration";

	private final String name;

	/**
	 * Create a convention with the default name {@code "http.server.requests"}.
	 */
	public DefaultSoapServerObservationConvention() {
		this(DEFAULT_NAME);
	}

	/**
	 * Create a convention with a custom name.
	 * @param name the observation name
	 */
	public DefaultSoapServerObservationConvention(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getContextualName(SoapServerObservationContext context) {
		if (context.getMethodName() != null) {
			return "soap " + context.getMethodName();
		}
		return "soap";
	}

	@Override
	public KeyValues getLowCardinalityKeyValues(SoapServerObservationContext context) {
		return KeyValues.of(faultCode(context), method(context), address(context), protocol(context), service(context));
	}

	private KeyValue faultCode(SoapServerObservationContext context) {
		WebServiceMessage response = context.getResponse();
		if (response instanceof FaultAwareWebServiceMessage faultResponse) {
			QName faultCode = faultResponse.getFaultCode();
			if (faultCode != null) {
				return KeyValue.of(LowCardinalityKeyNames.FAULT_CODE, faultCode.toString());
			}
		}
		return KeyValue.of(LowCardinalityKeyNames.FAULT_CODE, KeyValue.NONE_VALUE);
	}

	private KeyValue method(SoapServerObservationContext context) {
		if (context.getMethodName() != null) {
			return KeyValue.of(LowCardinalityKeyNames.METHOD, context.getMethodName());
		}
		return KeyValue.of(LowCardinalityKeyNames.METHOD, KeyValue.NONE_VALUE);
	}

	private KeyValue address(SoapServerObservationContext context) {
		URI uri = getConnectionURI(context);
		if (uri != null) {
			return KeyValue.of(LowCardinalityKeyNames.ADDRESS, uri.getHost());
		}
		return KeyValue.of(LowCardinalityKeyNames.ADDRESS, KeyValue.NONE_VALUE);
	}

	private KeyValue protocol(SoapServerObservationContext context) {
		URI uri = getConnectionURI(context);
		if (uri != null) {
			return KeyValue.of(LowCardinalityKeyNames.PROTOCOL, uri.getScheme());
		}
		return KeyValue.of(LowCardinalityKeyNames.PROTOCOL, KeyValue.NONE_VALUE);
	}

	private KeyValue service(SoapServerObservationContext context) {
		String serviceName = context.getServiceName();
		if (serviceName != null) {
			return KeyValue.of(LowCardinalityKeyNames.SERVICE, serviceName);
		}
		return KeyValue.of(LowCardinalityKeyNames.SERVICE, KeyValue.NONE_VALUE);
	}

	private @Nullable URI getConnectionURI(SoapServerObservationContext context) {
		try {
			return context.getConnection().getUri();
		}
		catch (URISyntaxException exc) {
			return null;
		}
	}

	@Override
	public KeyValues getHighCardinalityKeyValues(SoapServerObservationContext context) {
		return KeyValues.of(faultReason(context), uri(context));
	}

	private KeyValue uri(SoapServerObservationContext context) {
		URI uri = getConnectionURI(context);
		if (uri != null) {
			return KeyValue.of(HighCardinalityKeyNames.URL, uri.toString());
		}
		return KeyValue.of(HighCardinalityKeyNames.URL, KeyValue.NONE_VALUE);
	}

	private KeyValue faultReason(SoapServerObservationContext context) {
		WebServiceMessage response = context.getResponse();
		if (response instanceof FaultAwareWebServiceMessage faultResponse) {
			String faultReason = faultResponse.getFaultReason();
			if (faultReason != null) {
				return KeyValue.of(HighCardinalityKeyNames.FAULT_REASON, faultReason);
			}
		}
		return KeyValue.of(HighCardinalityKeyNames.FAULT_REASON, KeyValue.NONE_VALUE);
	}

}
