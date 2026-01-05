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

import org.springframework.ws.client.core.observation.ClientWebServiceObservationDocumentation.HighCardinalityKeyNames;
import org.springframework.ws.client.core.observation.ClientWebServiceObservationDocumentation.LowCardinalityKeyNames;

/**
 * Default implementation for a {@link ClientWebServiceObservationConvention}, extracting
 * information from the {@link ClientWebServiceObservationConvention}.
 *
 * @author Stephane Nicoll
 * @since 5.1.0
 */
public class DefaultClientWebServiceObservationConvention implements ClientWebServiceObservationConvention {

	private static final String DEFAULT_NAME = "soap.client";

	private final String name;

	public DefaultClientWebServiceObservationConvention() {
		this(DEFAULT_NAME);
	}

	public DefaultClientWebServiceObservationConvention(String name) {
		this.name = name;
	}

	@Override
	public @Nullable String getName() {
		return this.name;
	}

	@Override
	public KeyValues getLowCardinalityKeyValues(ClientWebServiceObservationContext context) {
		return KeyValues.of(location(context), namespace(context), protocol(context), operationName(context));
	}

	protected KeyValue location(ClientWebServiceObservationContext context) {
		// FIXME
		return KeyValue.of(LowCardinalityKeyNames.LOCATION, KeyValue.NONE_VALUE);
	}

	protected KeyValue namespace(ClientWebServiceObservationContext context) {
		// FIXME
		return KeyValue.of(LowCardinalityKeyNames.NAMESPACE, KeyValue.NONE_VALUE);
	}

	protected KeyValue protocol(ClientWebServiceObservationContext context) {
		URI uri = context.getUri();
		String protocol = (uri != null) ? uri.getScheme() : KeyValue.NONE_VALUE;
		return KeyValue.of(LowCardinalityKeyNames.PROTOCOL, protocol);
	}

	protected KeyValue operationName(ClientWebServiceObservationContext context) {
		// FIXME
		return KeyValue.of(LowCardinalityKeyNames.OPERATION_NAME, KeyValue.NONE_VALUE);
	}

	protected KeyValue faultCode(ClientWebServiceObservationContext context) {
		// FIXME
		return KeyValue.of(LowCardinalityKeyNames.FAULT_CODE, KeyValue.NONE_VALUE);
	}

	@Override
	public KeyValues getHighCardinalityKeyValues(ClientWebServiceObservationContext context) {
		return KeyValues.of(uri(context));
	}

	protected KeyValue uri(ClientWebServiceObservationContext context) {
		URI uri = context.getUri();
		String value = (uri != null) ? uri.toString() : KeyValue.NONE_VALUE;
		return KeyValue.of(HighCardinalityKeyNames.URI, value);
	}

}
