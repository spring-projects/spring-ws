/*
 * Copyright 2006 the original author or authors.
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

import java.util.HashSet;
import java.util.Set;

import org.joda.time.YearMonthDay;

public class Ticket extends Entity {

    private YearMonthDay issueDate;

    private Set passengers = new HashSet();

    private Flight flight;

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public YearMonthDay getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(YearMonthDay issueDate) {
        this.issueDate = issueDate;
    }

    public Set getPassengers() {
        return passengers;
    }

    public void setPassengers(Set passengers) {
        this.passengers = passengers;
    }

    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
    }
}
