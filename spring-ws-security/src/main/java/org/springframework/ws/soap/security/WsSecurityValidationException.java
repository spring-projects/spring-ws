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

package org.springframework.ws.soap.security;

import org.jspecify.annotations.Nullable;

/**
 * Exception indicating that something went wrong during the validation of a message.
 * <p>
 * This is a checked exception since we want it to be caught, logged and handled rather
 * than cause the application to fail. Failure to validate a message is usually not a
 * fatal problem.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public abstract class WsSecurityValidationException extends WsSecurityException {

	public WsSecurityValidationException(String msg) {
		super(msg);
	}

	public WsSecurityValidationException(@Nullable String msg, Throwable ex) {
		super(msg, ex);
	}

}
