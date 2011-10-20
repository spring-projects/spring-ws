/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.support;

import javax.xml.transform.Source;

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
     *
     * @param message the message
     */
    public static void fail(String message) {
        throw new AssertionError(message);
    }

    /**
     * Fails a test with the given message and source.
     *
     * @param message the message
     * @param source  the source
     */
    public static void fail(String message, String sourceLabel, Source source) {
        if (source != null) {
            throw new SourceAssertionError(message, sourceLabel, source);
        }
        else {
            fail(message);
        }
    }

    /**
     * Asserts that a condition is {@code true}. If not, throws an {@link AssertionError} with the given message.
     *
     * @param message   the message
     * @param condition the condition to test for
     */
    public static void assertTrue(String message, boolean condition) {
        assertTrue(message, condition, null, null);
    }

    /**
     * Asserts that a condition is {@code true}. If not, throws an {@link AssertionError} with the given message and
     * source.
     *
     * @param message   the message
     * @param condition the condition to test for
     */
    public static void assertTrue(String message, boolean condition, String sourceLabel, Source source) {
        if (!condition) {
            fail(message, sourceLabel, source);
        }
    }

    /**
     * Asserts that two objects are equal. If not, an {@link AssertionError} is thrown with the given message.
     *
     * @param message  the message
     * @param expected the expected value
     * @param actual   the actual value
     */
    public static void assertEquals(String message, Object expected, Object actual) {
        assertEquals(message, expected, actual, null, null);
    }

    /**
     * Asserts that two objects are equal. If not, an {@link AssertionError} is thrown with the given message.
     *
     * @param message  the message
     * @param expected the expected value
     * @param actual   the actual value
     * @param source   the source
     */
    public static void assertEquals(String message, Object expected, Object actual, String sourceLabel, Source source) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        fail(message + " expected:<" + expected + "> but was:<" + actual + ">", sourceLabel, source);
    }

}
