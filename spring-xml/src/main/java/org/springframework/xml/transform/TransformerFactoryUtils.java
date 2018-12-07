/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.xml.transform;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

/**
 * @author Greg Turnquist
 * @since 3.0.5
 */
public class TransformerFactoryUtils {

	/**
	 * Build a new {@link TransformerFactory} using the default constructor.
	 */
	public static TransformerFactory newInstance() {
		return defaultSettings(TransformerFactory.newInstance());
	}

	/**
	 * Build an {@link TransformerFactory} and prevent external entities from accessing.
	 *
	 * @see TransformerFactory#newInstance()
	 */
	public static TransformerFactory newInstance(Class<? extends TransformerFactory> transformerFactoryClass) {
		try {
			return defaultSettings(transformerFactoryClass.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			throw new TransformerFactoryConfigurationError(e,
					"Could not instantiate TransformerFactory [" + transformerFactoryClass + "]");
		}
	}

	/**
	 * Prevent external entities from accessing.
	 */
	private static TransformerFactory defaultSettings(TransformerFactory factory) {
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		return factory;
	}
}
