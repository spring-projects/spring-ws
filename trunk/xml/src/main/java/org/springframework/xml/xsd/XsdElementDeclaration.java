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

package org.springframework.xml.xsd;

import javax.xml.namespace.QName;

/**
 * Represents an abstraction for an XSD element declaration.
 *
 * @author Arjen Poutsma
 * @see XsdSchemaDocument#getElementDeclarations()
 * @since 1.0.2
 */
public interface XsdElementDeclaration {

    /** Returns the qualified name of the element declaration. */
    QName getName();

}
