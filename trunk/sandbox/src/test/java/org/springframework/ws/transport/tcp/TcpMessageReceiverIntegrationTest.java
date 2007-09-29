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

package org.springframework.ws.transport.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import javax.xml.transform.stream.StreamResult;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.xml.transform.StringSource;

public class TcpMessageReceiverIntegrationTest extends AbstractDependencyInjectionSpringContextTests {

    private WebServiceMessageFactory messageFactory;

    private WebServiceMessageSender messageSender;

    public void setMessageFactory(WebServiceMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    public void setMessageSender(WebServiceMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public static final String REQUEST =
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'\n" +
                    "                   SOAP-ENV:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/'>\n" +
                    "    <SOAP-ENV:Body>\n" +
                    "        <m:GetLastTradePrice xmlns:m='http://www.springframework.org/spring-ws'>\n" +
                    "            <symbol>DIS</symbol>\n" + "        </m:GetLastTradePrice>\n" +
                    "    </SOAP-ENV:Body>\n" + "</SOAP-ENV:Envelope>";

    public void testServer() throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", TcpMessageReceiver.DEFAULT_PORT);
        Writer writer;
        BufferedReader reader;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            writer.write(REQUEST);
            writer.flush();
            socket.shutdownOutput();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        finally {
            socket.close();
        }
    }

    public void testTemplate() throws Exception {
        WebServiceTemplate template = new WebServiceTemplate(messageFactory);
        template.setMessageSender(messageSender);
        template.sendSourceAndReceiveToResult("tcp://localhost", new StringSource(REQUEST),
                new StreamResult(System.out));
    }

    protected String[] getConfigLocations() {
        return new String[]{"classpath:/org/springframework/ws/transport/tcp/applicationContext.xml"};
    }

}