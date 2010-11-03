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

package org.springframework.ws.test.client;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class MockWebServiceServerTest {

    @Test
    public void createServerWebServiceTemplate() throws Exception {
        WebServiceTemplate template = new WebServiceTemplate();

        MockWebServiceServer server = MockWebServiceServer.createServer(template);
        assertNotNull(server);
    }
    
    @Test
    public void createServerGatewaySupport() throws Exception {
        MyClient client = new MyClient();

        MockWebServiceServer server = MockWebServiceServer.createServer(client);
        assertNotNull(server);
    }

    @Test
    public void createServerApplicationContextWebServiceTemplate() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("webServiceTemplate", WebServiceTemplate.class);
        applicationContext.refresh();

        MockWebServiceServer server = MockWebServiceServer.createServer(applicationContext);
        assertNotNull(server);
    }

    @Test
    public void createServerApplicationContextWebServiceGatewaySupport() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("myClient", MyClient.class);
        applicationContext.refresh();

        MockWebServiceServer server = MockWebServiceServer.createServer(applicationContext);
        assertNotNull(server);
    }

    @Test(expected = BeanInitializationException.class)
    public void createServerApplicationContextEmpty() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        MockWebServiceServer server = MockWebServiceServer.createServer(applicationContext);
        assertNotNull(server);
    }

    public static class MyClient extends WebServiceGatewaySupport {

    }
}
