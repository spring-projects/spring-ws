/*
 * Copyright 2007 the original author or authors.
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

import org.springframework.ws.FaultAwareWebServiceMessage;

/**
 * Thrown by <code>SimpleFaultMessageResolver</code> when the response message has a fault.
 *
 * @author Arjen Poutsma
 * @since 1.0
 */
public class WebServiceFaultException extends WebServiceClientException {

    private final FaultAwareWebServiceMessage faultMessage;

    /** Create a new instance of the <code>WebServiceFaultException</code> class. */
    public WebServiceFaultException(String msg) {
        super(msg);
        faultMessage = null;
    }

    /**
     * Create a new instance of the <code>WebServiceFaultException</code> class.
     *
     * @param faultMessage the fault message
     */
    public WebServiceFaultException(FaultAwareWebServiceMessage faultMessage) {
        super(faultMessage.getFaultReason());
        this.faultMessage = faultMessage;
    }

    /** Returns the fault message. */
    public FaultAwareWebServiceMessage getWebServiceMessage() {
        return faultMessage;
    }
}
