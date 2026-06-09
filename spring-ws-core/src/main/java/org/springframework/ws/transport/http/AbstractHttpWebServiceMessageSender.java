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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.ws.transport.AbstractWebServiceMessageSender;

/**
 * Abstract base class for
 * {@link org.springframework.ws.transport.WebServiceMessageSender} implementations that
 * use HTTP.
 * <p>
 * For {@link UriSource#APPLICATION}, default checks accept any {@code http:} or
 * {@code https:} URI. For {@link UriSource#REMOTE}, default checks require a host; reject
 * {@code localhost} and {@code *.localhost}; and when the host is an IP literal, reject
 * loopback, link-local, any-local addresses without DNS lookups, and RFC&nbsp;1918-style
 * private IPv4 literals. Non-literal host names are not resolved in the defaults, so
 * private network access through DNS is not rejected unless you add a
 * {@link DestinationPolicy}, for example, using
 * {@link HttpDestinationDescriptor#hostAddress(boolean)} with DNS for stricter checks or
 * host allowlists. Private IPv4 literals can be enabled by setting
 * {@link #setAllowSiteLocalIpv4(boolean) allowSiteLocalIpv4} to {@code true}.
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

	private boolean allowSiteLocalIpv4 = false;

	private boolean allowDnsResolution = false;

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

	/**
	 * Set whether to allow site-local IPv4 addresses for remote destinations.
	 * <p>
	 * Default is {@code false}. When {@code false}, remote default checks reject private
	 * (RFC 1918) IPv4 literals like {@code 10.0.0.1}. Set to {@code true} to allow
	 * out-of-band replies to internal network destinations unless blocked by another
	 * policy.
	 * @since 3.1.9
	 */
	public void setAllowSiteLocalIpv4(boolean allowSiteLocalIpv4) {
		this.allowSiteLocalIpv4 = allowSiteLocalIpv4;
	}

	/**
	 * Set whether to allow DNS resolution for remote destinations.
	 * <p>
	 * Default is {@code false} to avoid the performance penalty of resolving every remote
	 * destination. However, this means that malicious hostnames resolving to internal IPs
	 * (DNS rebinding or simply internal DNS records) are not blocked by the default
	 * IP-level checks. To secure out-of-band replies without the DNS resolution penalty,
	 * it is strongly recommended to configure a strict allowlist of known hosts using a
	 * {@link DestinationPolicy}.
	 * <p>
	 * Set to {@code true} to resolve host names and apply default checks to the resolved
	 * IPs, mitigating SSRF attacks using DNS at the cost of additional latency.
	 * @since 3.1.9
	 */
	public void setAllowDnsResolution(boolean allowDnsResolution) {
		this.allowDnsResolution = allowDnsResolution;
	}

	@Override
	protected HttpDestinationDescriptor createDescriptor(URI uri, UriSource uriSource) {
		return HttpDestinationDescriptor.of(uri, uriSource);
	}

	@Override
	protected Predicate<HttpDestinationDescriptor> defaultChecks(UriSource uriSource) {
		return (uriSource == UriSource.REMOTE) ? this::remoteDefaultChecks : (details) -> true;
	}

	/**
	 * Apply default checks for {@link UriSource#REMOTE} URIs.
	 * @param descriptor the descriptor
	 * @return whether the default checks accept the URI of the given descriptor
	 */
	protected boolean remoteDefaultChecks(HttpDestinationDescriptor descriptor) {
		String hostName = descriptor.hostName();
		if (hostName == null) {
			return false;
		}
		if ("localhost".equals(hostName) || hostName.endsWith(".localhost")) {
			return false;
		}
		if (!this.allowSiteLocalIpv4 && descriptor.isSiteLocalIpv4Literal()) {
			return false;
		}
		InetAddress hostAddress = descriptor.hostAddress(this.allowDnsResolution);
		if (hostAddress != null) {
			if (isLocalAddress(hostAddress)) {
				return false;
			}
			if (!this.allowSiteLocalIpv4 && hostAddress instanceof Inet4Address && hostAddress.isSiteLocalAddress()) {
				return false;
			}
		}
		return true;
	}

	private boolean isLocalAddress(InetAddress address) {
		return address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isAnyLocalAddress();
	}

}
