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

package org.springframework.ws.samples.airline.ws;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import org.springframework.ws.samples.airline.service.AirlineService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GetFrequentFlyerMileageEndpointTest extends TestCase {

    private GetFrequentFlyerMileageEndpoint endpoint;

    private AirlineService airlineServiceMock;

    private Document document;

    protected void setUp() throws Exception {
        airlineServiceMock = createMock(AirlineService.class);
        endpoint = new GetFrequentFlyerMileageEndpoint(airlineServiceMock);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
    }

    public void testGetFrequentFlyerMileage() throws Exception {
        expect(airlineServiceMock.getFrequentFlyerMileage()).andReturn(42);

        replay(airlineServiceMock);

        Element response = endpoint.invokeInternal(null, document);
        assertNotNull("Invalid response", response);

        verify(airlineServiceMock);
    }

}