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

package org.springframework.ws.soap.security.wss4j2.cache;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import org.springframework.cache.concurrent.ConcurrentMapCache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link SpringReplayCache}.
 *
 * @author Stephane Nicoll
 */
class SpringReplayCacheTests {

	@Test
	void createWithNullCacheFails() {
		assertThatIllegalArgumentException().isThrownBy(() -> new SpringReplayCache(null))
			.withMessage("cache must not be null");
	}

	@Test
	void createWithZeroTtlFails() {
		ConcurrentMapCache springCache = new ConcurrentMapCache("replay");
		assertThatIllegalArgumentException().isThrownBy(() -> new SpringReplayCache(springCache, 0, 10))
			.withMessage("defaultTtlSeconds must be positive");
	}

	@Test
	void createWithMaxTtlLowerThanTtlFails() {
		ConcurrentMapCache springCache = new ConcurrentMapCache("replay");
		assertThatIllegalArgumentException().isThrownBy(() -> new SpringReplayCache(springCache, 10, 5))
			.withMessage("maxTtlSeconds must be >= defaultTtlSeconds");
	}

	@Test
	void containsFalseUntilAdded() {
		ConcurrentMapCache springCache = new ConcurrentMapCache("replay");
		SpringReplayCache replayCache = new SpringReplayCache(springCache);
		assertThat(replayCache.contains("nonce-1")).isFalse();
		replayCache.add("nonce-1");
		assertThat(replayCache.contains("nonce-1")).isTrue();
	}

	@Test
	void containsFalseIfNotInstant() {
		ConcurrentMapCache springCache = new ConcurrentMapCache("replay");
		springCache.put("nonce-1", "not-an-instant");
		SpringReplayCache replayCache = new SpringReplayCache(springCache);
		assertThat(replayCache.contains("nonce-1")).isFalse();
		replayCache.add("nonce-1");
		assertThat(replayCache.contains("nonce-1")).isTrue();
	}

	@Test
	void containsFalseIfIdHasNoText() {
		SpringReplayCache replayCache = new SpringReplayCache(new ConcurrentMapCache("replay"));
		assertThat(replayCache.contains(null)).isFalse();
		assertThat(replayCache.contains("   ")).isFalse();
	}

	@Test
	void addWithExpiryStillRecordsPresence() {
		ConcurrentMapCache springCache = new ConcurrentMapCache("replay");
		SpringReplayCache replayCache = new SpringReplayCache(springCache);
		replayCache.add("nonce-2", Instant.now().plus(1, ChronoUnit.HOURS));
		assertThat(replayCache.contains("nonce-2")).isTrue();
	}

	@Test
	void expiredEntryIsNotContainedAndEvicted() {
		ConcurrentMapCache springCache = new ConcurrentMapCache("replay");
		springCache.put("stale", Instant.now().minusSeconds(10));
		SpringReplayCache replayCache = new SpringReplayCache(springCache);
		assertThat(replayCache.contains("stale")).isFalse();
		assertThat(replayCache.contains("stale")).isFalse();
	}

	@Test
	void closeClearsBackingCache() {
		ConcurrentMapCache springCache = new ConcurrentMapCache("replay");
		SpringReplayCache replayCache = new SpringReplayCache(springCache);
		replayCache.add("nonce-4");
		assertThat(replayCache.contains("nonce-4")).isTrue();
		replayCache.close();
		assertThat(replayCache.contains("nonce-4")).isFalse();
	}

}
