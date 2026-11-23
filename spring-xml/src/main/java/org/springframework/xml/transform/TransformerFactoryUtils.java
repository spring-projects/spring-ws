/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.transform;

import java.lang.reflect.InvocationTargetException;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * General utilities of {@link TransformerFactory}.
 *
 * @author Greg Turnquist
 * @since 3.0.5
 */
public abstract class TransformerFactoryUtils {

	private static final Log log = LogFactory.getLog(TransformerFactoryUtils.class);

	/**
	 * Build a new {@link TransformerFactory} using the default constructor.
	 */
	public static TransformerFactory newInstance() {
		return defaultSettings(TransformerFactory.newInstance());
	}

	/**
	 * Build an {@link TransformerFactory} and prevent external entities from accessing.
	 * @see TransformerFactory#newInstance()
	 */
	public static TransformerFactory newInstance(Class<? extends TransformerFactory> transformerFactoryClass) {
		try {
			return defaultSettings(transformerFactoryClass.getDeclaredConstructor().newInstance());
		}
		catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
			throw new TransformerFactoryConfigurationError(ex,
					"Could not instantiate TransformerFactory [" + transformerFactoryClass + "]");
		}
	}

	/**
	 * Prevent external entities from accessing.
	 */
	private static TransformerFactory defaultSettings(TransformerFactory factory) {
		try {
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		}
		catch (IllegalArgumentException ex) {
			if (log.isWarnEnabled()) {
				log.warn(XMLConstants.ACCESS_EXTERNAL_DTD + " property not supported by "
						+ factory.getClass().getCanonicalName());
			}
		}

		try {
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		}
		catch (IllegalArgumentException ex) {
			if (log.isWarnEnabled()) {
				log.warn(XMLConstants.ACCESS_EXTERNAL_STYLESHEET + " property not supported by "
						+ factory.getClass().getCanonicalName());
			}
		}

		return factory;
	}

}
