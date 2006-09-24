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

package org.springframework.ws.soap.context;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.AbstractMessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.TransportRequest;

/**
 * Abstract implementation of the <code>SoapMessageContext</code> interface. Implements base <code>MessageContext</code>
 * methods by delegating to <code>SoapMessageContext</code> functionality.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractSoapMessageContext extends AbstractMessageContext implements SoapMessageContext {

    protected AbstractSoapMessageContext(SoapMessage request, TransportRequest transportRequest) {
        super(request, transportRequest);
    }

    public final SoapMessage getSoapResponse() {
        return (SoapMessage) getResponse();
    }

    public final SoapMessage getSoapRequest() {
        return (SoapMessage) getRequest();
    }

    protected final WebServiceMessage createResponseMessage() {
        return createResponseSoapMessage();
    }

    protected abstract SoapMessage createResponseSoapMessage();
}
