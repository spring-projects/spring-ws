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

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

/**
 * Represents an abstraction for XSD schemas.
 *
 * @author Mark LaFond
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public interface XsdSchema {

    /**
     * Returns the target namespace of this schema.
     *
     * @return the target namespace
     */
    String getTargetNamespace();

    /**
     * Returns the qualified names of all top-level elements declared in the schema. This excludes elements declared as
     * child of another <code>element</code>, <code>simplyType</code>, or <code>complexType</code>.
     *
     * @return the top-level element names
     */
    QName[] getElementNames();

    /**
     * Returns the <code>Source</code> of the schema.
     *
     * @return the <code>Source</code> of this XSD schema
     */
    Source getSource();
}
