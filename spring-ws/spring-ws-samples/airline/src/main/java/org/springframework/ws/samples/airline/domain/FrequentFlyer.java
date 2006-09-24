/*
 * Copyright (c) 2006, Your Corporation. All Rights Reserved.
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

public class FrequentFlyer extends Passenger {

    private String username;

    private String password;

    private int miles;

    public FrequentFlyer() {
    }

    public FrequentFlyer(String username) {
        this.username = username;
    }

    public FrequentFlyer(String username, String password, String firstName, String lastName) {
        super(firstName, lastName);
        this.username = username;
        this.password = password;
    }

    public int getMiles() {
        return miles;
    }

    public void setMiles(int miles) {
        this.miles = miles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FrequentFlyer)) {
            return false;
        }
        final FrequentFlyer that = (FrequentFlyer) other;
        return this.username.equals(that.username);
    }

    public int hashCode() {
        return username.hashCode();
    }

    public void addMiles(int miles) {
        this.miles += miles;
    }
}
