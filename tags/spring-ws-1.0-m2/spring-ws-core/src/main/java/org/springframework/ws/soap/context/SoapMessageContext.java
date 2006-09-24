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

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;

/**
 * SOAP-specific extension of the <code>MessageContext</code> interface. Contains methods to obtain
 * <code>SoapMessage</code>s instead of <code>WebServiceMessage</code>s.
 *
 * @author Arjen Poutsma
 */
public interface SoapMessageContext extends MessageContext {

    /**
     * Returns the request SOAP message.
     *
     * @return the request message
     */
    SoapMessage getSoapRequest();

    /**
     * Returns the response message, if created. Returns <code>null</code> if no response message was created so far.
     *
     * @return the response message, or <code>null</code> if none was created
     * @see #hasResponse()
     */
    SoapMessage getSoapResponse();


}
