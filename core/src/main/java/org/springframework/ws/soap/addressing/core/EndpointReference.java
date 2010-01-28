/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.soap.addressing.core;

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
 * @see <a href="http://www.w3.org/TR/ws-addr-core/#eprs">Endpoint References</a>
 * @since 1.5.0
 */
public final class EndpointReference implements Serializable {

    private static final long serialVersionUID = 8999416009328865260L;

    private final URI address;

    private final List referenceProperties;

    private final List referenceParameters;

    /**
     * Creates a new instance of the {@link EndpointReference} class with the given address. The reference parameters
     * and properties are empty.
     *
     * @param address the endpoint address
     */
    public EndpointReference(URI address) {
        Assert.notNull(address, "address must not be null");
        this.address = address;
        this.referenceParameters = Collections.EMPTY_LIST;
        this.referenceProperties = Collections.EMPTY_LIST;
    }

    /**
     * Creates a new instance of the {@link EndpointReference} class with the given address, reference properties, and
     * reference parameters.
     *
     * @param address             the endpoint address
     * @param referenceProperties the reference properties, as a list of {@link Node}
     * @param referenceParameters the reference parameters, as a list of {@link Node}
     */
    public EndpointReference(URI address, List referenceProperties, List referenceParameters) {
        Assert.notNull(address, "address must not be null");
        Assert.notNull(referenceProperties, "referenceProperties must not be null");
        Assert.notNull(referenceParameters, "referenceParameters must not be null");
        this.address = address;
        this.referenceProperties = referenceProperties;
        this.referenceParameters = referenceParameters;
    }

    /** Returns the address of the endpoint. */
    public URI getAddress() {
        return address;
    }

    /** Returns the reference properties of the endpoint, as a list of {@link Node} objects. */
    public List getReferenceProperties() {
        return referenceProperties;
    }

    /** Returns the reference parameters of the endpoint, as a list of {@link Node} objects. */
    public List getReferenceParameters() {
        return referenceParameters;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof EndpointReference) {
            EndpointReference other = (EndpointReference) o;
            return address.equals(other.address);
        }
        return false;
    }

    public int hashCode() {
        return address.hashCode();
    }

    public String toString() {
        return address.toString();
    }
}
