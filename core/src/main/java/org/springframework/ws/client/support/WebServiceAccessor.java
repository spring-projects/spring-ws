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

    public void setMessageSender(WebServiceMessageSender messageSender) {
        Assert.notNull(messageSender, "'messageSender' must not be null");
        this.messageSenders = new WebServiceMessageSender[]{messageSender};
    }

    public void setMessageSenders(WebServiceMessageSender[] messageSenders) {
        Assert.notEmpty(messageSenders, "'messageSenders' must not be empty");
        this.messageSenders = messageSenders;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(getMessageFactory(), "Property 'messageFactory' is required");
        Assert.notEmpty(getMessageSenders(), "Property 'messageSenders' is required");
    }

    /**
     * Creates a connection to the given URI, or throws an exception when it cannot be resolved.
     *
     * @param uri the URI to open a connection to
     * @return the created connection
     * @throws IllegalArgumentException when the uri cannot be resolved
     * @throws IOException              when an I/O error occurs
     */
    protected WebServiceConnection createConnection(String uri) throws IOException {
        Assert.notEmpty(getMessageSenders(), "Property 'messageSenders' is required");
        WebServiceMessageSender messageSender = null;
        WebServiceMessageSender[] messageSenders = getMessageSenders();
        for (int i = 0; i < messageSenders.length; i++) {
            if (messageSenders[i].supports(uri)) {
                messageSender = messageSenders[i];
                break;
            }
        }
        Assert.notNull(messageSender, "Could not resolve [" + uri + "] to a WebServiceMessageSender");
        return messageSender.createConnection(uri);
    }

}
