/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.soap.addressing.server.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If this annotation is applied, the
 * {@link org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getMessageId()}
 * is marked as optional and is not checked in
 * {@link org.springframework.ws.soap.addressing.version.AddressingVersion#hasRequiredProperties(org.springframework.ws.soap.addressing.core.MessageAddressingProperties, Object)
 * hasRequiredProperties}
 * 
 * This annotation only MUST be applied if the endpoint method don't send any
 * response or throws any exception.
 *
 * @author David Goicocheta
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OptionalMessageId {

}
