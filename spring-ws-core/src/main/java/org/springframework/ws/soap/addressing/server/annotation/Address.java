/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.soap.addressing.server.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an endpoint with a WS-Addressing {@code Address}. If this annotation is
 * applied, the {@linkplain #value() value} is compared to the
 * {@link org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getTo()
 * destination} property of the incoming message.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Address {

	/**
	 * Signifies the value for the request WS-Addressing destination that
	 * is handled by the class.
	 */
	String value();

}
