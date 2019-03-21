/*
 * Copyright 2005-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.xmpp.support;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.jiveproperties.JivePropertiesManager;

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
	public static URI toUri(Message requestMessage) throws URISyntaxException {
		return new URI(XmppTransportConstants.XMPP_URI_SCHEME,
				requestMessage.getTo().asUnescapedString(), null);
	}

	public static String getTo(URI uri) {
		return uri.getSchemeSpecificPart();
	}

	public static boolean hasError(Message message) {
		return message != null && Message.Type.error.equals(message.getType());
	}

	public static String getErrorMessage(Message message) {
		if (message == null || !Message.Type.error.equals(message.getType())) {
			return null;
		}
		else {
			return message.getBody();
		}
	}

	public static void addHeader(Message message, String name, String value) {
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
			return Collections.<String>emptyList().iterator();
		}
	}

}
