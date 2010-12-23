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

package org.springframework.ws.samples.pox.client.sws;

import java.io.IOException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.xpath.XPathExpression;

public class ContactsClient extends WebServiceGatewaySupport {

    private Resource request;

    private XPathExpression expression;

    public ContactsClient(WebServiceMessageFactory messageFactory) {
        super(messageFactory);
    }

    public void setRequest(Resource request) {
        this.request = request;
    }

    public void setExpression(XPathExpression expression) {
        this.expression = expression;
    }

    public void contacts() throws IOException {
        Source requestSource = new ResourceSource(request);
        DOMResult result = new DOMResult();
        getWebServiceTemplate().sendSourceAndReceiveToResult(requestSource, result);
        int contactCount = (int) expression.evaluateAsNumber(result.getNode());
        System.out.println();
        System.out.println("contactCount = " + contactCount);
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("applicationContext.xml", ContactsClient.class);
        ContactsClient contactsClient = (ContactsClient) applicationContext.getBean("contactsClient");
        contactsClient.contacts();
    }
}
