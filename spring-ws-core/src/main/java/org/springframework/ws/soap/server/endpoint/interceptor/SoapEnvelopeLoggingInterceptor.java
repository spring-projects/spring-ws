/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.server.endpoint.interceptor;

import javax.xml.transform.Source;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.AbstractLoggingInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;

/**
 * SOAP-specific <code>EndpointInterceptor</code> that logs the complete request and response envelope of
 * <code>SoapMessage</code> messages. By default, request, response and fault messages are logged, but this behaviour
 * can be changed using the <code>logRequest</code>, <code>logResponse</code>, <code>logFault</code> properties.
 *
 * @author Arjen Poutsma
 * @see #setLogRequest(boolean)
 * @see #setLogResponse(boolean)
 * @see #setLogFault(boolean)
 * @since 1.0.0
 */
public class SoapEnvelopeLoggingInterceptor extends AbstractLoggingInterceptor implements SoapEndpointInterceptor {

    private boolean logFault = true;

    /** Indicates whether a SOAP Fault should be logged. Default is <code>true</code>. */
    public void setLogFault(boolean logFault) {
        this.logFault = logFault;
    }

    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        if (logFault && logger.isDebugEnabled()) {
            logMessageSource("Fault: ", getSource(messageContext.getResponse()));
        }
        return true;
    }

    public boolean understands(SoapHeaderElement header) {
        return false;
    }

    @Override
    protected Source getSource(WebServiceMessage message) {
        if (message instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) message;
            return soapMessage.getEnvelope().getSource();
        }
        else {
            return null;
        }
    }
}
