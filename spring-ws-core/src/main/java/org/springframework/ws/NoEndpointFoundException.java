/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws;

/**
 * Exception thrown when an endpoint cannot be resolved for an incoming message request.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public final class NoEndpointFoundException extends WebServiceException {

	public NoEndpointFoundException(WebServiceMessage request) {
		super("No endpoint can be found for request [" + request + "]");
	}
}
