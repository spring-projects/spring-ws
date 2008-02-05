/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.samples.airline.client.sws;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class GetFrequentFlyerMileage extends WebServiceGatewaySupport {

    public GetFrequentFlyerMileage(WebServiceMessageFactory messageFactory) {
        super(messageFactory);
    }

    public void getFrequentFlyerMileage() {
        Source source = new StringSource(
                "<GetFrequentFlyerMileageRequest xmlns=\"http://www.springframework.org/spring-ws/samples/airline/schemas/messages\" />");

        Result result = new StringResult();

        getWebServiceTemplate().sendSourceAndReceiveToResult(source, result);

        System.out.println("result = " + result);
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "org/springframework/ws/samples/airline/client/sws/applicationContext.xml");

        GetFrequentFlyerMileage getFrequentFlyerMileage = (GetFrequentFlyerMileage) applicationContext
                .getBean("getFrequentFlyerMileage", GetFrequentFlyerMileage.class);
        getFrequentFlyerMileage.getFrequentFlyerMileage();
    }


}
