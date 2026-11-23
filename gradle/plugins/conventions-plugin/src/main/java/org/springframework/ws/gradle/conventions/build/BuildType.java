/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.gradle.conventions.build;

import java.util.Locale;

/**
 * The type of build being performed.
 *
 * @author Phillip Webb
 */
public enum BuildType {

	/**
	 * An open source build.
	 */
	OPEN_SOURCE,

	/**
	 * A commercial build.
	 */
	COMMERCIAL;

	public String toIdentifier() {
		return toString().replace("_", "").toLowerCase(Locale.ROOT);
	}

}
