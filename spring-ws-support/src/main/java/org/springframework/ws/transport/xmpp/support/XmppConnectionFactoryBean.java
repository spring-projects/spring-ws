/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.transport.xmpp.support;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Factory to make {@link org.jivesoftware.smack.XMPPConnection} and perform connection and login on the XMPP server
 *
 * @author Gildas Cuisinier
 * @author Arjen Poutsma
 * @since 2.0
 */
public class XmppConnectionFactoryBean implements FactoryBean<XMPPConnection>, InitializingBean, DisposableBean {

    private static final int DEFAULT_PORT = 5222;

    private XMPPConnection connection;

    private String host;

    private int port = DEFAULT_PORT;

    private String serviceName;

    private String username;

    private String password;

    private String resource;

    /** Sets the server host to connect to. */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Sets the the server port to connect to.
     *
     * <p>Defaults to {@code 5222}.
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
    public void afterPropertiesSet() throws XMPPException {
        ConnectionConfiguration configuration = createConnectionConfiguration(host, port, serviceName);
        Assert.notNull(configuration, "'configuration' must not be null");
        Assert.hasText(username, "'username' must not be empty");
        Assert.hasText(password, "'password' must not be empty");

        connection = new XMPPConnection(configuration);
        connection.connect();
        if (StringUtils.hasText(resource)) {
            connection.login(username, password, resource);
        }
        else {
            connection.login(username, password);
        }
    }

    @Override
    public void destroy() {
        connection.disconnect();
    }

    @Override
    public XMPPConnection getObject() {
        return connection;
    }

    @Override
    public Class<XMPPConnection> getObjectType() {
        return XMPPConnection.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Creates the {@code ConnectionConfiguration} from the given parameters.
     *
     * @param host        the host to connect to
     * @param port        the port to connect to
     * @param serviceName the name of the service to connect to. May be {@code null}
     */
    protected ConnectionConfiguration createConnectionConfiguration(String host, int port, String serviceName) {
        Assert.hasText(host, "'host' must not be empty");
        if (StringUtils.hasText(serviceName)) {
            return new ConnectionConfiguration(host, port, serviceName);
        }
        else {
            return new ConnectionConfiguration(host, port);
        }
    }


}