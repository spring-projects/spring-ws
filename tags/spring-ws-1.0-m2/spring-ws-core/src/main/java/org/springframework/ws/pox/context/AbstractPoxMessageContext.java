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

package org.springframework.ws.pox.context;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.AbstractMessageContext;
import org.springframework.ws.pox.PoxMessage;
import org.springframework.ws.transport.TransportRequest;

/**
 * Abstract implementation of the <code>PoxMessageContext</code> interface. Implements base <code>MessageContext</code>
 * methods by delegating to <code>PoxMessageContext</code> functionality.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractPoxMessageContext extends AbstractMessageContext implements PoxMessageContext {

    protected AbstractPoxMessageContext(PoxMessage request, TransportRequest transportRequest) {
        super(request, transportRequest);
    }

    public final PoxMessage getPoxResponse() {
        return (PoxMessage) getResponse();
    }

    public final PoxMessage getPoxRequest() {
        return (PoxMessage) getRequest();
    }

    protected final WebServiceMessage createResponseMessage() {
        return createResponsePoxMessage();
    }

    protected abstract PoxMessage createResponsePoxMessage();
}
