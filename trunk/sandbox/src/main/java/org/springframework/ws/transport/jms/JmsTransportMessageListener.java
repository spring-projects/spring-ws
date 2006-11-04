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

import java.io.IOException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.endpoint.MessageEndpoint;
import org.springframework.ws.transport.SimpleTransportContext;
import org.springframework.ws.transport.TransportContext;
import org.springframework.ws.transport.TransportContextHolder;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * @author Arjen Poutsma
 */
public class JmsTransportMessageListener implements MessageListener, InitializingBean {

    private static final Log logger = LogFactory.getLog(JmsTransportMessageListener.class);

    private WebServiceMessageFactory messageFactory;

    private MessageEndpoint endpoint;

    public void setMessageFactory(WebServiceMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(messageFactory, "messageFactory is required");
        logger.info("Using message factory [" + messageFactory + "]");
    }

    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                handleTextMessage(textMessage);
            }
            catch (IOException ex) {
                throw new JmsTransportException("Could not create message: " + ex.getMessage(), ex);
            }
            catch (Exception ex) {
                throw new JmsTransportException("Could not handle message: " + ex.getMessage(), ex);
            }
        }
        else {
            throw new JmsTransportException("JmsTransportMessageListener can only handle TextMessages");
        }
    }

    private void handleTextMessage(TextMessage textMessage) throws Exception {
        TransportInputStream tis = new JmsTransportInputStream(textMessage);
        TransportOutputStream tos = new JmsTransportOutputStream(getSession());

        TransportContext previousTransportContext = TransportContextHolder.getTransportContext();
        TransportContextHolder.setTransportContext(new SimpleTransportContext(tis, tos));

        try {
            WebServiceMessage messageRequest = messageFactory.createWebServiceMessage(tis);
            MessageContext messageContext = new DefaultMessageContext(messageRequest, messageFactory);
            endpoint.invoke(messageContext);
            if (messageContext.hasResponse()) {
                WebServiceMessage messageResponse = messageContext.getResponse();
                messageResponse.writeTo(tos);
            }
        }
        catch (NoEndpointFoundException ex) {
            // do nothing
        }
        finally {
            TransportContextHolder.setTransportContext(previousTransportContext);
        }

    }

    private Session getSession() {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }
}
