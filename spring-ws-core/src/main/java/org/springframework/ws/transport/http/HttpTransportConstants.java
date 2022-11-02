/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.transport.http;

import org.springframework.ws.transport.TransportConstants;

/**
 * Declares HTTP-specific transport constants.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface HttpTransportConstants extends TransportConstants {

	/** The "Content-Encoding" header. */
	String HEADER_CONTENT_ENCODING = "Content-Encoding";

	/** The "Accept-Encoding" header. */
	String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

	/** Header value that indicates a compressed "Content-Encoding". */
	String CONTENT_ENCODING_GZIP = "gzip";

	/** The "200 OK" status code. */
	int STATUS_OK = 200;

	/** The "202 Accepted" status code. */
	int STATUS_ACCEPTED = 202;

	/** The "204 No Content" status code. */
	int STATUS_NO_CONTENT = 204;

	/** The "400 Bad Request" status code. */
	int STATUS_BAD_REQUEST = 400;

	/** The "404 Not Found" status code. */
	int STATUS_NOT_FOUND = 404;

	/** The "405 Method Not Allowed" status code. */
	int STATUS_METHOD_NOT_ALLOWED = 405;

	/** The "500 Server Error" status code. */
	int STATUS_INTERNAL_SERVER_ERROR = 500;

	/** The "http" URI scheme. */
	String HTTP_URI_SCHEME = "http";

	/** The "https" URI scheme. */
	String HTTPS_URI_SCHEME = "https";

	/** The "GET" HTTP method */
	String METHOD_GET = "GET";

	/** The "POST" HTTP method */
	String METHOD_POST = "POST";
}
