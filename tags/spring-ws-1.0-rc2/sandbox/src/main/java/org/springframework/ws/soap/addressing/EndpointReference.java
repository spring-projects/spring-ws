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

package org.springframework.ws.soap.addressing;

import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

/** @author Arjen Poutsma */
public class EndpointReference {

    private final String address;

    private final List referenceProperties;

    private final List referenceParameters;

    public EndpointReference(String address) {
        this.address = address;
        this.referenceParameters = Collections.EMPTY_LIST;
        this.referenceProperties = Collections.EMPTY_LIST;
    }

    public EndpointReference(String address, List referenceProperties, List referenceParameters) {
        Assert.notNull(address, "address must not be null");
        Assert.notNull(referenceProperties, "referenceProperties must not be null");
        Assert.notNull(referenceParameters, "referenceParameters must not be null");
        this.address = address;
        this.referenceProperties = referenceProperties;
        this.referenceParameters = referenceParameters;
    }

    public String getAddress() {
        return address;
    }

    public List getReferenceProperties() {
        return referenceProperties;
    }

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
        return "EndpointReference[" + address + ']';
    }
}
