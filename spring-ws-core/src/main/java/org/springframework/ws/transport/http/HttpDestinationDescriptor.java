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

package org.springframework.ws.transport.http;

import java.net.URI;

import org.springframework.ws.transport.WebServiceMessageSender.DestinationDescriptor;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

/**
 * {@link DestinationDescriptor} implementation for HTTP destinations.
 * <p>
 * The {@linkplain #uri() URI} is the destination as it was supplied, which is also what
 * the sender hands to its HTTP client.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public final class HttpDestinationDescriptor implements DestinationDescriptor {

	private final URI uri;

	private final UriSource uriSource;

	private HttpDestinationDescriptor(URI uri, UriSource uriSource) {
		this.uri = uri;
		this.uriSource = uriSource;
	}

	/**
	 * Create an instance from the given URI and URI source.
	 * @param uri the URI
	 * @param uriSource the URI source
	 * @return a descriptor
	 */
	public static HttpDestinationDescriptor of(URI uri, UriSource uriSource) {
		return new HttpDestinationDescriptor(uri, uriSource);
	}

	@Override
	public URI uri() {
		return this.uri;
	}

	@Override
	public UriSource uriSource() {
		return this.uriSource;
	}

}
