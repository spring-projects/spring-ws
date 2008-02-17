/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.client.support;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Base class for <code>WebServiceTemplate</code> and other WS-accessing helpers. Defines common properties like the
 * {@link WebServiceMessageFactory} and {@link WebServiceMessageSender}.
 * <p/>
 * Not intended to be used directly. See {@link org.springframework.ws.client.core.WebServiceTemplate}.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.client.core.WebServiceTemplate
 * @since 1.0.0
 */
public abstract class WebServiceAccessor extends TransformerObjectSupport implements InitializingBean {

    private WebServiceMessageFactory messageFactory;

    private WebServiceMessageSender[] messageSenders;

    /** Returns the message factory used for creating messages. */
    public WebServiceMessageFactory getMessageFactory() {
        return messageFactory;
    }

    /** Sets the message factory used for creating messages. */
    public void setMessageFactory(WebServiceMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /** Returns the message senders used for sending messages. */
    public WebServiceMessageSender[] getMessageSenders() {
        return messageSenders;
    }

    /**
     * Sets the single message sender used for sending messages.
     * <p/>
     * This message sender will be used to resolve an URI to a {@link WebServiceConnection}.
     *
     * @see #createConnection(URI)
     */
    public void setMessageSender(WebServiceMessageSender messageSender) {
        Assert.notNull(messageSender, "'messageSender' must not be null");
        messageSenders = new WebServiceMessageSender[]{messageSender};
    }

    /**
     * Sets the message senders used for sending messages.
     * <p/>
     * These message senders will be used to resolve an URI to a {@link WebServiceConnection}.
     *
     * @see #createConnection(URI)
     */
    public void setMessageSenders(WebServiceMessageSender[] messageSenders) {
        Assert.notEmpty(messageSenders, "'messageSenders' must not be empty");
        this.messageSenders = messageSenders;
    }

    public void afterPropertiesSet() {
        Assert.notNull(getMessageFactory(), "Property 'messageFactory' is required");
        Assert.notEmpty(getMessageSenders(), "Property 'messageSenders' is required");
    }

    /**
     * Creates a connection to the given URI, or throws an exception when it cannot be resolved.
     * <p/>
     * Default implementation iterates over all configured {@link WebServiceMessageSender} objects, and calls {@link
     * WebServiceMessageSender#supports(URI)} for each of them. If the sender supports the parameter URI, it creates a
     * connection using {@link WebServiceMessageSender#createConnection(URI)} .
     *
     * @param uri the URI to open a connection to
     * @return the created connection
     * @throws IllegalArgumentException when the uri cannot be resolved
     * @throws IOException              when an I/O error occurs
     */
    protected WebServiceConnection createConnection(URI uri) throws IOException {
        Assert.notEmpty(getMessageSenders(), "Property 'messageSenders' is required");
        WebServiceMessageSender[] messageSenders = getMessageSenders();
        for (int i = 0; i < messageSenders.length; i++) {
            if (messageSenders[i].supports(uri)) {
                WebServiceConnection connection = messageSenders[i].createConnection(uri);
                if (logger.isDebugEnabled()) {
                    try {
                        logger.debug("Opening [" + connection + "] to [" + connection.getUri() + "]");
                    }
                    catch (URISyntaxException e) {
                        // ignore
                    }
                }
                return connection;
            }
        }
        throw new IllegalArgumentException("Could not resolve [" + uri + "] to a WebServiceMessageSender");
    }

}
