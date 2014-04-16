/*
 * Copyright 2005-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.client.support.destination;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for {@link DestinationProvider} implementations that cache destination URI.
 *
 * <p>Caching can be disabled by setting the {@link #setCache(boolean) cache} property to {@code false}; forcing a
 * destination lookup for every call.
 *
 * @author Arjen Poutsma
 * @since 1.5.4
 */
public abstract class AbstractCachingDestinationProvider implements DestinationProvider {

    /** Logger available to subclasses. */
    protected final Log logger = LogFactory.getLog(getClass());

    private URI cachedUri;

    private boolean cache = true;

    /**
     * Set whether to cache resolved destinations. Default is {@code true}. This flag can be turned off to
     * re-lookup a destination for each operation, which allows for hot restarting of destinations. This is mainly
     * useful during development.
     */
    public void setCache(boolean cache) {
        this.cache = cache;
    }

    @Override
    public final URI getDestination() {
        if (cache) {
            if (cachedUri == null) {
                cachedUri = lookupDestination();
            }
            return cachedUri;
        }
        else {
            return lookupDestination();
        }
    }

    /**
     * Abstract template method that looks up the URI.
     *
     * <p>If {@linkplain #setCache(boolean) caching}  is enabled, this method will only be called once.
     *
     * @return the destination URI
     */
    protected abstract URI lookupDestination();
}
