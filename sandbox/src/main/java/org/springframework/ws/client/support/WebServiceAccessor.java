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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.endpoint.TransformerObjectSupport;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * Base class for <code>WebServiceTemplate</code> and other WS-accessing helpers. Defines common properties like the
 * {@link org.springframework.ws.WebServiceMessageFactory} and {@link org.springframework.ws.transport.WebServiceMessageSender}.
 * <p/>
 * Not intended to be used directly. See {@link org.springframework.ws.client.WebServiceTemplate}.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.client.WebServiceTemplate
 */
public abstract class WebServiceAccessor extends TransformerObjectSupport implements InitializingBean {

    private WebServiceMessageFactory messageFactory;

    private WebServiceMessageSender messageSender;

    /**
     * Returns the message factory used for creating messages.
     */
    public WebServiceMessageFactory getMessageFactory() {
        return messageFactory;
    }

    /**
     * Sets the message factory used for creating messages.
     */
    public void setMessageFactory(WebServiceMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * Returns the message sender.
     */
    public WebServiceMessageSender getMessageSender() {
        return messageSender;
    }

    /**
     * Sets the message sender.
     */
    public void setMessageSender(WebServiceMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    /**
     * Returns a <code>MessageContext</code> with an empty message, and the defined
     * <code>WebServiceMessageFactory</code>.
     *
     * @return the created message context
     */
    protected MessageContext createMessageContext() {
        return new DefaultMessageContext(getMessageFactory());
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(getMessageFactory(), "messageFactory is required");
        Assert.notNull(getMessageSender(), "messageSender is required");
    }
}
