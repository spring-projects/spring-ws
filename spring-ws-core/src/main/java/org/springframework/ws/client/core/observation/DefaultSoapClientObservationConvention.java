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

import java.net.URI;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.client.core.observation.SoapClientObservationDocumentation.HighCardinalityKeyNames;
import org.springframework.ws.client.core.observation.SoapClientObservationDocumentation.LowCardinalityKeyNames;

/**
 * Default implementation for a {@link SoapClientObservationConvention}, extracting
 * information from the {@link SoapClientObservationConvention}.
 *
 * @author Stephane Nicoll
 * @since 5.1.0
 */
public class DefaultSoapClientObservationConvention implements SoapClientObservationConvention {

	private static final String DEFAULT_NAME = "soap.client.requests";

	private final String name;

	/**
	 * Create a convention with the default name {@value DEFAULT_NAME}.
	 */
	public DefaultSoapClientObservationConvention() {
		this(DEFAULT_NAME);
	}

	/**
	 * Create a convention with a custom name.
	 * @param name the observation name
	 */
	public DefaultSoapClientObservationConvention(String name) {
		this.name = name;
	}

	@Override
	public @Nullable String getName() {
		return this.name;
	}

	@Override
	public KeyValues getLowCardinalityKeyValues(SoapClientObservationContext context) {
		return KeyValues.of(faultCode(context), namespace(context), operationName(context), protocol(context));
	}

	protected KeyValue faultCode(SoapClientObservationContext context) {
		// FIXME
		return KeyValue.of(LowCardinalityKeyNames.FAULT_CODE, KeyValue.NONE_VALUE);
	}

	protected KeyValue namespace(SoapClientObservationContext context) {
		// FIXME
		return KeyValue.of(LowCardinalityKeyNames.NAMESPACE, KeyValue.NONE_VALUE);
	}

	protected KeyValue operationName(SoapClientObservationContext context) {
		// FIXME
		return KeyValue.of(LowCardinalityKeyNames.OPERATION_NAME, KeyValue.NONE_VALUE);
	}

	protected KeyValue protocol(SoapClientObservationContext context) {
		URI uri = context.getUri();
		String protocol = (uri != null) ? uri.getScheme() : KeyValue.NONE_VALUE;
		return KeyValue.of(LowCardinalityKeyNames.PROTOCOL, protocol);
	}

	@Override
	public KeyValues getHighCardinalityKeyValues(SoapClientObservationContext context) {
		return KeyValues.of(faultReason(context), uri(context));
	}

	protected KeyValue faultReason(SoapClientObservationContext context) {
		// FIXME
		return KeyValue.of(HighCardinalityKeyNames.FAULT_REASON, KeyValue.NONE_VALUE);
	}

	protected KeyValue uri(SoapClientObservationContext context) {
		URI uri = context.getUri();
		String value = (uri != null) ? uri.toString() : KeyValue.NONE_VALUE;
		return KeyValue.of(HighCardinalityKeyNames.URL, value);
	}

}
