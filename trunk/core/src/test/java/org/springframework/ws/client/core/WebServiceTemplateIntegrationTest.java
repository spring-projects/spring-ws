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

package org.springframework.ws.client.core;

import java.net.URL;
import javax.xml.soap.MessageFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class WebServiceTemplateIntegrationTest extends XMLTestCase {

    private WebServiceTemplate template;

    private Server jettyServer;

    protected void setUp() throws Exception {
        jettyServer = new Server(8888);
        Context jettyContext = new Context(jettyServer, "/");
        jettyContext.addServlet(SimpleSaajServlet.class, "/");
        jettyServer.start();
        template = new WebServiceTemplate();
        HttpUrlConnectionMessageSender messageSender = new HttpUrlConnectionMessageSender();
        messageSender.setUrl(new URL("http://localhost:8888/"));
        template.setMessageSender(messageSender);
    }

    protected void tearDown() throws Exception {
        jettyServer.stop();
    }

    public void testSendAndReceiveSaaj() throws Exception {
        template.setMessageFactory(new SaajSoapMessageFactory(MessageFactory.newInstance()));
        String content = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";
        StringResult result = new StringResult();
        template.sendAndReceive(new StringSource(content), result);
        assertXMLEqual(content, result.toString());
    }

    public void testSendAndReceiveAxiom() throws Exception {
        template.setMessageFactory(new AxiomSoapMessageFactory());
        String content = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";
        StringResult result = new StringResult();
        template.sendAndReceive(new StringSource(content), result);
        assertXMLEqual(content, result.toString());
    }

}
