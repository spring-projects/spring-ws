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

package org.springframework.ws.samples.airline.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.service.AirlineService;

/** @author Arjen Poutsma */
public class FlightsController extends MultiActionController {

    private AirlineService airlineService;

    public FlightsController(AirlineService airlineService) {
        Assert.notNull(airlineService, "'airlineService' must not be null");
        this.airlineService = airlineService;
    }

    public ModelAndView flightList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String fromAirportCode = ServletRequestUtils.getStringParameter(request, "from");
        String toAirportCode = ServletRequestUtils.getStringParameter(request, "to");
        String departureDateString =
                ServletRequestUtils.getStringParameter(request, "departureDate", new LocalDate().toString());
        String serviceClassString = ServletRequestUtils.getStringParameter(request, "serviceClass", "ECONOMY");

        ServiceClass serviceClass = ServiceClass.valueOf(serviceClassString);
        LocalDate departureDate = new LocalDate(departureDateString);

        ModelAndView mav = new ModelAndView("flights");
        if (StringUtils.hasLength(fromAirportCode) && StringUtils.hasLength(toAirportCode)) {
            mav.addObject("from", fromAirportCode);
            mav.addObject("to", toAirportCode);
            mav.addObject("departureDate", departureDateString);
            mav.addObject("serviceClass", serviceClassString);
            mav.addObject("flights",
                    airlineService.getFlights(fromAirportCode, toAirportCode, departureDate, serviceClass));
        }
        return mav;
    }

    public ModelAndView singleFlight(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String uri = request.getRequestURI();
        int pos = uri.lastIndexOf('/') + 1;
        long id = Long.parseLong(uri.substring(pos));
        Flight flight = airlineService.getFlight(id);
        return new ModelAndView("flight", "flight", flight);
    }

}
