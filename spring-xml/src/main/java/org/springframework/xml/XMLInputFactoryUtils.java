/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml;

import javax.xml.stream.XMLInputFactory;

/**
 * General utilities for {@link XMLInputFactory}.
 *
 * @author Greg Turnquist
 */
public abstract class XMLInputFactoryUtils {

	/**
	 * Build an {@link XMLInputFactory} and set properties to prevent external entities
	 * from accessing.
	 * @see XMLInputFactory#newInstance()
	 */
	public static XMLInputFactory newInstance() {
		XMLInputFactory factory = XMLInputFactory.newInstance();

		factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

		return factory;
	}

}
