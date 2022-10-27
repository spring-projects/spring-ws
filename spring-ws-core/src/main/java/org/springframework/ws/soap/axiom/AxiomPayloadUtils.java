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
package org.springframework.ws.soap.axiom;

import static org.springframework.ws.soap.axiom.support.AxiomUtils.AXIOM14_IS_PRESENT;

import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;

/**
 * Utility to create various {@link Payload}s for Axiom.
 *
 * @author Greg Turnquist
 * @since 3.1
 */
final class AxiomPayloadUtils {

	private AxiomPayloadUtils() {
		throw new RuntimeException("Utility class not meant to be instantiated.");
	}

	static Payload createCachingPayload(SOAPBody axiomBody, SOAPFactory axiomFactory) {
		if (AXIOM14_IS_PRESENT()) {
			return new Axiom14CachingPayload(axiomBody, axiomFactory);
		} else {
			return new Axiom12CachingPayload(axiomBody, axiomFactory);
		}
	}

	static Payload createNonCachingPayload(SOAPBody axiomBody, SOAPFactory axiomFactory) {
		if (AXIOM14_IS_PRESENT()) {
			return new Axiom14NonCachingPayload(axiomBody, axiomFactory);
		} else {
			return new Axiom12NonCachingPayload(axiomBody, axiomFactory);
		}
	}
}
