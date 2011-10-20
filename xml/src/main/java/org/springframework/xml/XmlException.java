/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.xml;

import org.springframework.core.NestedRuntimeException;

/**
 * Root of the hierarchy of XML exception.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class XmlException extends NestedRuntimeException {

    /**
     * Constructs a new instance of the <code>XmlException</code> with the specific detail message.
     *
     * @param message the detail message
     */
    protected XmlException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of the <code>XmlException</code> with the specific detail message and exception.
     *
     * @param message   the detail message
     * @param throwable the wrapped exception
     */
    protected XmlException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
