/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.transport.context;

import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Default implementation of the {@code TransportContext} interface.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class DefaultTransportContext implements TransportContext {

	private final WebServiceConnection connection;

	/** Creates a new {@code DefaultTransportContext} that exposes the given connection. */
	public DefaultTransportContext(WebServiceConnection connection) {
		Assert.notNull(connection, "'connection' must not be null");
		this.connection = connection;
	}

	@Override
	public WebServiceConnection getConnection() {
		return connection;
	}

	public String toString() {
		return connection.toString();
	}
}
