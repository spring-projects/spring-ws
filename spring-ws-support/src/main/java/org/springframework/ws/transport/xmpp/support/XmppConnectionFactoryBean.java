/*
 * Copyright 2005-present the original author or authors.
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

import java.io.IOException;
import java.util.Objects;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jspecify.annotations.Nullable;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Factory to make {@link org.jivesoftware.smack.XMPPConnection} and perform connection
 * and login on the XMPP server.
 *
 * @author Gildas Cuisinier
 * @author Arjen Poutsma
 * @since 2.0
 */
public class XmppConnectionFactoryBean implements FactoryBean<XMPPTCPConnection>, InitializingBean, DisposableBean {

	private static final int DEFAULT_PORT = 5222;

	private @Nullable XMPPTCPConnection connection;

	private @Nullable String host;

	private int port = DEFAULT_PORT;

	private @Nullable String serviceName;

	private @Nullable String username;

	private @Nullable String password;

	private @Nullable String resource;

	/** Sets the server host to connect to. */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Sets the server port to connect to.
	 * <p>
	 * Defaults to {@code 5222}.
	 */
	public void setPort(int port) {
		Assert.isTrue(port > 0, "'port' must be larger than 0");
		this.port = port;
	}

	/** Sets the service name to connect to. */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	@Override
	public void afterPropertiesSet() throws XMPPException, IOException, SmackException {
		Assert.hasText(this.host, "'host' must not be empty");
		XMPPTCPConnectionConfiguration configuration = createConnectionConfiguration(this.host, this.port,
				this.serviceName);
		Assert.notNull(configuration, "'configuration' must not be null");
		Assert.hasText(this.username, "'username' must not be empty");
		Assert.hasText(this.password, "'password' must not be empty");

		this.connection = new XMPPTCPConnection(configuration);
		try {
			this.connection.connect();
			if (StringUtils.hasText(this.resource)) {
				this.connection.login(this.username, this.password, Resourcepart.from(this.resource));
			}
			else {
				this.connection.login(this.username, this.password);
			}
		}
		catch (InterruptedException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void destroy() {
		Objects.requireNonNull(this.connection).disconnect();
	}

	@Override
	public XMPPTCPConnection getObject() {
		return Objects.requireNonNull(this.connection);
	}

	@Override
	public Class<XMPPTCPConnection> getObjectType() {
		return XMPPTCPConnection.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * Creates the {@code ConnectionConfiguration} from the given parameters.
	 * @param host the host to connect to
	 * @param port the port to connect to
	 * @param serviceName the name of the service to connect to. May be {@code null}
	 */
	protected XMPPTCPConnectionConfiguration createConnectionConfiguration(String host, int port,
			@Nullable String serviceName) throws XmppStringprepException {
		Assert.hasText(host, "'host' must not be empty");
		if (StringUtils.hasText(serviceName)) {
			return XMPPTCPConnectionConfiguration.builder()
				.setHost(host)
				.setPort(port)
				.setXmppDomain(serviceName)
				.build();
		}
		else {
			return XMPPTCPConnectionConfiguration.builder().setHost(host).setPort(port).build();
		}
	}

}
