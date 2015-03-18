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

package org.springframework.ws.soap;

/**
 * Exception thrown when a SOAP header could not be accessed.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SoapHeaderException extends SoapMessageException {

	public SoapHeaderException(String msg) {
		super(msg);
	}

	public SoapHeaderException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public SoapHeaderException(Throwable ex) {
		super("Could not access header: " + ex.getMessage(), ex);
	}

}
