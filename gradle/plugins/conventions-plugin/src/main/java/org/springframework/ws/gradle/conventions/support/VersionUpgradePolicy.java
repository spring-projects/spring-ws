/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.gradle.conventions.support;

import java.util.function.BiPredicate;

/**
 * Policies used to decide which versions are considered as possible upgrades.
 *
 * @author Stephane Nicoll
 */
public enum VersionUpgradePolicy {

	/**
	 * Any version.
	 */
	ANY((current, candidate) -> true),

	/**
	 * Minor versions of the current major version.
	 */
	SAME_MAJOR_VERSION(Version::isSameMajor),

	/**
	 * Patch versions of the current minor version.
	 */
	SAME_MINOR_VERSION(Version::isSameMinor);

	private final BiPredicate<Version, Version> delegate;

	VersionUpgradePolicy(BiPredicate<Version, Version> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Specify if the {@code candidate} version is a valid upgrade according to this
	 * policy.
	 * @param current the current version
	 * @param candidate the candidate version
	 * @return {@code true} if the dependency can be upgraded to the {@code candidate}
	 * version
	 */
	public boolean isCandidate(Version current, Version candidate) {
		return this.delegate.test(current, candidate);
	}

}
