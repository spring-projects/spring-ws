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

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class WebServiceMessageListenerIntegrationTest extends AbstractDependencyInjectionSpringContextTests {

    private static final String CONTENT =
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" + "<SOAP-ENV:Body>\n" +
                    "<m:GetLastTradePrice xmlns:m='http://www.springframework.org/spring-ws'>\n" +
                    "<symbol>DIS</symbol>\n" + "</m:GetLastTradePrice>\n" + "</SOAP-ENV:Body></SOAP-ENV:Envelope>";

    private JmsTemplate jmsTemplate;

    private Queue responseQueue;

    private Queue requestQueue;

    private Topic requestTopic;

    public WebServiceMessageListenerIntegrationTest() {
        setAutowireMode(AUTOWIRE_BY_NAME);
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setRequestQueue(Queue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public void setRequestTopic(Topic requestTopic) {
        this.requestTopic = requestTopic;
    }

    public void setResponseQueue(Queue responseQueue) {
        this.responseQueue = responseQueue;
    }

    protected String[] getConfigLocations() {
        return new String[]{"classpath:org/springframework/ws/transport/jms/jms-receiver-applicationContext.xml"};
    }

    public void testReceiveQueue() throws Exception {
        final byte[] b = CONTENT.getBytes("UTF-8");
        jmsTemplate.send(requestQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                BytesMessage request = session.createBytesMessage();
                request.setJMSReplyTo(responseQueue);
                request.writeBytes(b);
                return request;
            }
        });
        BytesMessage response = (BytesMessage) jmsTemplate.receive(responseQueue);
        assertNotNull("No response received", response);
    }

    public void testReceiveTopic() throws Exception {
        final byte[] b = CONTENT.getBytes("UTF-8");
        jmsTemplate.send(requestTopic, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                BytesMessage request = session.createBytesMessage();
                request.writeBytes(b);
                return request;
            }
        });
        Thread.sleep(100);
    }

}
