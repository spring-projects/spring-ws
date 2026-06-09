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

/**
 * Tests for {@link ConcurrentMapReplayCache}.
 *
 * @author Stephane Nicoll
 */
class ConcurrentMapReplayCacheTests {

	@Test
	void containsFalseUntilAdded() {
		ConcurrentMapReplayCache replayCache = new ConcurrentMapReplayCache();
		assertThat(replayCache.contains("nonce-1")).isFalse();
		replayCache.add("nonce-1");
		assertThat(replayCache.contains("nonce-1")).isTrue();
	}

	@Test
	void expiredEntriesAreRemovedOnContains() {
		ConcurrentMapReplayCache replayCache = new ConcurrentMapReplayCache();
		ConcurrentMapCache backing = (ConcurrentMapCache) replayCache.getCache();
		backing.put("stale", Instant.now().minusSeconds(30));
		assertThat(replayCache.contains("other")).isFalse();
		assertThat(replayCache.contains("stale")).isFalse();
	}

	@Test
	void pastCustomExpiryIsReplacedWithDefaultTtl() {
		ConcurrentMapReplayCache replayCache = new ConcurrentMapReplayCache(60, 120);
		replayCache.add("nonce-2", Instant.now().minusSeconds(1));
		assertThat(replayCache.contains("nonce-2")).isTrue();
	}

	@Test
	void customExpiryWithinMaxIsStored() {
		ConcurrentMapReplayCache replayCache = new ConcurrentMapReplayCache(30, 600);
		Instant expiry = Instant.now().plus(5, ChronoUnit.MINUTES);
		replayCache.add("nonce-3", expiry);
		assertThat(replayCache.contains("nonce-3")).isTrue();
	}

	@Test
	void closeClearsBackingCache() {
		ConcurrentMapReplayCache replayCache = new ConcurrentMapReplayCache();
		replayCache.add("nonce-4");
		assertThat(replayCache.contains("nonce-4")).isTrue();
		replayCache.close();
		assertThat(replayCache.contains("nonce-4")).isFalse();
	}

}
