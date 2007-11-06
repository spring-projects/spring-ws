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
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * {@link WebServiceMessageSender} implementation that uses JMS.
 * <p/>
 * This message sender sends the request message of the queue configured with either the <code>queue</code> or
 * <code>queueName</code> property. It creates a temporary queue for the response message. For both request and response
 * {@link BytesMessage}s are used.
 *
 * @author Arjen Poutsma
 */
public class JmsMessageSender implements WebServiceMessageSender, JmsTransportConstants {

    /**
     * Default timeout for receive operations: -1 indicates a blocking receive without timeout.
     */
    public static final long DEFAULT_RECEIVE_TIMEOUT = -1;

    private ConnectionFactory connectionFactory;

    private DestinationResolver destinationResolver = new DynamicDestinationResolver();

    private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

    public JmsMessageSender() {
    }

    public JmsMessageSender(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Set the ConnectionFactory to use for obtaining JMS {@link Connection}s.
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setDestinationResolver(DestinationResolver destinationResolver) {
        this.destinationResolver = destinationResolver;
    }

    /**
     * Set the timeout to use for receive calls. The default is 0, which means no timeout.
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public WebServiceConnection createConnection(String uriString) throws IOException {
        Assert.notNull(connectionFactory, "connectionFactory must not be null");
        JmsSenderConnection connection = null;
        try {
            JmsUri uri = new JmsUri(uriString);
            connection = new JmsSenderConnection(uri, connectionFactory, destinationResolver, receiveTimeout);
            return connection;
        }
        catch (JMSException ex) {
            if (connection != null) {
                connection.close();
            }
            throw new JmsTransportException(ex);
        }
    }

    public boolean supports(String uri) {
        return StringUtils.hasLength(uri) && uri.startsWith(URI_SCHEME + ":");
    }

}
