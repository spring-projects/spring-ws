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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cache.concurrent.ConcurrentMapCache;

/**
 * {@link SpringReplayCache} backed by an internal {@link ConcurrentMapCache} that sweeps
 * all expired entries on cache access so keys that are never read again are still removed
 * once they expire.
 * <p>
 * Use this implementation only with single host deployment. Services that are deployment
 * on multiple hosts should use a distributed cache, see {@link SpringReplayCache} for
 * options.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public class ConcurrentMapReplayCache extends SpringReplayCache {

	public ConcurrentMapReplayCache() {
		this(DEFAULT_TTL, MAX_TTL);
	}

	public ConcurrentMapReplayCache(long defaultTtlSeconds, long maxTtlSeconds) {
		super(new ConcurrentMapCache("wss4j-replay"), defaultTtlSeconds, maxTtlSeconds);
	}

	@Override
	protected void onCacheAccess() {
		Instant now = Instant.now();
		ConcurrentMapCache mapCache = (ConcurrentMapCache) getCache();
		ConcurrentMap<Object, Object> store = mapCache.getNativeCache();
		List<Object> keysToRemove = new ArrayList<>();
		for (Map.Entry<Object, Object> entry : store.entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Instant instant && instant.isBefore(now)) {
				keysToRemove.add(key);
			}
		}
		for (Object key : keysToRemove) {
			mapCache.evict(key);
		}
	}

}
