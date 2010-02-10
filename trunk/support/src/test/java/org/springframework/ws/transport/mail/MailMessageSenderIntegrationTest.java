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

package org.springframework.ws.transport.mail;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.WebServiceConnection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

public class MailMessageSenderIntegrationTest {

    private MailMessageSender messageSender;

    private MessageFactory messageFactory;

    private static final String SOAP_ACTION = "http://springframework.org/DoIt";

    @Before
    public void setUp() throws Exception {
        messageSender = new MailMessageSender();
        messageSender.setFrom("Spring-WS SOAP Client <client@example.com>");
        messageSender.setTransportUri("smtp://smtp.example.com");
        messageSender.setStoreUri("imap://imap.example.com/INBOX");
        messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        messageSender.afterPropertiesSet();
    }

    @After
    public void tearDown() throws Exception {
        Mailbox.clearAll();
    }

    @Test
    public void testSendAndReceiveQueueNoResponse() throws Exception {
        WebServiceConnection connection = null;
        try {
            URI mailTo = new URI("mailto:server@example.com?subject=SOAP%20Test");
            connection = messageSender.createConnection(mailTo);
            SOAPMessage saajMessage = messageFactory.createMessage();
            saajMessage.getSOAPBody().addBodyElement(new QName("http://springframework.org", "test"));
            SoapMessage soapRequest = new SaajSoapMessage(saajMessage);
            soapRequest.setSoapAction(SOAP_ACTION);
            connection.send(soapRequest);
            Assert.assertEquals("No mail message sent", 1, Mailbox.get("server@example.com").size());
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

}