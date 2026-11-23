/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import java.net.URI;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.ws.transport.AbstractWebServiceMessageSender;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * Abstract base class for {@link WebServiceMessageSender} implementations that use HTTP.
 * <p>
 * Any {@code http:} or {@code https:} URI supplied by the application is accepted. A URI
 * supplied by a {@linkplain UriSource#REMOTE remote} peer, such as a WS-Addressing
 * {@code ReplyTo} or {@code FaultTo} address, is only accepted when a
 * {@link DestinationPolicy DestinationPolicy} accepts it; without a policy, every remote
 * destination is rejected. {@link HttpDestinationPolicies#allowedHosts(String...)} can be
 * used to state the hosts that remote destinations may target. For more advanced or
 * constrained scenarios, implement {@link DestinationPolicy DestinationPolicy} directly.
 *
 * @author Arjen Poutsma
 * @author Stephane Nicoll
 * @since 1.0.0
 */
public abstract class AbstractHttpWebServiceMessageSender
		extends AbstractWebServiceMessageSender<HttpDestinationDescriptor> {

	/**
	 * Logger available to subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	private boolean acceptGzipEncoding = true;

	/**
	 * Create an instance with the URI filter to apply to accept a given URI.
	 * @param supportedUriFilter the predicate to apply to a URI to test if it is
	 * supported by the instance
	 * @since 3.1.9
	 */
	protected AbstractHttpWebServiceMessageSender(Predicate<URI> supportedUriFilter) {
		super(supportedUriFilter);
	}

	/**
	 * Create an instance that accepts {@code http:} and {@code https:} URIs.
	 */
	protected AbstractHttpWebServiceMessageSender() {
		this((uri) -> uri.getScheme().equals(HttpTransportConstants.HTTP_URI_SCHEME)
				|| uri.getScheme().equals(HttpTransportConstants.HTTPS_URI_SCHEME));
	}

	/**
	 * Return whether to accept GZIP encoding, that is, whether to send the HTTP
	 * {@code Accept-Encoding} header with {@code gzip} as value.
	 */
	public boolean isAcceptGzipEncoding() {
		return this.acceptGzipEncoding;
	}

	/**
	 * Set whether to accept GZIP encoding, that is, whether to send the HTTP
	 * {@code Accept-Encoding} header with {@code gzip} as value.
	 * <p>
	 * Default is {@code true}. Turn this flag off if you do not want GZIP response
	 * compression even if enabled on the HTTP server.
	 */
	public void setAcceptGzipEncoding(boolean acceptGzipEncoding) {
		this.acceptGzipEncoding = acceptGzipEncoding;
	}

	@Override
	protected HttpDestinationDescriptor createDescriptor(URI uri, UriSource uriSource) {
		return HttpDestinationDescriptor.of(uri, uriSource);
	}

}
