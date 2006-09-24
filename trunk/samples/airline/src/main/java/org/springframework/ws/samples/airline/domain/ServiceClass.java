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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ServiceClass implements Serializable {

    private String name;

    public static final ServiceClass ECONOMY = new ServiceClass("economy");

    public static final ServiceClass BUSINESS = new ServiceClass("business");

    public static final ServiceClass FIRST = new ServiceClass("first");

    private static final Map INSTANCES = new HashMap();

    static {
        INSTANCES.put(ECONOMY.toString(), ECONOMY);
        INSTANCES.put(BUSINESS.toString(), BUSINESS);
        INSTANCES.put(FIRST.toString(), FIRST);
    }

    private ServiceClass(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    Object readResolve() {
        return getInstance(name);
    }

    public static ServiceClass getInstance(String name) {
        return (ServiceClass) INSTANCES.get(name);
    }
}
