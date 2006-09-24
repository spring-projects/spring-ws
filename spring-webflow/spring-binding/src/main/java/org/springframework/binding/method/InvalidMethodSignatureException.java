/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.binding.method;

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown when a method key could not be resolved to an invokable java Method on
 * a Class.
 * 
 * @author Keith Donald
 */
public class InvalidMethodSignatureException extends NestedRuntimeException {

	/**
	 * Creates an exception for the specified class method key with the
	 * specified root cause.
	 * 
	 * @param methodKey the method key
	 * @param cause the cause
	 */
	public InvalidMethodSignatureException(ClassMethodKey signature, Exception cause) {
		super("Could not resolve method with signature " + signature, cause);
	}
}