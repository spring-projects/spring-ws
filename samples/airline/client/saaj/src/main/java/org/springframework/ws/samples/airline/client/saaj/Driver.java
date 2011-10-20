/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.samples.airline.client.saaj;

/**
 * @author Arjen Poutsma
 */
public class Driver {

    public static void main(String[] args) throws Exception {
        String url = "http://localhost:8080/airline-server/services";
        if (args.length > 0) {
            url = args[0];
        }
        GetFlights getFlights = new GetFlights(url);
        getFlights.getFlights();

        if (!System.getProperty("java.version").startsWith("1.5")) {
            return;
        }
        String username = "john";
        String password = "changeme";
        GetFrequentFlyerMileage getMileage = new GetFrequentFlyerMileage(url);
        getMileage.getMileage(username, password);
    }

}
