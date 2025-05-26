/*
 * Copyright 2012-2023 the original author or authors.
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

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.ws.gradle.conventions.support.Version.Parts;
import org.springframework.ws.gradle.conventions.support.Version.Qualifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link VersionParser}.
 *
 * @author Stephane Nicoll
 */
class VersionParserTests {

	@Test
	void parseSimpleVersion() {
		assertVersion("1.2.3.RELEASE", "1", "1.2", new Qualifier("RELEASE"), new Parts(1, 2, 3, null));
	}

	@Test
	void parseVersionWithDashedQualifier() {
		assertVersion("2.0.0-M1", "2", "2.0", new Qualifier("M", 1, "-"), new Parts(2, 0, 0, null));
	}

	@Test
	void parseNoQualifier() {
		assertVersion("2.1.4", "2", "2.1", null, new Parts(2, 1, 4, null));
	}

	@Test
	void parseCommercialRevision() {
		assertVersion("2.7.19.1", "2", "2.7", null, new Parts(2, 7, 19, 1));
	}

	@Test
	void parseWithQuestionMarkPrefix() {
		assertVersion("?2.1.4", "2.1.4", "2", "2.1", null, new Parts(2, 1, 4, null));
	}

	@Test
	void parseWithEncodedVersion() {
		assertVersion("4%2E1%2E6%2ERELEASE", "4.1.6.RELEASE", "4", "4.1", new Qualifier("RELEASE", null, "."),
				new Parts(4, 1, 6, null));
	}

	@Test
	void parseWithMajorMinorOnly() {
		assertVersion("2.0", "2", "2.0", null, new Parts(2, 0, null, null));
	}

	@Test
	void parseReleaseTrain() {
		assertVersion("Gosling-SR1", null, "Gosling", null);
	}

	@Test
	void parseCalVer() {
		assertVersion("2000.0.1", null, "2000.0", null, new Parts(2000, 0, 1, null));
	}

	@Test
	void parseCalVerWithQualifier() {
		assertVersion("2020.1.0-RC1", null, "2020.1", new Qualifier("RC", 1, "-"), new Parts(2020, 1, 0, null));
	}

	@Test
	void parseVersionProperty() {
		assertVersion("${spring.version}", null, null, null);
	}

	@Test
	void versionNumberWithMajorMinor() {
		assertThat(new Parts(2, 12, null, null).toNumber()).isEqualTo(2120000);
	}

	@Test
	void versionNumberWithMajorMinorPatch() {
		assertThat(new Parts(2, 12, 5, null).toNumber()).isEqualTo(2120500);
	}

	@Test
	void versionNumberWithMajorMinorPatchHotPatch() {
		assertThat(new Parts(2, 12, 5, 1).toNumber()).isEqualTo(2120501);
	}

	@ParameterizedTest
	@CsvSource(textBlock = """
			3.4.5,3.6.0
			3.4.0-M1,3.5.0-M2
			2020.1.0,2020.2.0
			3.4.5,3.4.5
			""")
	void isSameMajorTrue(String left, String right) {
		assertThat(Version.from(left).isSameMajor(Version.from(right))).isTrue();
	}

	@ParameterizedTest
	@CsvSource(textBlock = """
			3.4.5,2.4.5
			3.4.0-M1,2.4.0-M1
			2020.1.0,2021.1.0
			""")
	void isSameMajorFalse(String left, String right) {
		assertThat(Version.from(left).isSameMajor(Version.from(right))).isFalse();
	}

	@ParameterizedTest
	@CsvSource(textBlock = """
			3.4.5,3.4.9
			3.4.0-M1,3.4.0-M2
			2020.1.0,2020.1.9
			3.4.5,3.4.5
			""")
	void isSameMinorTrue(String left, String right) {
		assertThat(Version.from(left).isSameMinor(Version.from(right))).isTrue();
	}

	@ParameterizedTest
	@CsvSource(textBlock = """
			3.4.5,3.3.9
			3.4.0-M1,3.5.0-M1
			2020.1.0,2020.2.0
			""")
	void isSameMinorFalse(String left, String right) {
		assertThat(Version.from(left).isSameMinor(Version.from(right))).isFalse();
	}

	private void assertVersion(String text, String major, String minor, Qualifier qualifier) {
		assertVersion(text, major, minor, qualifier, null);
	}

	private void assertVersion(String text, String major, String minor, Qualifier qualifier, Parts parts) {
		assertVersion(text, text, major, minor, qualifier, parts);
	}

	private void assertVersion(String text, String id, String major, String minor, Qualifier qualifier, Parts parts) {
		assertThat(VersionParser.safeParse(text)).satisfies(isVersion(id, major, minor, qualifier, parts));
	}

	private Consumer<Version> isVersion(String id, String major, String minor, Qualifier qualifier, Parts parts) {
		return (version) -> {
			assertThat(version.getId()).isEqualTo(id);
			assertThat(version.getMajor()).isEqualTo(major);
			assertThat(version.getMinor()).isEqualTo(minor);
			assertThat(version.getQualifier()).isEqualTo(qualifier);
			assertThat(version.getParts()).isEqualTo(parts);
		};
	}

}
