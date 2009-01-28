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
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.WebServiceMessageReceiver;

/**
 * Spring {@link SessionAwareMessageListener} that can be used to handle incoming {@link Message} service requests.
 * <p/>
 * Requires a {@link WebServiceMessageFactory} which is used to convert the incoming JMS {@link BytesMessage} into a
 * {@link WebServiceMessage}, and passes that to the {@link WebServiceMessageReceiver} {@link
 * #setMessageReceiver(WebServiceMessageReceiver) registered}.
 *
 * @author Arjen Poutsma
 * @see #setMessageFactory(org.springframework.ws.WebServiceMessageFactory)
 * @see #setMessageReceiver(org.springframework.ws.transport.WebServiceMessageReceiver)
 * @since 1.5.0
 */
public class WebServiceMessageListener extends JmsMessageReceiver implements SessionAwareMessageListener {

    public void onMessage(Message message, Session session) throws JMSException {
        try {
            handleMessage(message, session);
        }
        catch (JmsTransportException ex) {
            throw ex.getJmsException();
        }
        catch (Exception ex) {
            JMSException jmsException = new JMSException(ex.getMessage());
            jmsException.setLinkedException(ex);
            throw jmsException;
        }
    }

}
