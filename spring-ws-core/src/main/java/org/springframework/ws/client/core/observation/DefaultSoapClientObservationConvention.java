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

import javax.xml.namespace.QName;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;

import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.observation.SoapClientObservationDocumentation.HighCardinalityKeyNames;
import org.springframework.ws.client.core.observation.SoapClientObservationDocumentation.LowCardinalityKeyNames;

/**
 * Default {@link SoapClientObservationConvention} implementation.
 *
 * @author Stephane Nicoll
 * @since 5.1.0
 */
public class DefaultSoapClientObservationConvention implements SoapClientObservationConvention {

	private static final String DEFAULT_NAME = "soap.client.requests";

	private static final KeyValue EXCEPTION_NONE = KeyValue.of(LowCardinalityKeyNames.EXCEPTION, KeyValue.NONE_VALUE);

	private static final KeyValue FAULT_CODE_NONE = KeyValue.of(LowCardinalityKeyNames.FAULT_CODE, KeyValue.NONE_VALUE);

	private static final KeyValue NAMESPACE_NONE = KeyValue.of(LowCardinalityKeyNames.NAMESPACE, KeyValue.NONE_VALUE);

	private static final KeyValue OPERATION_NAME_NONE = KeyValue.of(LowCardinalityKeyNames.OPERATION_NAME,
			KeyValue.NONE_VALUE);

	private static final KeyValue PROTOCOL_NONE = KeyValue.of(LowCardinalityKeyNames.PROTOCOL, KeyValue.NONE_VALUE);

	private static final KeyValue SOAP_ACTION_NONE = KeyValue.of(LowCardinalityKeyNames.SOAP_ACTION,
			KeyValue.NONE_VALUE);

	private static final KeyValue OUTCOME_SUCCESS = KeyValue.of(LowCardinalityKeyNames.OUTCOME, "SUCCESS");

	private static final KeyValue OUTCOME_FAULT = KeyValue.of(LowCardinalityKeyNames.OUTCOME, "FAULT");

	private static final KeyValue OUTCOME_ERROR = KeyValue.of(LowCardinalityKeyNames.OUTCOME, "ERROR");

	private static final KeyValue OUTCOME_UNKNOWN = KeyValue.of(LowCardinalityKeyNames.OUTCOME, "UNKNOWN");

	private static final KeyValue FAULT_REASON_NONE = KeyValue.of(HighCardinalityKeyNames.FAULT_REASON,
			KeyValue.NONE_VALUE);

	private static final KeyValue URI_NONE = KeyValue.of(HighCardinalityKeyNames.URI, KeyValue.NONE_VALUE);

	private final String name;

	/**
	 * Create a convention with the default name {@code soap.client.requests}.
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
	public String getName() {
		return this.name;
	}

	@Override
	public String getContextualName(SoapClientObservationContext context) {
		String operationName = context.getOperationName();
		return (operationName != null) ? "soap " + operationName : "soap";
	}

	@Override
	public KeyValues getLowCardinalityKeyValues(SoapClientObservationContext context) {
		return KeyValues.of(exception(context), faultCode(context), namespace(context), operationName(context),
				outcome(context), protocol(context), soapAction(context));
	}

	private KeyValue exception(SoapClientObservationContext context) {
		Throwable error = context.getError();
		return (error != null) ? KeyValue.of(LowCardinalityKeyNames.EXCEPTION, error.getClass().getSimpleName())
				: EXCEPTION_NONE;
	}

	private KeyValue faultCode(SoapClientObservationContext context) {
		if (context.getResponse() instanceof FaultAwareWebServiceMessage response) {
			QName faultCode = response.getFaultCode();
			if (faultCode != null) {
				return KeyValue.of(LowCardinalityKeyNames.FAULT_CODE, faultCode.toString());
			}
		}
		return FAULT_CODE_NONE;
	}

	private KeyValue namespace(SoapClientObservationContext context) {
		String namespace = context.getNamespace();
		return (namespace != null) ? KeyValue.of(LowCardinalityKeyNames.NAMESPACE, namespace) : NAMESPACE_NONE;
	}

	private KeyValue operationName(SoapClientObservationContext context) {
		String operationName = context.getOperationName();
		return (operationName != null) ? KeyValue.of(LowCardinalityKeyNames.OPERATION_NAME, operationName)
				: OPERATION_NAME_NONE;
	}

	private KeyValue outcome(SoapClientObservationContext context) {
		WebServiceMessage response = context.getResponse();
		if (response instanceof FaultAwareWebServiceMessage faultResponse && faultResponse.hasFault()) {
			return OUTCOME_FAULT;
		}
		if (context.getError() != null) {
			return OUTCOME_ERROR;
		}
		return (response != null) ? OUTCOME_SUCCESS : OUTCOME_UNKNOWN;
	}

	private KeyValue protocol(SoapClientObservationContext context) {
		URI uri = context.getUri();
		return (uri != null) ? KeyValue.of(LowCardinalityKeyNames.PROTOCOL, uri.getScheme()) : PROTOCOL_NONE;
	}

	private KeyValue soapAction(SoapClientObservationContext context) {
		String soapAction = context.getSoapAction();
		return (soapAction != null) ? KeyValue.of(LowCardinalityKeyNames.SOAP_ACTION, soapAction) : SOAP_ACTION_NONE;
	}

	@Override
	public KeyValues getHighCardinalityKeyValues(SoapClientObservationContext context) {
		return KeyValues.of(faultReason(context), uri(context));
	}

	private KeyValue faultReason(SoapClientObservationContext context) {
		if (context.getResponse() instanceof FaultAwareWebServiceMessage response) {
			String faultReason = response.getFaultReason();
			if (faultReason != null) {
				return KeyValue.of(HighCardinalityKeyNames.FAULT_REASON, faultReason);
			}
		}
		return FAULT_REASON_NONE;
	}

	private KeyValue uri(SoapClientObservationContext context) {
		URI uri = context.getUri();
		return (uri != null) ? KeyValue.of(HighCardinalityKeyNames.URI, uri.toString()) : URI_NONE;
	}

}
