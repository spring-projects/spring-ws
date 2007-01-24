/*
 * Copyright 2005, 2006 the original author or authors.
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

import org.joda.time.DateTime;

public class Flight extends Entity {

    private String number;

    private DateTime departureTime;

    private DateTime arrivalTime;

    private Airport to;

    private Airport from;

    private ServiceClass serviceClass;

    private int seatsAvailable;

    private int miles;

    public DateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(DateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public DateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(DateTime departureTime) {
        this.departureTime = departureTime;
    }

    public Airport getFrom() {
        return from;
    }

    public void setFrom(Airport from) {
        this.from = from;
    }

    public int getMiles() {
        return miles;
    }

    public void setMiles(int miles) {
        this.miles = miles;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getSeatsAvailable() {
        return seatsAvailable;
    }

    public void setSeatsAvailable(int seatsAvailable) {
        this.seatsAvailable = seatsAvailable;
    }

    public ServiceClass getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(ServiceClass serviceClass) {
        this.serviceClass = serviceClass;
    }

    public Airport getTo() {
        return to;
    }

    public void setTo(Airport to) {
        this.to = to;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Flight flight = (Flight) o;

        if (!departureTime.equals(flight.departureTime)) {
            return false;
        }
        if (!number.equals(flight.number)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = number.hashCode();
        result = 29 * result + departureTime.hashCode();
        return result;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getNumber());
        buffer.append(' ');
        buffer.append(getDepartureTime());
        return buffer.toString();
    }

    public void substractSeats(int count) {
        this.seatsAvailable -= count;
    }
}
