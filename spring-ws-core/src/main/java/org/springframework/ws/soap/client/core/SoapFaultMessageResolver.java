/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.soap.client.core;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.FaultMessageResolver;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;

/**
 * Simple fault resolver that simply throws a {@link SoapFaultClientException} when a fault occurs.
 *
 * @author Arjen Poutsma
 * @see SoapFaultClientException
 * @since 1.0.0
 */
public class SoapFaultMessageResolver implements FaultMessageResolver {

	@Override
	public void resolveFault(WebServiceMessage message) throws IOException {
		SoapMessage soapMessage = (SoapMessage) message;
		throw new SoapFaultClientException(soapMessage);
	}
}
