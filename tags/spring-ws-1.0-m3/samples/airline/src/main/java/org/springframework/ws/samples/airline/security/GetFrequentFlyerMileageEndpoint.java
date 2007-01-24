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

package org.springframework.ws.samples.airline.security;

import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ws.samples.airline.service.AirlineService;
import org.springframework.ws.server.endpoint.AbstractJDomPayloadEndpoint;

/**
 * Endpoint that returns the amount of frequent flyer miles for the currently logged in user. Secured via a WS-Security
 * UsernameToken
 *
 * @author Arjen Poutsma
 */
public class GetFrequentFlyerMileageEndpoint extends AbstractJDomPayloadEndpoint implements InitializingBean {

    private AirlineService airlineService;

    private Namespace namespace;

    public void setAirlineService(AirlineService airlineService) {
        this.airlineService = airlineService;
    }

    protected Element invokeInternal(Element ignored) throws Exception {
        int result = airlineService.getFrequentFlyerMileage();
        Element response = new Element("GetFrequentFlyerMileageResponse", namespace);
        response.setText(Integer.toString(result));
        return response;
    }

    public void afterPropertiesSet() throws Exception {
        namespace = Namespace.getNamespace("tns", "http://www.springframework.org/spring-ws/samples/airline/schemas");

    }
}
