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

package org.springframework.ws.client.core;

import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.WebServiceAccessor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * <strong>The central class for client-side Web services.</strong> It provides a message-driven approach to sending and
 * receiving {@link org.springframework.ws.WebServiceMessage} instances.
 * <p/>
 * Code using this class need only implement callback interfaces, provide {@link Source} objects to read data from, or
 * use the pluggable {@link Marshaller} support.
 * <p/>
 * This template uses a {@link SimpleFaultResolver} to handle responses that contain faults.
 *
 * @author Arjen Poutsma
 */
public class WebServiceTemplate extends WebServiceAccessor implements WebServiceOperations {

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    private FaultResolver faultResolver = new SimpleFaultResolver();

    /**
     * Creates a new <code>WebServiceTemplate</code>.
     * <p/>
     * <b>Note</b> that the message factory and message sender properties have to be set before this template can be
     * used.
     *
     * @see #setMessageFactory(org.springframework.ws.WebServiceMessageFactory)
     * @see #setMessageSender(org.springframework.ws.transport.WebServiceMessageSender)
     */
    public WebServiceTemplate() {
    }

    /**
     * Creates a new <code>WebServiceTemplate</code> based on the given message factory and message sender.
     *
     * @param messageFactory the message factory to use
     * @param messageSender  the message sender to use
     */
    public WebServiceTemplate(WebServiceMessageFactory messageFactory, WebServiceMessageSender messageSender) {
        setMessageFactory(messageFactory);
        setMessageSender(messageSender);
    }

    /**
     * Returns the marshaller for this template.
     */
    public Marshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Sets the marshaller for this template.
     */
    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Returns the unmarshaller for this template.
     */
    public Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    /**
     * Sets the unmarshaller for this template.
     */
    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    /**
     * Returns the fault resolver for this template.
     */
    public FaultResolver getFaultResolver() {
        return faultResolver;
    }

    /**
     * Sets the fault resolver for this template.
     */
    public void setFaultResolver(FaultResolver faultResolver) {
        Assert.notNull(faultResolver, "faultResolver must not be null");
        this.faultResolver = faultResolver;
    }

    public Object marshalSendAndReceive(final Object requestPayload) throws IOException {
        return marshalSendAndReceive(requestPayload, null);
    }

    public Object marshalSendAndReceive(final Object requestPayload, final WebServiceMessageCallback requestCallback)
            throws IOException {
        checkMarshallerAndUnmarshaller();
        WebServiceMessage response = sendAndReceive(new WebServiceMessageCallback() {

            public void doInMessage(WebServiceMessage message) throws IOException {
                getMarshaller().marshal(requestPayload, message.getPayloadResult());
                if (requestCallback != null) {
                    requestCallback.doInMessage(message);
                }
            }
        });
        if (response != null) {
            return getUnmarshaller().unmarshal(response.getPayloadSource());
        }
        else {
            return null;
        }
    }

    public boolean sendAndReceive(Source requestPayload, Result responseResult) throws IOException {
        return sendAndReceive(requestPayload, null, responseResult);
    }

    public boolean sendAndReceive(Source requestPayload,
                                  WebServiceMessageCallback requestCallback,
                                  Result responseResult) throws IOException {
        Source responsePayload = sendAndReceive(requestPayload, requestCallback);
        if (responsePayload != null) {
            try {
                Transformer transformer = createTransformer();
                transformer.transform(responsePayload, responseResult);
                return true;
            }
            catch (TransformerException e) {
                throw new WebServiceClientException("Could not transform payload of responsePayload message");
            }
        }
        else {
            return false;
        }
    }

    public Source sendAndReceive(final Source requestPayload) throws IOException {
        return sendAndReceive(requestPayload, (WebServiceMessageCallback) null);
    }

    public Source sendAndReceive(final Source requestPayload, final WebServiceMessageCallback requestCallback)
            throws IOException {
        WebServiceMessage response = sendAndReceive(new WebServiceMessageCallback() {
            public void doInMessage(WebServiceMessage message) throws IOException {
                try {
                    Transformer transformer = createTransformer();
                    transformer.transform(requestPayload, message.getPayloadResult());
                    if (requestCallback != null) {
                        requestCallback.doInMessage(message);
                    }
                }
                catch (TransformerException ex) {
                    throw new WebServiceClientException("Could not transform payload to request message", ex);
                }
            }
        });
        return response != null ? response.getPayloadSource() : null;
    }

    public WebServiceMessage sendAndReceive(WebServiceMessageCallback requestCallback) throws IOException {
        MessageContext messageContext = createMessageContext();
        if (requestCallback != null) {
            requestCallback.doInMessage(messageContext.getRequest());
        }
        getMessageSender().sendAndReceive(messageContext);
        if (!messageContext.hasResponse()) {
            return null;
        }
        WebServiceMessage response = messageContext.getResponse();
        if (response.hasFault()) {
            getFaultResolver().resolveFault(response);
            return null;
        }
        return response;
    }

    private void checkMarshallerAndUnmarshaller() throws IllegalStateException {
        if (getMarshaller() == null) {
            throw new IllegalStateException("No marshaller registered. Check configuration of WebServiceTemplate.");
        }
        if (getUnmarshaller() == null) {
            throw new IllegalStateException("No unmarshaller registered. Check configuration of WebServiceTemplate.");
        }
    }

}
