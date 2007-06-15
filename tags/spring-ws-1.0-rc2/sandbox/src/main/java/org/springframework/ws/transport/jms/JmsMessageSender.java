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
import java.util.Properties;
import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jndi.JndiTemplate;
import org.springframework.util.StringUtils;
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

    private static final Log logger = LogFactory.getLog(JmsMessageSender.class);

    /** Default timeout for receive operations: -1 indicates a blocking receive without timeout. */
    public static final long DEFAULT_RECEIVE_TIMEOUT = -1;

    private static final String JMS_SCHEME = "jms:";

    private ConnectionFactory defaultConnectionFactory;

    private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

    public JmsMessageSender() {
    }

    public JmsMessageSender(ConnectionFactory defaultConnectionFactory) {
        this.defaultConnectionFactory = defaultConnectionFactory;
    }

    /** Set the default ConnectionFactory to use for obtaining JMS Connections. */
    public void setDefaultConnectionFactory(ConnectionFactory defaultConnectionFactory) {
        this.defaultConnectionFactory = defaultConnectionFactory;
    }

    /** Set the timeout to use for receive calls. The default is 0, which means no timeout. */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public WebServiceConnection createConnection(String uriString) throws IOException {
        JmsSenderConnection connection = null;
        try {
            JmsUri uri = new JmsUri(uriString);
            ConnectionFactory connectionFactory = resolveConnectionFactory(uri);
            connection = new JmsSenderConnection(uri, connectionFactory, receiveTimeout);
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
        return StringUtils.hasLength(uri) && uri.startsWith(JMS_SCHEME);
    }

    protected ConnectionFactory resolveConnectionFactory(JmsUri uri) {
        if (uri.hasConnectionFactoryName()) {
            Properties environment = new Properties();
            if (uri.hasInitialContextFactory()) {
                environment.setProperty(Context.INITIAL_CONTEXT_FACTORY, uri.getInitialContextFactory());
            }
            if (uri.hasJndiUrl()) {
                environment.setProperty(Context.PROVIDER_URL, uri.getJndiUrl());
            }
            try {
                JndiTemplate jndiTemplate = new JndiTemplate(environment);
                return (ConnectionFactory) jndiTemplate.lookup(uri.getConnectionFactoryName(), ConnectionFactory.class);
            }
            catch (NamingException ex) {
                logger.debug("ConnectionFactory [" + uri.getConnectionFactoryName() + "] not found in JNDI", ex);
                // fall through to the default
            }
        }
        if (defaultConnectionFactory != null) {
            return defaultConnectionFactory;
        }
        else {
            throw new IllegalStateException("Could not resolve JMS ConnectionFactory. " +
                    "Specify a 'defaultConnectionFactory' or 'connectionFactoryName' in the URI.");
        }
    }

}
