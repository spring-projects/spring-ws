/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport;

/**
 * Declares useful transport constants.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface TransportConstants {

	/** The "Accept" header. */
	String HEADER_ACCEPT = "Accept";

	/** The "Accept-Encoding" header. */
	String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

	/** The "Content-Id" header. */
	String HEADER_CONTENT_ID = "Content-Id";

	/** The "Content-Length" header. */
	String HEADER_CONTENT_LENGTH = "Content-Length";

	/** The "Content-Transfer-Encoding" header. */
	String HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";

	/** The "Content-Type" header. */
	String HEADER_CONTENT_TYPE = "Content-Type";

	/** The "SOAPAction" header, used in SOAP 1.1. */
	String HEADER_SOAP_ACTION = "SOAPAction";

	/** The "action" parameter, used to set SOAP Actions in SOAP 1.2. */
	String PARAMETER_ACTION = "action";

	/** The empty SOAP action value. */
	String EMPTY_SOAP_ACTION = "\"\"";

}
