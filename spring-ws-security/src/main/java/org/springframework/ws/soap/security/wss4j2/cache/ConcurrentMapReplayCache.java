/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
