/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.xmpp.support;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jivesoftware.smackx.jiveproperties.JivePropertiesManager;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.transport.xmpp.XmppTransportConstants;

/**
 * Collection of utility methods to work with Mail transports.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class XmppTransportUtils {

	private XmppTransportUtils() {
	}

	/**
	 * Converts the given XMPP destination into a {@code xmpp} URI.
	 */
	public static URI toUri(MessageBuilder requestMessage) throws URISyntaxException {
		return new URI(XmppTransportConstants.XMPP_URI_SCHEME, requestMessage.getTo().asUnescapedString(), null);
	}

	public static URI toUri(Message requestMessage) throws URISyntaxException {
		return new URI(XmppTransportConstants.XMPP_URI_SCHEME, requestMessage.getTo().asUnescapedString(), null);
	}

	public static String getTo(URI uri) {
		return uri.getSchemeSpecificPart();
	}

	public static boolean hasError(@Nullable MessageBuilder message) {
		return message != null && Message.Type.error.equals(message.getType());
	}

	public static @Nullable String getErrorMessage(@Nullable MessageBuilder messageBuilder) {
		if (messageBuilder == null || !Message.Type.error.equals(messageBuilder.getType())) {
			return null;
		}
		else {
			return messageBuilder.getBody();
		}
	}

	public static void addHeader(MessageBuilder message, String name, String value) {
		JivePropertiesManager.addProperty(message, name, value);
	}

	public static Iterator<String> getHeaderNames(Message message) {
		Assert.notNull(message, "'message' must not be null");
		return JivePropertiesManager.getPropertiesNames(message).iterator();
	}

	public static Iterator<String> getHeaders(Message message, String name) {
		Assert.notNull(message, "'message' must not be null");
		String value = JivePropertiesManager.getProperty(message, name).toString();
		if (value != null) {
			return Collections.singletonList(value).iterator();
		}
		else {
			return Collections.emptyIterator();
		}
	}

}
