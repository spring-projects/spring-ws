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

package org.springframework.ws.transport.jms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.ws.transport.WebServiceConnection;

public class JmsMessageSenderIntegrationTest extends AbstractDependencyInjectionSpringContextTests {

    private static final String REQUEST_HEADER_NAME = "RequestHeader";

    private static final String REQUEST_HEADER_VALUE = "RequestHeaderValue";

    private static final String RESPONSE_HEADER_NAME = "ResponseHeader";

    private static final String RESPONSE_HEADER_VALUE = "ResponseHeaderValue";

    private static final String REQUEST = "Request";

    private static final String RESPONSE = "Response";

    private JmsMessageSender messageSender;

    private JmsTemplate jmsTemplate;

    public void setMessageSender(JmsMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    protected String[] getConfigLocations() {
        return new String[]{"classpath:org/springframework/ws/transport/jms/jms-sender-applicationContext.xml"};
    }

    public void testSendAndReceiveNoResponse() throws Exception {
        WebServiceConnection wsConnection = null;
        try {
            wsConnection = messageSender.createConnection();
            TransportOutputStream tos = wsConnection.getTransportOutputStream();
            tos.addHeader("Content-Type", "text/xml");
            tos.addHeader(REQUEST_HEADER_NAME, REQUEST_HEADER_VALUE);
            FileCopyUtils.copy(REQUEST.getBytes("UTF-8"), tos);

            BytesMessage request = (BytesMessage) jmsTemplate.receive();
            assertEquals("Invalid header value received on server side", REQUEST_HEADER_VALUE,
                    request.getStringProperty(REQUEST_HEADER_NAME));
            assertEquals("Invalid request received", REQUEST, getMessageContents(request));
            assertNull("Response", wsConnection.getTransportInputStream());
        }
        finally {
            if (wsConnection != null) {
                wsConnection.close();
            }
        }
    }

    public void testSendAndReceiveResponse() throws Exception {
        WebServiceConnection wsConnection = null;
        try {
            wsConnection = messageSender.createConnection();
            TransportOutputStream tos = wsConnection.getTransportOutputStream();
            tos.addHeader("Content-Type", "text/xml");
            tos.addHeader(REQUEST_HEADER_NAME, REQUEST_HEADER_VALUE);
            FileCopyUtils.copy(REQUEST.getBytes("UTF-8"), tos);

            BytesMessage request = (BytesMessage) jmsTemplate.receive();
            assertEquals("Invalid header value received on server side", REQUEST_HEADER_VALUE,
                    request.getStringProperty(REQUEST_HEADER_NAME));
            assertEquals("Invalid request received", REQUEST, getMessageContents(request));
            final byte[] bytes = RESPONSE.getBytes("UTF-8");
            jmsTemplate.send(request.getJMSReplyTo(), new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    BytesMessage response = session.createBytesMessage();
                    response.setStringProperty(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE);
                    response.writeBytes(bytes);
                    return response;
                }
            });
            assertNotNull("No response", wsConnection.getTransportInputStream());
            TransportInputStream tis = wsConnection.getTransportInputStream();
            boolean headerFound = false;
            for (Iterator iterator = tis.getHeaderNames(); iterator.hasNext();) {
                String headerName = (String) iterator.next();
                if (RESPONSE_HEADER_NAME.equals(headerName)) {
                    headerFound = true;
                }
            }
            assertTrue("Response has invalid header", headerFound);
            Iterator headerValues = tis.getHeaders(RESPONSE_HEADER_NAME);
            assertTrue("Response has no header values", headerValues.hasNext());
            assertEquals("Response has invalid header values", RESPONSE_HEADER_VALUE, headerValues.next());
            String result = new String(FileCopyUtils.copyToByteArray(tis), "UTF-8");
            assertEquals("Invalid response", RESPONSE, result);
        }
        finally {
            if (wsConnection != null) {
                wsConnection.close();
            }
        }
    }

    private String getMessageContents(BytesMessage message) throws JMSException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = -1;
        while ((bytesRead = message.readBytes(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
        return out.toString("UTF-8");
    }

}