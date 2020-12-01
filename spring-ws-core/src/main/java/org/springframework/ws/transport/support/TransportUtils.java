/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.transport.support;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Generic utility methods for working with Web service transports. Mainly for internal use within the framework.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class TransportUtils {

	private static final Log logger = LogFactory.getLog(TransportUtils.class);

	/**
	 * Close the given {@link WebServiceConnection} and ignore any thrown exception. This is useful for typical
	 * {@code finally} blocks.
	 *
	 * @param connection the web service connection to close (may be {@code null})
	 */
	public static void closeConnection(WebServiceConnection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (IOException ex) {
				logger.debug("Could not close WebServiceConnection", ex);
			} catch (Throwable ex) {
				logger.debug("Unexpected exception on closing WebServiceConnection", ex);
			}
		}
	}

}
