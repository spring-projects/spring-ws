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

/**
 * Abstract implementation of the <code>SoapMessageContext</code> interface. Implements base <code>MessageContext</code>
 * methods by delegating to <code>SoapMessageContext</code> functionality.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractSoapMessageContext extends AbstractMessageContext implements SoapMessageContext {

    /**
     * Delegates to <code>getSoapRequest</code>.
     *
     * @see #getSoapRequest()
     */
    public WebServiceMessage getRequest() {
        return getSoapRequest();
    }

    /**
     * Delegates to <code>createSoapResponse</code>.
     *
     * @see #createSoapResponse()
     */
    public WebServiceMessage createResponse() {
        return createSoapResponse();
    }

    public final SoapMessage createSoapResponse() {
        if (getSoapResponse() != null) {
            throw new IllegalStateException("Response already created");
        }
        return createSoapResponseInternal();
    }

    /**
     * Protected template method that should create a <code>SoapMessage</code> response.
     */
    protected abstract SoapMessage createSoapResponseInternal();

    /**
     * Delegates to <code>getSoapResponse</code>.
     *
     * @see #getSoapResponse()
     */
    public WebServiceMessage getResponse() {
        return getSoapResponse();
    }
}
