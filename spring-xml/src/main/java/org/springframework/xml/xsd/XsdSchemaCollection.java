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

package org.springframework.xml.xsd;

import java.io.IOException;

import org.springframework.xml.validation.XmlValidator;

/**
 * Represents an abstraction for a collection of XSD schemas.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public interface XsdSchemaCollection {

    /**
     * Returns all schemas contained in this collection.
     *
     * @return the schemas contained in this collection
     */
    XsdSchema[] getXsdSchemas();

    /**
     * Creates a {@link XmlValidator} based on the schemas contained in this collection.
     *
     * @return a validator for this collection
     * @throws IOException in case of I/O errors
     */
    XmlValidator createValidator() throws IOException;

}
