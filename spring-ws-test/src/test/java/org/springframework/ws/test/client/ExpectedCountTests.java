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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link ExpectedCount}.
 *
 * @author Sharang Gupta
 */
class ExpectedCountTests {

	@Test
	void once() {
		assertRange(ExpectedCount.once(), 1, 1);
	}

	@Test
	void twice() {
		assertRange(ExpectedCount.twice(), 2, 2);
	}

	@Test
	void times() {
		assertRange(ExpectedCount.times(3), 3, 3);
	}

	@Test
	void timesRequiresAPositiveCount() {
		assertThatIllegalArgumentException().isThrownBy(() -> ExpectedCount.times(0));
	}

	@Test
	void min() {
		assertRange(ExpectedCount.min(2), 2, Integer.MAX_VALUE);
	}

	@Test
	void minRequiresAPositiveCount() {
		assertThatIllegalArgumentException().isThrownBy(() -> ExpectedCount.min(0));
	}

	@Test
	void max() {
		assertRange(ExpectedCount.max(2), 1, 2);
	}

	@Test
	void maxRequiresAPositiveCount() {
		assertThatIllegalArgumentException().isThrownBy(() -> ExpectedCount.max(0));
	}

	@Test
	void manyTimes() {
		assertRange(ExpectedCount.manyTimes(), 1, Integer.MAX_VALUE);
	}

	@Test
	void never() {
		assertRange(ExpectedCount.never(), 0, 0);
	}

	@Test
	void between() {
		assertRange(ExpectedCount.between(0, 3), 0, 3);
	}

	@Test
	void betweenRequiresANonNegativeMinimum() {
		assertThatIllegalArgumentException().isThrownBy(() -> ExpectedCount.between(-1, 3));
	}

	@Test
	void betweenRequiresAMaximumNotLowerThanTheMinimum() {
		assertThatIllegalArgumentException().isThrownBy(() -> ExpectedCount.between(3, 2));
	}

	private static void assertRange(ExpectedCount expectedCount, int minCount, int maxCount) {
		assertThat(expectedCount.getMinCount()).isEqualTo(minCount);
		assertThat(expectedCount.getMaxCount()).isEqualTo(maxCount);
	}

}
