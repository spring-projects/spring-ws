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

package org.springframework.ws.endpoint;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.springframework.ws.EndpointInterceptor;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;

/**
 * Simple <code>EndpointInterceptor</code> that logs the payload of request and response messages.  By default, both
 * request and response messages are logged, but this behaviour can be changed using the <code>logRequest</code> and
 * <code>logResponse</code> properties.
 *
 * @author Arjen Poutsma
 * @see #setLogRequest(boolean)
 * @see #setLogResponse(boolean)
 */
public class PayloadLoggingInterceptor extends TransformerObjectSupport implements EndpointInterceptor {

    private boolean logRequest = true;

    private boolean logResponse = true;

    /**
     * Indicates whether the request should be logged. Default is <code>true</code>.
     */
    public void setLogRequest(boolean logRequest) {
        this.logRequest = logRequest;
    }

    /**
     * Indicates whether the response should be logged. Default is <code>true</code>.
     */
    public void setLogResponse(boolean logResponse) {
        this.logResponse = logResponse;
    }

    /**
     * Logs the request message payload. Logging only ocurs if <code>logRequest</code> is set to <code>true</code>,
     * which is the default.
     *
     * @param messageContext the message context
     * @return <code>true</code>
     * @throws TransformerException when the payload cannot be transformed to a string
     */
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws TransformerException {
        if (logRequest && logger.isDebugEnabled()) {
            logMessagePayload("Request payload: ", messageContext.getRequest());
        }
        return true;
    }

    /**
     * Logs the response message payload. Logging only ocurs if <code>logResponse</code> is set to <code>true</code>,
     * which is the default.
     *
     * @param messageContext the message context
     * @return <code>true</code>
     * @throws TransformerException when the payload cannot be transformed to a string
     */
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        if (logResponse && logger.isDebugEnabled()) {
            logMessagePayload("Response payload: ", messageContext.getResponse());
        }
        return true;
    }

    private void logMessagePayload(String logMessage, WebServiceMessage message) throws TransformerException {
        Source source = message.getPayloadSource();
        if (source != null) {
            Transformer transformer = createNonIndentingTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            logger.debug(logMessage + writer.toString());
        }
    }

    private Transformer createNonIndentingTransformer() throws TransformerConfigurationException {
        Transformer transformer = createTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        return transformer;
    }
}
