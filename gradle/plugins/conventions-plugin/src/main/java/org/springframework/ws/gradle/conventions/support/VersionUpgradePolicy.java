/*
 * Copyright 2005-2025 the original author or authors.
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
