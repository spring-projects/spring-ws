/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.client.core;

import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceFaultException;

/**
 * Simple fault resolver that simply throws a {@link WebServiceFaultException} when a fault occurs.
 *
 * @author Arjen Poutsma
 * @see WebServiceFaultException
 * @since 1.0.0
 */
public class SimpleFaultMessageResolver implements FaultMessageResolver {

	/** Throws a new {@code WebServiceFaultException}. */
	@Override
	public void resolveFault(WebServiceMessage message) {
		if (message instanceof FaultAwareWebServiceMessage) {
			throw new WebServiceFaultException((FaultAwareWebServiceMessage) message);
		}
		else {
			throw new WebServiceFaultException("Message has unknown fault: " + message);
		}
	}
}
