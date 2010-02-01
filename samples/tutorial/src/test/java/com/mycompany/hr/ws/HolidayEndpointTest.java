/*
 * Copyright 2005-2010 the original author or authors.
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

package com.mycompany.hr.ws;

import java.io.InputStream;
import java.util.Calendar;

import com.mycompany.hr.service.HumanResourceService;
import junit.framework.TestCase;
import org.easymock.MockControl;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class HolidayEndpointTest extends TestCase {

    private Document holidayRequest;

    private HolidayEndpoint endpoint;

    private MockControl mockControl;

    private HumanResourceService serviceMock;

    private Calendar startCalendar;

    private Calendar endCalendar;

    @Override
    protected void setUp() throws Exception {
        mockControl = MockControl.createControl(HumanResourceService.class);
        serviceMock = (HumanResourceService) mockControl.getMock();
        SAXBuilder builder = new SAXBuilder();
        InputStream is = getClass().getResourceAsStream("holidayRequest.xml");
        try {
            holidayRequest = builder.build(is);
        }
        finally {
            is.close();
        }
        endpoint = new HolidayEndpoint(serviceMock);
        startCalendar = Calendar.getInstance();
        startCalendar.clear();
        startCalendar.set(2006, Calendar.JULY, 3);
        endCalendar = Calendar.getInstance();
        endCalendar.clear();
        endCalendar.set(2006, Calendar.JULY, 7);
    }

    public void testInvokeInternal() throws Exception {
        serviceMock.bookHoliday(startCalendar.getTime(), endCalendar.getTime(), "John Doe");
        mockControl.replay();
        Element result = endpoint.invokeInternal(holidayRequest.getRootElement());
        assertNull("No result expected", result);
        mockControl.verify();
    }


}
