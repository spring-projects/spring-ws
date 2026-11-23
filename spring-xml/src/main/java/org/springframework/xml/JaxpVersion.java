/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml;

import org.springframework.util.ClassUtils;

/**
 * Helper class used to find the current version of JAXP. We cannot depend on the Java
 * version, since JAXP can be upgraded independently of the Java version.
 * <p>
 * Only distinguishes between JAXP 1.0, 1.1, 1.3, 1.4, and 1.5, since JAXP 1.2 was a
 * maintenance release with no new classes.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.0.0
 */
public abstract class JaxpVersion {

	/**
	 * Constant identifying JAXP 1.0.
	 */
	public static final int JAXP_10 = 0;

	/**
	 * Constant identifying JAXP 1.1.
	 */
	public static final int JAXP_11 = 1;

	/**
	 * Constant identifying JAXP 1.3.
	 */
	public static final int JAXP_13 = 3;

	/**
	 * Constant identifying JAXP 1.4.
	 */
	public static final int JAXP_14 = 4;

	/**
	 * Constant identifying JAXP 1.5.
	 */
	public static final int JAXP_15 = 5;

	private static final String JAXP_14_CLASS_NAME = "javax.xml.transform.stax.StAXSource";

	private static final String JAXP_15_CLASS_NAME = "javax.xml.validation.SchemaFactoryConfigurationError";

	private static int jaxpVersion;

	static {
		ClassLoader classLoader = JaxpVersion.class.getClassLoader();

		try {
			ClassUtils.forName(JAXP_15_CLASS_NAME, classLoader);
			jaxpVersion = JAXP_15;
		}
		catch (ClassNotFoundException ex) {
			try {
				ClassUtils.forName(JAXP_14_CLASS_NAME, classLoader);
				jaxpVersion = JAXP_14;
			}
			catch (ClassNotFoundException ex2) {
				// leave 1.3 as default (it's either 1.3 or unknown)
				jaxpVersion = JAXP_13;
			}
		}
	}

	/**
	 * Gets the JAXP version. This means we can do things like if
	 * {@code (getJaxpVersion() < JAXP_13)}.
	 * @return a code comparable to the JAXP_XX codes in this class
	 * @see #JAXP_10
	 * @see #JAXP_11
	 * @see #JAXP_13
	 * @see #JAXP_14
	 * @see #JAXP_15
	 */
	public static int getJaxpVersion() {
		return jaxpVersion;
	}

	/**
	 * Convenience method to determine if the current JAXP version is at least 1.4
	 * (packaged with JDK 1.6).
	 * @return {@code true} if the current JAXP version is at least JAXP 1.4
	 * @see #getJaxpVersion()
	 * @see #JAXP_14
	 */
	public static boolean isAtLeastJaxp14() {
		return getJaxpVersion() >= JAXP_14;
	}

}
