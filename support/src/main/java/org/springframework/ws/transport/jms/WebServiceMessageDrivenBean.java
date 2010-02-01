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

package org.springframework.ws.transport.jms;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.naming.NamingException;

import org.springframework.ejb.support.AbstractJmsMessageDrivenBean;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jndi.JndiLookupFailureException;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.WebServiceMessageReceiver;

/**
 * EJB {@link MessageDrivenBean} that can be used to handleMessage incoming JMS messages.
 * <p/>
 * This class needs a JMS {@link ConnectionFactory}, a {@link WebServiceMessageFactory} and {@link
 * WebServiceMessageReceiver} to operate. By default, these are obtained by doing a bean lookup on the bean factory
 * provided by {@link #getBeanFactory()} the super class.
 *
 * @author Arjen Poutsma
 * @see #createConnectionFactory()
 * @see #createMessageFactory()
 * @see #createMessageReceiver()
 */
public class WebServiceMessageDrivenBean extends AbstractJmsMessageDrivenBean {

    /** Well-known name for the {@link ConnectionFactory} object in the bean factory for this bean. */
    public static final String CONNECTION_FACTORY_BEAN_NAME = "connectionFactory";

    /** Well-known name for the {@link WebServiceMessageFactory} bean in the bean factory for this bean. */
    public static final String MESSAGE_FACTORY_BEAN_NAME = "messageFactory";

    /** Well-known name for the {@link WebServiceMessageReceiver} object in the bean factory for this bean. */
    public static final String MESSAGE_RECEIVER_BEAN_NAME = "messageReceiver";

    private JmsMessageReceiver delegate;

    private ConnectionFactory connectionFactory;

    /** Delegates to {@link JmsMessageReceiver#handleMessage(Message,Session)}. */
    public void onMessage(Message message) {
        Connection connection = null;
        Session session = null;
        try {
            connection = createConnection(connectionFactory);
            session = createSession(connection);
            delegate.handleMessage(message, session);
        }
        catch (JmsTransportException ex) {
            throw JmsUtils.convertJmsAccessException(ex.getJmsException());
        }
        catch (JMSException ex) {
            throw JmsUtils.convertJmsAccessException(ex);
        }
        catch (Exception ex) {
            throw new EJBException(ex);
        }
        finally {
            JmsUtils.closeSession(session);
            ConnectionFactoryUtils.releaseConnection(connection, connectionFactory, true);
        }
    }

    /**
     * Creates a new {@link Connection}, {@link WebServiceMessageFactory}, and {@link WebServiceMessageReceiver}.
     *
     * @see #createConnectionFactory()
     * @see #createMessageFactory()
     * @see #createMessageReceiver()
     */
    @Override
    protected void onEjbCreate() {
        try {
            connectionFactory = createConnectionFactory();
            delegate = new JmsMessageReceiver();
            delegate.setMessageFactory(createMessageFactory());
            delegate.setMessageReceiver(createMessageReceiver());
        }
        catch (NamingException ex) {
            throw new JndiLookupFailureException("Could not create connection", ex);
        }
        catch (JMSException ex) {
            throw JmsUtils.convertJmsAccessException(ex);
        }
        catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    /** Creates a connection factory. Default implemantion does a bean lookup for {@link #CONNECTION_FACTORY_BEAN_NAME}. */
    protected ConnectionFactory createConnectionFactory() throws Exception {
        return (ConnectionFactory) getBeanFactory().getBean(CONNECTION_FACTORY_BEAN_NAME, ConnectionFactory.class);
    }

    /** Creates a message factory. Default implemantion does a bean lookup for {@link #MESSAGE_FACTORY_BEAN_NAME}. */
    protected WebServiceMessageFactory createMessageFactory() {
        return (WebServiceMessageFactory) getBeanFactory()
                .getBean(MESSAGE_FACTORY_BEAN_NAME, WebServiceMessageFactory.class);
    }

    /** Creates a connection factory. Default implemantion does a bean lookup for {@link #MESSAGE_RECEIVER_BEAN_NAME}. */
    protected WebServiceMessageReceiver createMessageReceiver() {
        return (WebServiceMessageReceiver) getBeanFactory()
                .getBean(MESSAGE_RECEIVER_BEAN_NAME, WebServiceMessageReceiver.class);
    }

    /**
     * Create a JMS {@link Connection} using the given {@link ConnectionFactory}.
     * <p/>
     * This implementation uses JMS 1.1 API.
     *
     * @param connectionFactory the JMS ConnectionFactory to create a Connection with
     * @return the new JMS Connection
     * @throws JMSException if thrown by JMS API methods
     * @see ConnectionFactory#createConnection()
     */
    protected Connection createConnection(ConnectionFactory connectionFactory) throws JMSException {
        return connectionFactory.createConnection();
    }

    /**
     * Creates a JMS {@link Session}. Default implemantion creates a non-transactional, {@link Session#AUTO_ACKNOWLEDGE
     * auto acknowledged} session.
     * <p/>
     * This implementation uses JMS 1.1 API.
     *
     * @param connection the JMS Connection to create a Session for
     * @return the new JMS Session
     * @throws JMSException if thrown by JMS API methods
     * @see Connection#createSession(boolean,int)
     */
    protected Session createSession(Connection connection) throws JMSException {
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

}
