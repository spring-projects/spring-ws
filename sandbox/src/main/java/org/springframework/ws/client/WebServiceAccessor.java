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

package org.springframework.ws.client;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.endpoint.TransformerObjectSupport;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * Base class for <code>WebServiceTemplate</code> and other WS-accessing helpers.
 *
 * @author Arjen Poutsma
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

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(getMessageFactory(), "messageFactory is required");
        Assert.notNull(getMessageSender(), "messageSender is required");
    }
}
