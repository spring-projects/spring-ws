/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
