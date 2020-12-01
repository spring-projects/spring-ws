/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.xml;

import javax.xml.stream.XMLInputFactory;

/**
 * @author Greg Turnquist
 */
public class XMLInputFactoryUtils {

	/**
	 * Build an {@link XMLInputFactory} and set properties to prevent external entities from accessing.
	 *
	 * @see XMLInputFactory#newInstance()
	 */
	public static XMLInputFactory newInstance() {
		XMLInputFactory factory = XMLInputFactory.newInstance();

		factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

		return factory;
	}

}
