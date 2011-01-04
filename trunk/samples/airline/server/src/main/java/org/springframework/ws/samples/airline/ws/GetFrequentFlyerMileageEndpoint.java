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

package org.springframework.ws.samples.airline.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.samples.airline.service.AirlineService;
import org.springframework.ws.server.endpoint.AbstractDomPayloadEndpoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Endpoint that returns the amount of frequent flyer miles for the currently logged in user. Secured via a WS-Security
 * UsernameToken
 *
 * @author Arjen Poutsma
 */
public class GetFrequentFlyerMileageEndpoint extends AbstractDomPayloadEndpoint implements AirlineWebServiceConstants {

    private final AirlineService airlineService;

    @Autowired
    public GetFrequentFlyerMileageEndpoint(AirlineService airlineService) {
        this.airlineService = airlineService;
    }

    protected Element invokeInternal(Element ignored, Document responseDocument) throws Exception {
        int mileage = airlineService.getFrequentFlyerMileage();
        Element response = responseDocument.createElementNS(MESSAGES_NAMESPACE, GET_FREQUENT_FLYER_MILEAGE_RESPONSE);
        response.setTextContent(Integer.toString(mileage));
        return response;
    }
}