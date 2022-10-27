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

import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Defines the contract for payloads.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.5.2
 */
interface Payload {

	/**
	 * Returns the source of the payload.
	 *
	 * @return the source of the payload
	 */
	Source getSource();

	/**
	 * Returns the result of the payload.
	 *
	 * @return the result of the payload
	 */
	Result getResult();
}
