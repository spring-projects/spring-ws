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

package org.springframework.ws.samples.stockquote.client.sws;

import java.io.IOException;
import java.net.URI;
import javax.xml.transform.Source;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.addressing.client.ActionCallback;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.transform.StringResult;

public class StockClient extends WebServiceGatewaySupport {

    private Resource request;

    private URI action;

    public void setRequest(Resource request) {
        this.request = request;
    }

    public void setAction(URI action) {
        this.action = action;
    }

    public void quotes() throws IOException {
        Source requestSource = new ResourceSource(request);
        StringResult result = new StringResult();
        getWebServiceTemplate().sendSourceAndReceiveToResult(requestSource, new ActionCallback(action), result);
        System.out.println();
        System.out.println(result);
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("applicationContext.xml", StockClient.class);
        StockClient stockClient = (StockClient) applicationContext.getBean("stockClient");
        stockClient.quotes();
    }

}
