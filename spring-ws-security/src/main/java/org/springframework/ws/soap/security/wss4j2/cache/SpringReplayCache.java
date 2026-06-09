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

import org.apache.wss4j.common.cache.MemoryReplayCache;
import org.apache.wss4j.common.cache.ReplayCache;
import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;
import org.springframework.util.Assert;

/**
 * Apache WSS4J {@link ReplayCache} backed by a Spring {@link Cache}. Each key stores an
 * expiry {@link Instant} with configurable TTLs. The default TTL is 5 minutes and the max
 * TTL is an hour.
 * <p>
 * {@link #contains} consults that instant and evicts the entry when it is past expiry.
 * This does not rely on the backing cache's own TTL configuration.
 * <p>
 * Subclasses may override {@link #onCacheAccess()} and to perform additional eviction,
 * for example, a full sweep over an in-memory cache.
 * <p>
 * For multi hosts deployment in production, make sure to use a {@link Cache}
 * implementation that is distributed, as each host needs to access the same data. For
 * simple, single host deployment, {@link ConcurrentMapReplayCache} can be used.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 * @see ConcurrentMapReplayCache
 */
public class SpringReplayCache implements ReplayCache {

	/**
	 * Default TTL for entries in seconds.
	 */
	public static final long DEFAULT_TTL = MemoryReplayCache.DEFAULT_TTL;

	/**
	 * Maximum TTL for entries in seconds.
	 */
	public static final long MAX_TTL = MemoryReplayCache.MAX_TTL;

	private final Cache cache;

	private final long defaultTtlSeconds;

	private final long maxTtlSeconds;

	/**
	 * Create an instance with the given cache and default TTLs.
	 * @param cache the cache to use
	 */
	public SpringReplayCache(Cache cache) {
		this(cache, DEFAULT_TTL, MAX_TTL);
	}

	/**
	 * Create an instance with the given cache and TTLs.
	 * @param cache the cache to use
	 * @param defaultTtlSeconds the default TTL in seconds
	 * @param maxTtlSeconds the maximum TTL in seconds
	 */
	public SpringReplayCache(Cache cache, long defaultTtlSeconds, long maxTtlSeconds) {
		Assert.notNull(cache, "cache must not be null");
		Assert.isTrue(defaultTtlSeconds > 0, "defaultTtlSeconds must be positive");
		Assert.isTrue(maxTtlSeconds >= defaultTtlSeconds, "maxTtlSeconds must be >= defaultTtlSeconds");
		this.cache = cache;
		this.defaultTtlSeconds = defaultTtlSeconds;
		this.maxTtlSeconds = maxTtlSeconds;
	}

	protected final Cache getCache() {
		return this.cache;
	}

	@Override
	public void add(String identifier) {
		add(identifier, Instant.now().plusSeconds(this.defaultTtlSeconds));
	}

	@Override
	public void add(String identifier, @Nullable Instant expiry) {
		if (identifier == null || identifier.isEmpty()) {
			return;
		}
		Instant now = Instant.now();
		Instant maxExpiry = now.plusSeconds(this.maxTtlSeconds);
		if (expiry == null || expiry.isBefore(now) || expiry.isAfter(maxExpiry)) {
			expiry = now.plusSeconds(this.defaultTtlSeconds);
		}
		this.cache.put(identifier, expiry);
		onCacheAccess();
	}

	@Override
	public boolean contains(String identifier) {
		if (identifier == null || identifier.isEmpty()) {
			return false;
		}
		onCacheAccess();
		Cache.ValueWrapper wrapper = this.cache.get(identifier);
		if (wrapper == null) {
			return false;
		}
		Object value = wrapper.get();
		if (!(value instanceof Instant exp)) {
			return false;
		}
		if (exp.isBefore(Instant.now())) {
			this.cache.evict(identifier);
			return false;
		}
		return true;
	}

	/**
	 * Invoked when the cache is accessed, either after an entry has been stored or before
	 * checking for the presence of one.
	 */
	protected void onCacheAccess() {
	}

	@Override
	public void close() {
		this.cache.clear();
	}

}
