/*
 * Copyright 2005-present the original author or authors.
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

import java.io.Serializable;
import java.util.Objects;

/**
 * ë A version representation that provides a major and minor identifier. For instance,
 * {@code 1.2.5} would have a {@code major} of "1" and {@code minor} of "1.2".
 *
 * @author Stephane Nicoll
 */
public class Version {

	private final String id;

	private final String major;

	private final String minor;

	private final Qualifier qualifier;

	private final Parts parts;

	Version(String id, String major, String minor, Qualifier qualifer, Parts parts) {
		this.id = id;
		this.major = major;
		this.minor = minor;
		this.qualifier = qualifer;
		this.parts = parts;
	}

	public static Version from(String version) {
		return VersionParser.safeParse(version);
	}

	/**
	 * Return the version.
	 * @return the version
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Return the major qualifier or {@code null}.
	 * @return the major
	 */
	public String getMajor() {
		return this.major;
	}

	/**
	 * Return the minor qualifier or {@code null}.
	 * @return the minor
	 */
	public String getMinor() {
		return this.minor;
	}

	/**
	 * Return tue {@link Qualifier} or {@code null}.
	 * @return the qualifier
	 */
	public Qualifier getQualifier() {
		return this.qualifier;
	}

	/**
	 * Return the elements of the version, if any. Does not apply for non-numeric version
	 * such as a release train.
	 * @return the parts
	 */
	public Parts getParts() {
		return this.parts;
	}

	/**
	 * Returns whether this version has the same major and minor versions as the
	 * {@code other} version.
	 * @param other the version to test
	 * @return {@code true} if this version has the same major and minor, otherwise
	 * {@code false}
	 */
	public boolean isSameMinor(Version other) {
		return isSameMajor(other) && Objects.equals(this.parts.minor, other.parts.minor);
	}

	/**
	 * Returns whether this version has the same major version as the {@code other}
	 * version.
	 * @param other the version to test
	 * @return {@code true} if this version has the same major, otherwise {@code false}
	 */
	public boolean isSameMajor(Version other) {
		return (this.parts != null && other.parts != null && Objects.equals(this.parts.major, other.parts.major));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Version version)) {
			return false;
		}
		return Objects.equals(this.id, version.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.id);
	}

	@Override
	public String toString() {
		return this.id;
	}

	public record Parts(Integer major, Integer minor, Integer patch, Integer hotPatch) {

		/**
		 * Return a unique number for this instance that allows to compare two versions.
		 * @return a comparable number
		 */
		public long toNumber() {
			String paddedValue = paddedNumber(this.major) + paddedNumber(this.minor) + paddedNumber(this.patch)
					+ paddedNumber(this.hotPatch);
			return Long.parseLong(paddedValue);
		}

		private String paddedNumber(Integer number) {
			if (number != null) {
				return String.format("%02d", number);
			}
			return "00";
		}

	}

	/**
	 * A version qualifier.
	 *
	 * @param id the identifier of the qualifier
	 * @param version the version or {@code null}
	 * @param separator the separator
	 */
	public record Qualifier(String id, Integer version, String separator) implements Serializable {

		public Qualifier(String id) {
			this(id, null, ".");
		}

	}

}
