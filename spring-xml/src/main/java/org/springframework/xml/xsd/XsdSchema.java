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

package org.springframework.xml.xsd;

import javax.xml.transform.Source;

import org.springframework.xml.validation.XmlValidator;

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
	 * Returns the {@link Source} of the schema.
	 *
	 * @return the source of this XSD schema
	 */
	Source getSource();

	/**
	 * Creates a {@link XmlValidator} based on the schema.
	 *
	 * @return a validator for this schema
	 */
	XmlValidator createValidator();
}
