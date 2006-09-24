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

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.TransportRequest;

/**
 * Abstract implementation of the <code>MessageContext</code> interface. Contains functionality to set and remove
 * properties.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractMessageContext implements MessageContext {

    private WebServiceMessage request;

    private WebServiceMessage response;

    private final TransportRequest transportRequest;

    /**
     * Keys are <code>Strings</code>, values are <code>Objects</code>. Lazily initalized by
     * <code>getProperties()</code>.
     */
    private Map properties;

    /**
     * Construct a new instance of the <code>AbstractMessageContext</code> with the given request message and
     * transportRequest.
     */
    protected AbstractMessageContext(WebServiceMessage request, TransportRequest transportRequest) {
        Assert.notNull(request, "No request given");
        Assert.notNull(transportRequest, "No transport request given");
        this.request = request;
        this.transportRequest = transportRequest;
    }

    public final WebServiceMessage getRequest() {
        return request;
    }

    /**
     * Protected method that sets the request message directly.
     */
    protected final void setRequest(WebServiceMessage request) {
        Assert.notNull(request);
        this.request = request;
    }

    public final WebServiceMessage getResponse() {
        if (response == null) {
            response = createResponseMessage();
        }
        return response;
    }

    public final boolean hasResponse() {
        return response != null;
    }

    public final TransportRequest getTransportRequest() {
        return transportRequest;
    }

    /**
     * Protected method that sets the response message directly.
     */
    protected final void setResponse(WebServiceMessage response) {
        Assert.notNull(response);
        this.response = response;
    }

    private Map getProperties() {
        if (properties == null) {
            properties = new HashMap();
        }
        return properties;
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

    /**
     * Abstract template method that creates a new <code>WebServiceMessage</code>.
     */
    protected abstract WebServiceMessage createResponseMessage();
}
