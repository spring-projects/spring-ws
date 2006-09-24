/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.ws.samples.airline.domain;

import org.springframework.util.Assert;

public class Passenger extends Entity {

    private String firstName;

    private String lastName;

    public Passenger() {
    }

    public Passenger(String firstName, String lastName) {
        Assert.hasLength(firstName);
        Assert.hasLength(lastName);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Passenger passenger = (Passenger) o;

        if (!getFirstName().equals(passenger.getFirstName())) {
            return false;
        }
        if (!getLastName().equals(passenger.getLastName())) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = getFirstName().hashCode();
        result = 29 * result + getLastName().hashCode();
        return result;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
