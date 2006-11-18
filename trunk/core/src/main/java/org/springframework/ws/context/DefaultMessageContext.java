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
 * Simple implementation of <code>MessageContext</code>.
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
        Assert.notNull(request, "No request given");
        Assert.notNull(messageFactory, "messageFactory must not be null");
        this.request = request;
        this.messageFactory = messageFactory;
    }

    private Map getProperties() {
        if (properties == null) {
            properties = new HashMap();
        }
        return properties;
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

    public void readResponse(InputStream inputStream) throws IOException {
        if (response != null) {
            throw new IllegalStateException("Response message already created");
        }
        else {
            response = messageFactory.createWebServiceMessage(inputStream);
        }
    }

    /**
     * Check if this message context contains a property with the given name.
     *
     * @param name the name of the property to look fo
     * @return <code>true</code> if the <code>MessageContext</code> contains the property; <code>false</code> otherwise
     */
    public boolean containsProperty(String name) {
        return getProperties().containsKey(name);
    }

    /**
     * Gets the value of a specific property from the <code>MessageContext</code>.
     *
     * @param name name of the property whose value is to be retrieved
     * @return value of the property
     */
    public Object getProperty(String name) {
        return getProperties().get(name);
    }

    /**
     * Return the names of all properties in this <code>MessageContext</code>.
     *
     * @return the names of all properties in this context, or an empty array if none defined
     */
    public String[] getPropertyNames() {
        return (String[]) getProperties().keySet().toArray(new String[getProperties().size()]);
    }

    /**
     * Indicates whether this context has a resonse.
     *
     * @return <code>true</code> if this context has a response; <code>false</code> otherwise
     */
    public boolean hasResponse() {
        return response != null;
    }

    /**
     * Removes a property from the <code>MessageContext</code>.
     *
     * @param name name of the property to be removed
     */
    public void removeProperty(String name) {
        getProperties().remove(name);
    }

    /**
     * Sets the name and value of a property associated with the <code>MessageContext</code>. If the
     * <code>MessageContext</code> contains a value of the same property, the old value is replaced.
     *
     * @param name  name of the property associated with the value
     * @param value value of the property
     */
    public void setProperty(String name, Object value) {
        getProperties().put(name, value);
    }
}
