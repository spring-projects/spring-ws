/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
