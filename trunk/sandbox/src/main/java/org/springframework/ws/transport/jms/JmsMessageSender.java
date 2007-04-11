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

import java.io.IOException;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * <code>WebServiceMessageSender</code> implementation that uses JMS {@link Queue}.
 * <p/>
 * This message sender sends the request message of the queue configured with either the <code>queue</code> or
 * <code>queueName</code> property. It creates a temporary queue for the response message. For both request and response
 * {@link BytesMessage}s are used.
 *
 * @author Arjen Poutsma
 */
public class JmsMessageSender implements WebServiceMessageSender {

    /** Default timeout for receive operations. */
    public static final long DEFAULT_RECEIVE_TIMEOUT = 0;

    private QueueConnectionFactory connectionFactory;

    private Object queue;

    private DestinationResolver destinationResolver = new DynamicDestinationResolver();

    private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

    /** Set the QueueConnectionFactory to use for obtaining JMS QueueConnections. */
    public void setConnectionFactory(QueueConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /** Set the target Queue to send invoker requests to. */
    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    /** Set the name of target queue to send invoker requests to. */
    public void setQueueName(String queueName) {
        queue = queueName;
    }

    /** Set the timeout to use for receive calls. The default is 0, which means no timeout. */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    /**
     * Set the DestinationResolver that is to be used to resolve Queue references for this accessor. <p>The default
     * resolver is a DynamicDestinationResolver. Specify a JndiDestinationResolver for resolving destination names as
     * JNDI locations.
     *
     * @param destinationResolver the DestinationResolver that is to be used
     * @see org.springframework.jms.support.destination.DynamicDestinationResolver
     * @see org.springframework.jms.support.destination.JndiDestinationResolver
     */
    public void setDestinationResolver(DestinationResolver destinationResolver) {
        Assert.notNull(destinationResolver, "DestinationResolver must not be null");
        this.destinationResolver = destinationResolver;
    }

    public void afterPropertiesSet() {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory is required");
        }
        if (queue == null) {
            throw new IllegalArgumentException("'queue' or 'queueName' is required");
        }
    }

    /**
     * Resolve this accessor's target queue.
     *
     * @param session the current JMS Session
     * @return the resolved target Queue
     * @throws JMSException if resolution failed
     */
    protected Queue resolveQueue(Session session) throws JMSException {
        if (queue instanceof Queue) {
            return (Queue) queue;
        }
        else if (queue instanceof String) {
            return resolveQueueName(session, (String) queue);
        }
        else {
            throw new javax.jms.IllegalStateException(
                    "Queue object [" + queue + "] is neither a [javax.jms.Queue] nor a queue name String");
        }
    }

    /**
     * Resolve the given queue name into a JMS {@link javax.jms.Queue}, via this accessor's {@link
     * DestinationResolver}.
     *
     * @param session   the current JMS Session
     * @param queueName the name of the queue
     * @return the located Queue
     * @throws JMSException if resolution failed
     * @see #setDestinationResolver
     */
    protected Queue resolveQueueName(Session session, String queueName) throws JMSException {
        return (Queue) destinationResolver.resolveDestinationName(session, queueName, false);
    }

    public WebServiceConnection createConnection() throws IOException {
        try {
            QueueConnection con = connectionFactory.createQueueConnection();
            QueueSession session = con.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queueToUse = resolveQueue(session);
            return new JmsSendingWebServiceConnection(con, session, queueToUse, receiveTimeout);
        }
        catch (JMSException ex) {
            throw new JmsTransportException(ex);
        }
    }
}
