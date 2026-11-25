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

package org.springframework.ws.test.client;

import org.springframework.util.Assert;

/**
 * A simple type representing the range for the expected number of requests that match an
 * expectation of the {@link MockWebServiceServer}.
 * <p>
 * Examples:
 *
 * <pre class="code">
 * import static org.springframework.ws.test.client.ExpectedCount.*
 *
 * once()
 * twice()
 * manyTimes()
 * times(5)
 * min(2)
 * max(4)
 * never()
 * between(2, 4)
 * </pre>
 * <p>
 * This mirrors the {@code ExpectedCount} of the {@code MockRestServiceServer} of the
 * Spring Framework.
 *
 * @author Sharang Gupta
 * @since 5.1
 * @see MockWebServiceServer#expect(ExpectedCount, RequestMatcher)
 */
public final class ExpectedCount {

	private final int minCount;

	private final int maxCount;

	private ExpectedCount(int minCount, int maxCount) {
		Assert.isTrue(minCount >= 0, "minCount >= 0 is required");
		Assert.isTrue(maxCount >= minCount, "maxCount >= minCount is required");
		this.minCount = minCount;
		this.maxCount = maxCount;
	}

	/**
	 * Return the {@code min} boundary of the expected count range.
	 * @return the minimum number of expected requests
	 */
	public int getMinCount() {
		return this.minCount;
	}

	/**
	 * Return the {@code max} boundary of the expected count range.
	 * @return the maximum number of expected requests
	 */
	public int getMaxCount() {
		return this.maxCount;
	}

	/**
	 * Exactly once.
	 * @return the expected count
	 */
	public static ExpectedCount once() {
		return new ExpectedCount(1, 1);
	}

	/**
	 * Exactly twice.
	 * @return the expected count
	 */
	public static ExpectedCount twice() {
		return new ExpectedCount(2, 2);
	}

	/**
	 * Many times (range of {@code 1..Integer.MAX_VALUE}).
	 * @return the expected count
	 */
	public static ExpectedCount manyTimes() {
		return new ExpectedCount(1, Integer.MAX_VALUE);
	}

	/**
	 * Exactly N times.
	 * @param count the number of expected requests
	 * @return the expected count
	 */
	public static ExpectedCount times(int count) {
		Assert.isTrue(count >= 1, "'count' must be >= 1");
		return new ExpectedCount(count, count);
	}

	/**
	 * At least {@code min} number of times.
	 * @param min the minimum number of expected requests
	 * @return the expected count
	 */
	public static ExpectedCount min(int min) {
		Assert.isTrue(min >= 1, "'min' must be >= 1");
		return new ExpectedCount(min, Integer.MAX_VALUE);
	}

	/**
	 * At most {@code max} number of times, and at least once.
	 * @param max the maximum number of expected requests
	 * @return the expected count
	 */
	public static ExpectedCount max(int max) {
		Assert.isTrue(max >= 1, "'max' must be >= 1");
		return new ExpectedCount(1, max);
	}

	/**
	 * No requests expected at all, i.e. min=0 and max=0.
	 * @return the expected count
	 */
	public static ExpectedCount never() {
		return new ExpectedCount(0, 0);
	}

	/**
	 * Between {@code min} and {@code max} number of times.
	 * @param min the minimum number of expected requests
	 * @param max the maximum number of expected requests
	 * @return the expected count
	 */
	public static ExpectedCount between(int min, int max) {
		return new ExpectedCount(min, max);
	}

}
