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

import java.io.IOException;
import java.net.URI;
import java.util.function.Predicate;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;

/**
 * Defines the methods for classes capable of sending and receiving
 * {@link WebServiceMessage} instances for a given transport. Also validates if a
 * destination {@link URI} is supported, which is used both for expressing that the
 * instance can handle the URI but that it is acceptable for a given {@link UriSource}.
 * <p>
 * The {@code WebServiceMessageSender} is basically a factory for
 * {@link WebServiceConnection} objects.
 *
 * @author Arjen Poutsma
 * @author Stephane Nicoll
 * @since 1.0.0
 * @see WebServiceConnection
 */
public interface WebServiceMessageSender {

	/**
	 * Create a new {@link WebServiceConnection} to the specified URI.
	 * @param uri the URI to open a connection to
	 * @return the new connection
	 * @throws IOException in case of I/O errors
	 */
	WebServiceConnection createConnection(URI uri) throws IOException;

	/**
	 * Whether this sender supports the given URI for the supplied {@link UriSource}.
	 * <p>
	 * A URI is supported when this sender handles its transport <b>and</b> the configured
	 * {@link DestinationPolicy} accepts it. When no policy is configured,
	 * {@link UriSource#APPLICATION} destinations are accepted and
	 * {@link UriSource#REMOTE} destinations are rejected: only the application knows
	 * which peer-supplied destinations are legitimate, so there is no safe default.
	 * @param uri the URI to be checked
	 * @param uriSource whether the URI is application-controlled or remote-influenced
	 * @return {@code true} if this sender accepts the URI for that source
	 * @since 3.1.9
	 */
	boolean supports(URI uri, UriSource uriSource);

	/**
	 * Whether this sender supports the given URI for application-controlled use.
	 * @param uri the URI to be checked
	 * @return {@code true} if this {@code WebServiceMessageSender} supports the supplied
	 * URI
	 * @deprecated as of 3.1.9 in favor of {@link #supports(URI, UriSource)}
	 */
	@Deprecated
	default boolean supports(URI uri) {
		return supports(uri, UriSource.APPLICATION);
	}

	/**
	 * Whether this sender is configured to accept destinations supplied by a remote peer
	 * at all.
	 * <p>
	 * Return {@code false} when the sender rejects every {@link UriSource#REMOTE}
	 * destination out of hand, typically because no {@link DestinationPolicy} is
	 * configured.
	 * @return {@code false} if no remote destination can ever be accepted
	 * @since 3.1.10
	 */
	default boolean supportsRemoteDestinations() {
		return true;
	}

	/**
	 * Source of a destination URI.
	 *
	 * @since 3.1.9
	 */
	enum UriSource {

		/**
		 * Destination chosen by the application (for example, a
		 * {@code WebServiceTemplate} endpoint URL).
		 */
		APPLICATION,

		/**
		 * Destination influenced by a remote peer (for example, WS-Addressing
		 * {@code ReplyTo} / {@code FaultTo} on a server).
		 */
		REMOTE

	}

	/**
	 * Description of a destination URI for validation, including its
	 * {@linkplain UriSource source}.
	 *
	 * @since 3.1.9
	 */
	interface DestinationDescriptor {

		/**
		 * Return the URI.
		 * @return the URI
		 */
		URI uri();

		/**
		 * Return the origin of the URI.
		 * @return the source of the URI
		 */
		UriSource uriSource();

	}

	/**
	 * Decides which destinations a sender may open a connection to.
	 * <p>
	 * A policy is <b>required</b> to send to a {@link UriSource#REMOTE} destination: when
	 * a sender has no policy, every remote destination is rejected. Express the
	 * destinations that the application considers legitimate, ideally as an explicit
	 * allowlist. Transport-specific factories are available to cover the common case and
	 * leave {@link UriSource#APPLICATION} destinations untouched.
	 * <p>
	 * A policy is consulted for every destination, whatever its source, so a policy that
	 * only means to constrain remote destinations should either check
	 * {@link DestinationDescriptor#uriSource()} or be built with
	 * {@link #forRemoteDestinations(Predicate)}.
	 *
	 * @param <D> concrete descriptor type supplied by the sender implementation
	 * @since 3.1.9
	 */
	@FunctionalInterface
	interface DestinationPolicy<D extends DestinationDescriptor> {

		/**
		 * Whether the given destination is acceptable.
		 * @param descriptor transport-specific view of the destination
		 * @return whether the destination is acceptable
		 * @since 3.1.10
		 */
		boolean accept(D descriptor);

		/**
		 * Return a policy that applies the given predicate to {@link UriSource#REMOTE}
		 * destinations and accepts every other destination.
		 * @param <D> concrete descriptor type supplied by the sender implementation
		 * @param remotePolicy the predicate to apply to remote destinations
		 * @return a policy that only constrains remote destinations
		 * @since 3.1.10
		 */
		static <D extends DestinationDescriptor> DestinationPolicy<D> forRemoteDestinations(Predicate<D> remotePolicy) {
			Assert.notNull(remotePolicy, "'remotePolicy' must not be null");
			return (descriptor) -> descriptor.uriSource() != UriSource.REMOTE || remotePolicy.test(descriptor);
		}

	}

}
