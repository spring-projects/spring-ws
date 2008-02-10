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

package com.mycompany.hr.ws;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.mycompany.hr.service.HumanResourceService;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.springframework.ws.server.endpoint.AbstractJDomPayloadEndpoint;

/**
 * This endpoint handles holiday requests. It uses a combination of JDOM and XPath to extract interesting pieces of XML
 * from the incoming message, and invoked the injected {@link HumanResourceService} with those.
 *
 * @author Arjen Poutsma
 */
public class HolidayEndpoint extends AbstractJDomPayloadEndpoint {

    private XPath startDateExpression;

    private XPath endDateExpression;

    private XPath nameExpression;

    private HumanResourceService humanResourceService;

    public HolidayEndpoint(HumanResourceService humanResourceService) throws JDOMException {
        this.humanResourceService = humanResourceService;
        Namespace namespace = Namespace.getNamespace("hr", "http://mycompany.com/hr/schemas");
        startDateExpression = XPath.newInstance("//hr:StartDate");
        startDateExpression.addNamespace(namespace);
        endDateExpression = XPath.newInstance("//hr:EndDate");
        endDateExpression.addNamespace(namespace);
        nameExpression = XPath.newInstance("concat(//hr:FirstName,' ',//hr:LastName)");
        nameExpression.addNamespace(namespace);
    }

    protected Element invokeInternal(Element holidayRequest) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = dateFormat.parse(startDateExpression.valueOf(holidayRequest));
        Date endDate = dateFormat.parse(endDateExpression.valueOf(holidayRequest));
        String name = nameExpression.valueOf(holidayRequest);

        humanResourceService.bookHoliday(startDate, endDate, name);
        return null;
    }

}
