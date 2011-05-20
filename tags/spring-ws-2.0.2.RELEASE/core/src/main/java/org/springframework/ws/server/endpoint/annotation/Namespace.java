/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.server.endpoint.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.xml.XMLConstants;

/**
 * Sets up a namespace to be used in an {@link Endpoint @Endpoint} method, class, or package.
 * <p/>
 * Typically used in combination with {@link XPathParam @XPathParam}, or {@link PayloadRoot @PayloadRoot}.
 *
 * @author Arjen Poutsma
 * @see XPathParam
 * @see PayloadRoot
 * @since 2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface Namespace {

    /**
     * Signifies the prefix of the namespace.
     *
     * @see #uri()
     */
    String prefix() default XMLConstants.DEFAULT_NS_PREFIX;

    /**
     * Signifies the URI of the namespace.
     *
     * @see #prefix()
     */
    String uri();

}
