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

package org.springframework.ws.transport;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceMessageSender.DestinationDescriptor;

/**
 * Base class for {@link WebServiceMessageSender} implementations that share
 * {@link DestinationPolicy} handling for {@link #supports(URI, UriSource)}.
 * <p>
 * A URI filter must be provided to only accept a URI that the implementation handles,
 * irrespective of whether it is acceptable according to an {@link UriSource}.
 * Implementations typically inspect the URI scheme (or other transport-specific traits)
 * in that filter.
 * <p>
 * A URI that passes that filter is accepted when the configured {@link DestinationPolicy}
 * accepts it. Without a policy, {@link UriSource#APPLICATION} URIs are accepted and
 * {@link UriSource#REMOTE} URIs are rejected.
 *
 * @param <D> concrete {@link DestinationDescriptor} type
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public abstract class AbstractWebServiceMessageSender<D extends DestinationDescriptor>
		implements WebServiceMessageSender {

	private final Log logger = LogFactory.getLog(getClass());

	private final Predicate<URI> supportedUriFilter;

	private @Nullable DestinationPolicy<D> destinationPolicy;

	private final DestinationPolicy<D> fallbackDestinationPolicy = new FallbackDestinationPolicy<>();

	/**
	 * Create an instance with the URI filter to apply to accept a given URI.
	 * @param supportedUriFilter the predicate to apply to a URI to test if it is
	 * supported by the instance
	 */
	protected AbstractWebServiceMessageSender(Predicate<URI> supportedUriFilter) {
		Assert.notNull(supportedUriFilter, "'supportedUriFilter' must not be null");
		this.supportedUriFilter = supportedUriFilter;
	}

	/**
	 * Set the {@link DestinationPolicy} that decides which destinations this sender may
	 * open a connection to.
	 * <p>
	 * A policy is required to send to a {@link UriSource#REMOTE} destination, such as a
	 * WS-Addressing {@code ReplyTo} or {@code FaultTo} address: when none is configured,
	 * every remote destination is rejected.
	 * @param destinationPolicy the policy to apply
	 */
	public void setDestinationPolicy(DestinationPolicy<D> destinationPolicy) {
		this.destinationPolicy = destinationPolicy;
	}

	/**
	 * Return the configured {@link DestinationPolicy} or {@code null} if none is set.
	 * @return the custom destination policy or {@code null} if none is set.
	 */
	protected @Nullable DestinationPolicy<D> getDestinationPolicy() {
		return this.destinationPolicy;
	}

	@Override
	public boolean supportsRemoteDestinations() {
		return this.destinationPolicy != null;
	}

	@Override
	public boolean supports(URI uri, UriSource uriSource) {
		if (!this.supportedUriFilter.test(uri)) {
			return false;
		}
		DestinationPolicy<D> destinationPolicyToUse = (this.destinationPolicy != null) ? this.destinationPolicy
				: this.fallbackDestinationPolicy;
		return destinationPolicyToUse.accept(createDescriptor(uri, uriSource));
	}

	/**
	 * Create a {@link DestinationDescriptor} for the given {@code uri} and
	 * {@code uriSource}.
	 * @param uri the URI to check
	 * @param uriSource the source of the URI
	 * @return a suitable descriptor
	 */
	protected abstract D createDescriptor(URI uri, UriSource uriSource);

	private final class FallbackDestinationPolicy<T extends DestinationDescriptor> implements DestinationPolicy<T> {

		private final AtomicBoolean missingDestinationPolicyLogged = new AtomicBoolean();

		@Override
		public boolean accept(T descriptor) {
			if (descriptor.uriSource() == UriSource.REMOTE) {
				logMissingDestinationPolicy(descriptor.uri());
				return false;
			}
			return true;
		}

		private void logMissingDestinationPolicy(URI uri) {
			Log loggerToUse = AbstractWebServiceMessageSender.this.logger;
			if (this.missingDestinationPolicyLogged.compareAndSet(false, true)) {
				loggerToUse.warn("Rejected remote destination [" + uri + "]: no DestinationPolicy is configured on "
						+ getClass().getName() + ", so every destination supplied by a remote peer is rejected. "
						+ "Call setDestinationPolicy with an explicit allowlist of the destinations that out-of-band "
						+ "replies may target. Further rejections are logged at debug level.");
			}
			else if (loggerToUse.isDebugEnabled()) {
				loggerToUse.debug("Rejected remote destination [" + uri + "]: no DestinationPolicy is configured on "
						+ getClass().getName());
			}
		}

	}

}
