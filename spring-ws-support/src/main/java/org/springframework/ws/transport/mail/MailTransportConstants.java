/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.mail;

import org.springframework.ws.transport.TransportConstants;

/**
 * Declares Mail-specific transport constants.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public interface MailTransportConstants extends TransportConstants {

	/**
	 * The "mail" URI scheme.
	 */
	String MAIL_URI_SCHEME = "mailto";

	/**
	 * The "In-Reply-To" header.
	 */
	String HEADER_IN_REPLY_TO = "In-Reply-To";

}
