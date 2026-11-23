/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.support;

import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;

import org.springframework.lang.Contract;

/**
 * JUnit independent assertion class.
 *
 * @author Lukas Krecan
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class AssertionErrors {

	private AssertionErrors() {
	}

	/**
	 * Fails a test with the given message.
	 * @param message the message
	 */
	@Contract("_ -> fail")
	public static void fail(@Nullable String message) {
		throw new AssertionError(message);
	}

	/**
	 * Fails a test with the given message and source.
	 * @param message the message
	 * @param source the source
	 */
	public static void fail(String message, @Nullable String sourceLabel, @Nullable Source source) {
		if (source != null) {
			throw new SourceAssertionError(message, sourceLabel, source);
		}
		else {
			fail(message);
		}
	}

	/**
	 * Asserts that a condition is {@code true}. If not, throws an {@link AssertionError}
	 * with the given message.
	 * @param message the message
	 * @param condition the condition to test for
	 */
	public static void assertTrue(String message, boolean condition) {
		assertTrue(message, condition, null, null);
	}

	/**
	 * Asserts that a condition is {@code true}. If not, throws an {@link AssertionError}
	 * with the given message and source.
	 * @param message the message
	 * @param condition the condition to test for
	 */
	public static void assertTrue(String message, boolean condition, @Nullable String sourceLabel,
			@Nullable Source source) {
		if (!condition) {
			fail(message, sourceLabel, source);
		}
	}

	/**
	 * Asserts that two objects are equal. If not, an {@link AssertionError} is thrown
	 * with the given message.
	 * @param message the message
	 * @param expected the expected value
	 * @param actual the actual value
	 */
	public static void assertEquals(String message, @Nullable Object expected, @Nullable Object actual) {
		assertEquals(message, expected, actual, null, null);
	}

	/**
	 * Asserts that two objects are equal. If not, an {@link AssertionError} is thrown
	 * with the given message.
	 * @param message the message
	 * @param expected the expected value
	 * @param actual the actual value
	 * @param source the source
	 */
	public static void assertEquals(String message, @Nullable Object expected, @Nullable Object actual,
			@Nullable String sourceLabel, @Nullable Source source) {
		if (expected == null && actual == null) {
			return;
		}
		if (expected != null && expected.equals(actual)) {
			return;
		}
		fail(message + " expected:<" + expected + "> but was:<" + actual + ">", sourceLabel, source);
	}

}
