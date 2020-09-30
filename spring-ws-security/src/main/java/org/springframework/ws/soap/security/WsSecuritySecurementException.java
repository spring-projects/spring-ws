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

package org.springframework.ws.soap.security;

/**
 * Exception indicating that something went wrong during the securement of a message.
 * <p>
 * This is a checked exception since we want it to be caught, logged and handled rather than cause the application to
 * fail. Failure to secure a message is usually not a fatal problem.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public abstract class WsSecuritySecurementException extends WsSecurityException {

	public WsSecuritySecurementException(String msg) {
		super(msg);
	}

	public WsSecuritySecurementException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
