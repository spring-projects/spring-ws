/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.jms;

import java.io.IOException;
import java.net.URI;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.JmsDestinationAccessor;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.jms.support.JmsTransportUtils;

/**
 * {@link WebServiceMessageSender} implementation that uses JMS {@link Message}s. Requires a JMS {@link
 * ConnectionFactory} to operate.
 * <p/>
 * This message sender supports URI's of the following format: <blockquote> <tt><b>jms:</b></tt><i>destination</i>[<tt><b>?</b></tt><i>param-name</i><tt><b>=</b></tt><i>param-value</i>][<tt><b>&amp;</b></tt><i>param-name</i><tt><b>=</b></tt><i>param-value</i>]*
 * </blockquote> where the characters <tt><b>:</b></tt>, <tt><b>?</b></tt>, and <tt><b>&amp;</b></tt> stand for
 * themselves. The <i>destination</i> represents the name of the {@link Queue} or {@link Topic} that will be resolved by
 * the {@link #getDestinationResolver() destination resolver}. Valid <i>param-name</i> include:
 * <p/>
 * <blockquote>
 *     <table>
 *         <tr><th><i>param-name</i></th><th><i>Description</i></th></tr>
 *         <tr>
 *             <td><tt>deliveryMode</tt></td>
 *             <td>Indicates whether the request message is persistent or not. This may be <tt>PERSISTENT</tt> or
 *             <tt>NON_PERSISTENT</tt>. See {@link MessageProducer#setDeliveryMode(int)}</td>
 *         </tr>
 *         <tr>
 *             <td><tt>messageType</tt></td>
 *             <td>The message type. This may be <tt>BINARY_MESSAGE</tt> (the default) or <tt>TEXT_MESSAGE</tt></td>
 *         </tr>
 *         <tr>
 *             <td><tt>priority</tt></td>
 *             <td>The JMS priority (0-9) associated with the request message. See
 *             {@link MessageProducer#setPriority(int)}</td>
 *         </tr>
 *         <tr>
 *             <td><tt>replyToName</tt></td>
 *             <td>The name of the destination to which the response message must be sent, that will be resolved by
 *             the {@link #getDestinationResolver() destination resolver}.</td>
 *         </tr>
 *         <tr>
 *             <td><tt>timeToLive</tt></td>
 *             <td>The lifetime, in milliseconds, of the request message. See
 *             {@link MessageProducer#setTimeToLive(long)}</td>
 *         </tr>
 *     </table>
 * </blockquote>
 * <p/>
 * If the <tt>replyToName</tt> is not set, a {@link Session#createTemporaryQueue() temporary queue} is used.
 * <p/>
 * This class uses {@link BytesMessage} messages by default, but can be configured to send {@link TextMessage} messages
 * instead. <b>Note</b> that <code>BytesMessages</code> are preferred, since <code>TextMessages</code> do not support
 * attachments and character encodings reliably.
 * <p/>
 * Some examples of JMS URIs are:
 * <p/>
 * <blockquote> <tt>jms:SomeQueue</tt><br> <tt>jms:SomeTopic?priority=3&deliveryMode=NON_PERSISTENT</tt><br>
 * <tt>jms:RequestQueue?replyToName=ResponseQueueName</tt><br> <tt>jms:Queue?messageType=TEXT_MESSAGE</blockquote>
 *
 * @author Arjen Poutsma
 * @see <a href="http://tools.ietf.org/id/draft-merrick-jms-iri-00.txt">IRI Scheme for Java(tm) Message Service 1.0</a>
 * @since 1.5.0
 */
public class JmsMessageSender extends JmsDestinationAccessor implements WebServiceMessageSender {

    /** Default timeout for receive operations: -1 indicates a blocking receive without timeout. */
    public static final long DEFAULT_RECEIVE_TIMEOUT = -1;

    /** Default encoding used to read fromn and write to {@link TextMessage} messages. */
    public static final String DEFAULT_TEXT_MESSAGE_ENCODING = "UTF-8";

    private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

    private String textMessageEncoding = DEFAULT_TEXT_MESSAGE_ENCODING;

    private MessagePostProcessor postProcessor;

    /**
     * Create a new <code>JmsMessageSender</code>
     * <p/>
     * <b>Note</b>: The ConnectionFactory has to be set before using the instance. This constructor can be used to
     * prepare a JmsTemplate via a BeanFactory, typically setting the ConnectionFactory via {@link
     * #setConnectionFactory(ConnectionFactory)}.
     *
     * @see #setConnectionFactory(ConnectionFactory)
     */
    public JmsMessageSender() {
    }

    /**
     * Create a new <code>JmsMessageSender</code>, given a ConnectionFactory.
     *
     * @param connectionFactory the ConnectionFactory to obtain Connections from
     */
    public JmsMessageSender(ConnectionFactory connectionFactory) {
        setConnectionFactory(connectionFactory);
    }

    /**
     * Set the timeout to use for receive calls. The default is -1, which means no timeout.
     *
     * @see MessageConsumer#receive(long)
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    /** Sets the encoding used to read from {@link TextMessage} messages. Defaults to <code>UTF-8</code>. */
    public void setTextMessageEncoding(String textMessageEncoding) {
        this.textMessageEncoding = textMessageEncoding;
    }

    /**
     * Sets the optional {@link MessagePostProcessor} to further modify outgoing messages after the XML contents has
     * been set.
     */
    public void setPostProcessor(MessagePostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    public WebServiceConnection createConnection(URI uri) throws IOException {
        Connection jmsConnection = null;
        Session jmsSession = null;
        try {
            jmsConnection = createConnection();
            jmsSession = createSession(jmsConnection);
            Destination requestDestination = resolveRequestDestination(jmsSession, uri);
            Message requestMessage = createRequestMessage(jmsSession, uri);
            JmsSenderConnection wsConnection =
                    new JmsSenderConnection(getConnectionFactory(), jmsConnection, jmsSession, requestDestination,
                            requestMessage);
            wsConnection.setDeliveryMode(JmsTransportUtils.getDeliveryMode(uri));
            wsConnection.setPriority(JmsTransportUtils.getPriority(uri));
            wsConnection.setReceiveTimeout(receiveTimeout);
            wsConnection.setResponseDestination(resolveResponseDestination(jmsSession, uri));
            wsConnection.setTimeToLive(JmsTransportUtils.getTimeToLive(uri));
            wsConnection.setTextMessageEncoding(textMessageEncoding);
            wsConnection.setSessionTransacted(isSessionTransacted());
            wsConnection.setPostProcessor(postProcessor);
            return wsConnection;
        }
        catch (JMSException ex) {
            JmsUtils.closeSession(jmsSession);
            ConnectionFactoryUtils.releaseConnection(jmsConnection, getConnectionFactory(), true);
            throw new JmsTransportException(ex);
        }
    }

    public boolean supports(URI uri) {
        return uri.getScheme().equals(JmsTransportConstants.JMS_URI_SCHEME);
    }

    private Destination resolveRequestDestination(Session session, URI uri) throws JMSException {
        return resolveDestinationName(session, JmsTransportUtils.getDestinationName(uri));
    }

    private Destination resolveResponseDestination(Session session, URI uri) throws JMSException {
        String destinationName = JmsTransportUtils.getReplyToName(uri);
        return StringUtils.hasLength(destinationName) ? resolveDestinationName(session, destinationName) : null;
    }

    private Message createRequestMessage(Session session, URI uri) throws JMSException {
        int messageType = JmsTransportUtils.getMessageType(uri);
        if (messageType == JmsTransportConstants.BYTES_MESSAGE_TYPE) {
            return session.createBytesMessage();
        }
        else if (messageType == JmsTransportConstants.TEXT_MESSAGE_TYPE) {
            return session.createTextMessage();
        }
        else {
            throw new IllegalArgumentException("Invalid message type [" + messageType + "].");
        }

    }


}
