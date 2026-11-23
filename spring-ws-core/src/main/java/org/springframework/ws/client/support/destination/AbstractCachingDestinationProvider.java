/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.support.destination;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

/**
 * Abstract base class for {@link DestinationProvider} implementations that cache
 * destination URI.
 * <p>
 * Caching can be disabled by setting the {@link #setCache(boolean) cache} property to
 * {@code false}; forcing a destination lookup for every call.
 *
 * @author Arjen Poutsma
 * @since 1.5.4
 */
public abstract class AbstractCachingDestinationProvider implements DestinationProvider {

	/** Logger available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	private @Nullable URI cachedUri;

	private boolean cache = true;

	/**
	 * Set whether to cache resolved destinations. Default is {@code true}. This flag can
	 * be turned off to re-lookup a destination for each operation, which allows for hot
	 * restarting of destinations. This is mainly useful during development.
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}

	@Override
	public final @Nullable URI getDestination() {
		if (this.cache) {
			if (this.cachedUri == null) {
				this.cachedUri = lookupDestination();
			}
			return this.cachedUri;
		}
		else {
			return lookupDestination();
		}
	}

	/**
	 * Abstract template method that looks up the URI.
	 * <p>
	 * If {@linkplain #setCache(boolean) caching} is enabled, this method will only be
	 * called once.
	 * @return the destination URI
	 */
	protected abstract @Nullable URI lookupDestination();

}
