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

package org.springframework.xml.xpath;

import org.springframework.xml.XmlException;

/**
 * Exception thrown when an error occurs during XPath processing.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class XPathException extends XmlException {

    /**
     * Constructs a new instance of the {@code XPathException} with the specific detail message.
     *
     * @param message the detail message
     */
    public XPathException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of the {@code XPathException} with the specific detail message and exception.
     *
     * @param message   the detail message
     * @param throwable the wrapped exception
     */
    public XPathException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
