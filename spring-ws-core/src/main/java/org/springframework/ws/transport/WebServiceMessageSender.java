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
	 * Implementations typically apply the same transport rules for
	 * {@link UriSource#APPLICATION} as for legacy {@link #supports(URI)}, and add
	 * stricter checks for {@link UriSource#REMOTE}.
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
	 * {@linkplain UriSource source}. Transport-specific descriptors provide additional
	 * details to help express custom checks.
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
	 * Specifies the policy to apply for a given {@linkplain DestinationDescriptor
	 * descriptor}. Implementation may choose to apply the default checks that would
	 * otherwise be applied if such policy was not configured.
	 *
	 * @param <D> concrete descriptor type supplied by the sender implementation
	 * @since 3.1.9
	 */
	@FunctionalInterface
	interface DestinationPolicy<D extends DestinationDescriptor> {

		/**
		 * Whether the given destination is acceptable.
		 * @param descriptor transport-specific view of the destination
		 * @param defaultChecks predicate that runs the sender's default checks for this
		 * descriptor. Call {@code defaultChecks.test(descriptor)} when those checks
		 * should apply
		 * @return whether the destination is acceptable
		 */
		boolean accept(D descriptor, Predicate<D> defaultChecks);

	}

}
