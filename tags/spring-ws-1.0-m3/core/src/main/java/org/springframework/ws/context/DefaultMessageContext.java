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

package org.springframework.ws.context;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Default implementation of <code>MessageContext</code>.
 *
 * @author Arjen Poutsma
 */
public class DefaultMessageContext implements MessageContext {

    private final WebServiceMessageFactory messageFactory;

    /**
     * Keys are <code>Strings</code>, values are <code>Objects</code>. Lazily initalized by
     * <code>getProperties()</code>.
     */
    private Map properties;

    private WebServiceMessage request;

    private WebServiceMessage response;

    /**
     * Construct a new, empty instance of the <code>DefaultMessageContext</code> with the given message factory.
     */
    public DefaultMessageContext(WebServiceMessageFactory messageFactory) {
        this(messageFactory.createWebServiceMessage(), messageFactory);
    }

    /**
     * Construct a new instance of the <code>DefaultMessageContext</code> with the given request message and message
     * factory.
     */
    public DefaultMessageContext(WebServiceMessage request, WebServiceMessageFactory messageFactory) {
        Assert.notNull(request, "request must not be null");
        Assert.notNull(messageFactory, "messageFactory must not be null");
        this.request = request;
        this.messageFactory = messageFactory;
    }

    public WebServiceMessage getRequest() {
        return request;
    }

    public WebServiceMessage getResponse() {
        if (response == null) {
            response = messageFactory.createWebServiceMessage();
        }
        return response;
    }

    public boolean hasResponse() {
        return response != null;
    }

    public void readResponse(InputStream inputStream) throws IOException {
        if (response != null) {
            throw new IllegalStateException("Response message already created");
        }
        else {
            response = messageFactory.createWebServiceMessage(inputStream);
        }
    }

    public boolean containsProperty(String name) {
        return getProperties().containsKey(name);
    }

    public Object getProperty(String name) {
        return getProperties().get(name);
    }

    public String[] getPropertyNames() {
        return (String[]) getProperties().keySet().toArray(new String[getProperties().size()]);
    }

    public void removeProperty(String name) {
        getProperties().remove(name);
    }

    public void setProperty(String name, Object value) {
        getProperties().put(name, value);
    }

    private Map getProperties() {
        if (properties == null) {
            properties = new HashMap();
        }
        return properties;
    }
}
