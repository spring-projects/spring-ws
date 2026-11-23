/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.addressing.core;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;

import org.springframework.util.Assert;

/**
 * Represents an Endpoint Reference, as defined in the WS-Addressing specification.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 * @see <a href="http://www.w3.org/TR/ws-addr-core/#eprs">Endpoint References</a>
 */
public final class EndpointReference implements Serializable {

	@Serial
	private static final long serialVersionUID = 8999416009328865260L;

	private final URI address;

	private final List<Node> referenceProperties;

	private final List<Node> referenceParameters;

	/**
	 * Creates a new instance of the {@link EndpointReference} class with the given
	 * address. The reference parameters and properties are empty.
	 * @param address the endpoint address
	 */
	public EndpointReference(URI address) {
		Assert.notNull(address, "address must not be null");
		this.address = address;
		this.referenceParameters = Collections.emptyList();
		this.referenceProperties = Collections.emptyList();
	}

	/**
	 * Creates a new instance of the {@link EndpointReference} class with the given
	 * address, reference properties, and reference parameters.
	 * @param address the endpoint address
	 * @param referenceProperties the reference properties, as a list of {@link Node}
	 * @param referenceParameters the reference parameters, as a list of {@link Node}
	 */
	public EndpointReference(URI address, List<Node> referenceProperties, List<Node> referenceParameters) {
		Assert.notNull(address, "address must not be null");
		Assert.notNull(referenceProperties, "referenceProperties must not be null");
		Assert.notNull(referenceParameters, "referenceParameters must not be null");
		this.address = address;
		this.referenceProperties = referenceProperties;
		this.referenceParameters = referenceParameters;
	}

	/** Returns the address of the endpoint. */
	public URI getAddress() {
		return this.address;
	}

	/**
	 * Returns the reference properties of the endpoint, as a list of {@link Node}
	 * objects.
	 */
	public List<Node> getReferenceProperties() {
		return this.referenceProperties;
	}

	/**
	 * Returns the reference parameters of the endpoint, as a list of {@link Node}
	 * objects.
	 */
	public List<Node> getReferenceParameters() {
		return this.referenceParameters;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof EndpointReference other) {
			return this.address.equals(other.address);
		}
		return false;
	}

	public int hashCode() {
		return this.address.hashCode();
	}

	public String toString() {
		return this.address.toString();
	}

}
