/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.jms;

import jakarta.jms.BytesMessage;
import jakarta.jms.TextMessage;

import org.springframework.ws.transport.TransportConstants;

/**
 * Declares JMS-specific transport constants.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public interface JmsTransportConstants extends TransportConstants {

	/** The "jms" URI scheme" */
	String JMS_URI_SCHEME = "jms";

	/** Indicates a {@link BytesMessage} type. */
	int BYTES_MESSAGE_TYPE = 1;

	/** Indicates a {@link TextMessage} type. */
	int TEXT_MESSAGE_TYPE = 2;

	/** Prefix for JMS properties that map to transport headers. */
	String PROPERTY_PREFIX = "SOAPJMS_";

	/** JMS property used for storing {@link #HEADER_ACCEPT_ENCODING}. */
	String PROPERTY_ACCEPT_ENCODING = PROPERTY_PREFIX + "acceptEncoding";

	/** JMS property used for storing {@link #HEADER_SOAP_ACTION}. */
	String PROPERTY_SOAP_ACTION = PROPERTY_PREFIX + "soapAction";

	/** JMS property used for storing {@link #HEADER_CONTENT_LENGTH}. */
	String PROPERTY_CONTENT_LENGTH = PROPERTY_PREFIX + "contentLength";

	/** JMS property used for storing {@link #HEADER_CONTENT_TYPE}. */
	String PROPERTY_CONTENT_TYPE = PROPERTY_PREFIX + "contentType";

}
