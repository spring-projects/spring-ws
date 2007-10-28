/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.xml.schema;

import org.springframework.xml.XmlException;

/**
 * Base class for all schema exceptions.
 *
 * @author Arjen Poutsma
 * @since 1.0.2
 */
public class SchemaException extends XmlException {

    public SchemaException(String message) {
        super(message);
    }

    public SchemaException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
