/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.xmpp;

import org.springframework.ws.transport.TransportConstants;

/**
 * Declares XMPP-specific transport constants.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface XmppTransportConstants extends TransportConstants {

	/**
	 * The "xmpp" URI scheme.
	 */
	String XMPP_URI_SCHEME = "xmpp";

}
