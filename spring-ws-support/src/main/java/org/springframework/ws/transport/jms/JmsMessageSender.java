/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.jms;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import org.apache.commons.logging.Log;
import org.jspecify.annotations.Nullable;

import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.JmsDestinationAccessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.jms.support.JmsTransportUtils;

/**
 * {@link WebServiceMessageSender} implementation that uses JMS {@link Message}s. Requires
 * a JMS {@link ConnectionFactory} to operate.
 * <p>
 * This message sender supports URI's of the following format: <blockquote>
 * {@code jms:<destination>[?param-name=param-value][&param-name=param-value]}
 * </blockquote> where the characters {@code :}, {@code ?}, and {@code &} stand for
 * themselves. The {@code <destination>} represents the name of the {@link Queue} or
 * {@link Topic} that will be resolved by the {@link #getDestinationResolver() destination
 * resolver}. Valid {@code param-name} include:
 * <table>
 * <caption>Parameter Names</caption>
 * <tr>
 * <th><i>param-name</i></th>
 * <th><i>Description</i></th>
 * </tr>
 * <tr>
 * <td>{@code deliveryMode}</td>
 * <td>Indicates whether the request message is persistent or not. This may be
 * {@code PERSISTENT} or {@code NON_PERSISTENT}. See
 * {@link jakarta.jms.MessageProducer#setDeliveryMode(int)}</td>
 * </tr>
 * <tr>
 * <td>{@code messageType}</td>
 * <td>The message type. This may be {@code BINARY_MESSAGE} (the default) or
 * {@code TEXT_MESSAGE}</td>
 * </tr>
 * <tr>
 * <td>{@code priority}</td>
 * <td>The JMS priority (0-9) associated with the request message. See
 * {@link jakarta.jms.MessageProducer#setPriority(int)}</td>
 * </tr>
 * <tr>
 * <td>{@code replyToName}</td>
 * <td>The name of the destination to which the response message must be sent, that will
 * be resolved by the {@link #getDestinationResolver() destination resolver}</td>
 * </tr>
 * <tr>
 * <td>{@code timeToLive}</td>
 * <td>The lifetime, in milliseconds, of the request message. See
 * {@link jakarta.jms.MessageProducer#setTimeToLive(long)}</td>
 * </tr>
 * </table>
 * <p>
 * If the {@code replyToName} is not set, a {@link Session#createTemporaryQueue()
 * temporary queue} is used.
 * <p>
 * This class uses {@link jakarta.jms.BytesMessage} messages by default, but can be
 * configured to send {@link jakarta.jms.TextMessage} messages instead. <b>Note</b> that
 * {@code BytesMessages} are preferred, since {@code TextMessages} do not support
 * attachments and character encodings reliably.
 * <p>
 * Some examples of JMS URIs are: <blockquote> {@code jms:SomeQueue}<br>
 * {@code jms:SomeTopic?priority=3&deliveryMode=NON_PERSISTENT}<br>
 * {@code jms:RequestQueue?replyToName=ResponseQueueName}<br>
 * {@code jms:Queue?messageType=TEXT_MESSAGE}</blockquote>
 * <p>
 * Any {@code jms:} URI supplied by the application is accepted. A URI supplied by a
 * {@linkplain UriSource#REMOTE remote} peer, such as a WS-Addressing {@code ReplyTo} or
 * {@code FaultTo} address, is only accepted when a {@link DestinationPolicy
 * DestinationPolicy} accepts it; without a policy, every remote destination is rejected.
 * Use {@link JmsDestinationPolicies#allowedDestinationNames(String...)} for the common
 * case. Remember that both the destination name and the {@code replyToName} are handed to
 * the {@link #getDestinationResolver() destination resolver}, so a policy has to
 * constrain both. That matters most when the resolver performs a JNDI lookup, as it then
 * treats either name as a lookup key.
 *
 * @author Arjen Poutsma
 * @author Stephane Nicoll
 * @since 1.5.0
 * @see <a href="https://datatracker.ietf.org/doc/rfc6167">IRI Scheme for Java(tm) Message
 * Service 1.0</a>
 */
public class JmsMessageSender extends JmsDestinationAccessor implements WebServiceMessageSender {

	/**
	 * Default timeout for receive operations: -1 indicates a blocking receive without
	 * timeout.
	 */
	public static final long DEFAULT_RECEIVE_TIMEOUT = -1;

	/**
	 * Default encoding used to read from and write to {@link jakarta.jms.TextMessage}
	 * messages.
	 */
	public static final String DEFAULT_TEXT_MESSAGE_ENCODING = "UTF-8";

	private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

	private String textMessageEncoding = DEFAULT_TEXT_MESSAGE_ENCODING;

	private @Nullable MessagePostProcessor postProcessor;

	private @Nullable DestinationPolicy<JmsDestinationDescriptor> destinationPolicy;

	private final DestinationPolicy<JmsDestinationDescriptor> fallbackDestinationPolicy = new FallbackDestinationPolicy<>();

	/**
	 * Create a new {@code JmsMessageSender}
	 * <p>
	 * <b>Note</b>: The ConnectionFactory has to be set before using the instance. This
	 * constructor can be used to prepare a JmsTemplate via a BeanFactory, typically
	 * setting the ConnectionFactory via {@link #setConnectionFactory(ConnectionFactory)}.
	 * @see #setConnectionFactory(ConnectionFactory)
	 */
	public JmsMessageSender() {
	}

	/**
	 * Create a new {@code JmsMessageSender}, given a ConnectionFactory.
	 * @param connectionFactory the ConnectionFactory to obtain Connections from
	 */
	public JmsMessageSender(ConnectionFactory connectionFactory) {
		setConnectionFactory(connectionFactory);
	}

	/**
	 * Set the timeout to use for receive calls. The default is -1, which means no
	 * timeout.
	 * @see jakarta.jms.MessageConsumer#receive(long)
	 */
	public void setReceiveTimeout(long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	/**
	 * Sets the encoding used to read from {@link jakarta.jms.TextMessage} messages.
	 * Defaults to {@code UTF-8}.
	 */
	public void setTextMessageEncoding(String textMessageEncoding) {
		this.textMessageEncoding = textMessageEncoding;
	}

	/**
	 * Sets the optional {@link MessagePostProcessor} to further modify outgoing messages
	 * after the XML contents has been set.
	 */
	public void setPostProcessor(MessagePostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}

	/**
	 * Set the {@link DestinationPolicy} that decides which destinations this sender may
	 * open a connection to.
	 * <p>
	 * A policy is required to send to a {@link UriSource#REMOTE} destination, such as a
	 * WS-Addressing {@code ReplyTo} or {@code FaultTo} address: when none is configured,
	 * every remote destination is rejected.
	 * @param destinationPolicy the policy to apply
	 * @since 3.1.9
	 */
	public void setDestinationPolicy(DestinationPolicy<JmsDestinationDescriptor> destinationPolicy) {
		this.destinationPolicy = destinationPolicy;
	}

	@Override
	public WebServiceConnection createConnection(URI uri) throws IOException {
		ConnectionFactory connectionFactory = getConnectionFactory();
		Assert.notNull(connectionFactory, "ConnectionFactory is required");
		Connection jmsConnection = null;
		Session jmsSession = null;
		try {
			jmsConnection = createConnection();
			jmsSession = createSession(jmsConnection);
			Destination requestDestination = resolveRequestDestination(jmsSession, uri);
			Message requestMessage = createRequestMessage(jmsSession, uri);
			JmsSenderConnection wsConnection = new JmsSenderConnection(connectionFactory, jmsConnection, jmsSession,
					requestDestination, requestMessage);
			wsConnection.setDeliveryMode(JmsTransportUtils.getDeliveryMode(uri));
			wsConnection.setPriority(JmsTransportUtils.getPriority(uri));
			wsConnection.setReceiveTimeout(this.receiveTimeout);
			wsConnection.setResponseDestination(resolveResponseDestination(jmsSession, uri));
			wsConnection.setTimeToLive(JmsTransportUtils.getTimeToLive(uri));
			wsConnection.setTextMessageEncoding(this.textMessageEncoding);
			wsConnection.setSessionTransacted(isSessionTransacted());
			wsConnection.setPostProcessor(this.postProcessor);
			return wsConnection;
		}
		catch (JMSException ex) {
			JmsUtils.closeSession(jmsSession);
			ConnectionFactoryUtils.releaseConnection(jmsConnection, getConnectionFactory(), true);
			throw new JmsTransportException(ex);
		}
	}

	@Override
	public boolean supports(URI uri, UriSource uriSource) {
		if (!uri.getScheme().equals(JmsTransportConstants.JMS_URI_SCHEME)) {
			return false;
		}
		DestinationPolicy<JmsDestinationDescriptor> destinationPolicyToUse = (this.destinationPolicy != null)
				? this.destinationPolicy : this.fallbackDestinationPolicy;
		return destinationPolicyToUse.accept(JmsDestinationDescriptor.of(uri, uriSource));
	}

	@Override
	public boolean supportsRemoteDestinations() {
		return this.destinationPolicy != null;
	}

	private Destination resolveRequestDestination(Session session, URI uri) throws JMSException {
		String destinationName = JmsTransportUtils.getDestinationName(uri);
		Assert.notNull(destinationName, "No destination name found for URI [" + uri + "]");
		return resolveDestinationName(session, destinationName);
	}

	private @Nullable Destination resolveResponseDestination(Session session, URI uri) throws JMSException {
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

	private final class FallbackDestinationPolicy<T extends DestinationDescriptor> implements DestinationPolicy<T> {

		private final AtomicBoolean missingDestinationPolicyLogged = new AtomicBoolean();

		@Override
		public boolean accept(T descriptor) {
			if (descriptor.uriSource() == UriSource.REMOTE) {
				logMissingDestinationPolicy(descriptor.uri());
				return false;
			}
			return true;
		}

		private void logMissingDestinationPolicy(URI uri) {
			Log loggerToUse = JmsMessageSender.this.logger;
			if (this.missingDestinationPolicyLogged.compareAndSet(false, true)) {
				loggerToUse.warn("Rejected remote destination [" + uri + "]: no DestinationPolicy is configured on "
						+ getClass().getName() + ", so every destination supplied by a remote peer is rejected. "
						+ "Call setDestinationPolicy with an explicit allowlist of the destinations that out-of-band "
						+ "replies may target. Further rejections are logged at debug level.");
			}
			else if (loggerToUse.isDebugEnabled()) {
				loggerToUse.debug("Rejected remote destination [" + uri + "]: no DestinationPolicy is configured on "
						+ getClass().getName());
			}
		}

	}

}
