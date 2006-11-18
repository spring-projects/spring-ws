/*
 * Copyright 2006 the original author or authors.
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
import javax.jms.Session;

import org.springframework.jms.listener.SessionAwareMessageListener;

/**
 * Spring-2.0 <code>SessionAwareMessageListener</code> that can be used to handle incoming JMS messages. Requires a
 * <code>WebServiceMessageFactory</code> which is used to convert the incoming JMS <code>TextMessage</code> into a
 * <code>WebServiceMessage</code>, and passes that context to the required <code>MessageEndpoint</code>. If a response
 * is created, it is sent using a response JMS message.
 * <p/>
 * Note that the <code>MessageDispatcher</code> implements the <code>MessageEndpoint</code> interface, enabling this
 * adapter to function as a gateway to further message handling logic.
 *
 * @author Arjen Poutsma
 * @see #setMessageFactory(org.springframework.ws.WebServiceMessageFactory)
 * @see #setMessageEndpoint(org.springframework.ws.endpoint.MessageEndpoint)
 */
public class MessageEndpointMessageListener extends JmsReceiverObjectSupport implements SessionAwareMessageListener {

    public void onMessage(Message message, Session session) throws JMSException {
        try {
            handle((BytesMessage) message, session);
        }
        catch (Exception ex) {
            JMSException jmsException = new JMSException(ex.getMessage());
            jmsException.setLinkedException(ex);
            throw jmsException;
        }
    }

}
