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

package org.springframework.ws.client;

import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;

/**
 * @author Arjen Poutsma
 */
public class WebServiceTemplate extends WebServiceAccessor implements WebServiceOperations {

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

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

    public Object sendAndReceive(final Object requestPayload) throws IOException {
        checkMarshallerAndUnmarshaller();
        WebServiceMessage response = sendAndReceive(new WebServiceMessageCallback() {

            public void doInMessage(WebServiceMessage message) throws IOException {
                getMarshaller().marshal(requestPayload, message.getPayloadResult());
            }
        });
        if (response != null) {
            return getUnmarshaller().unmarshal(response.getPayloadSource());
        }
        else {
            return null;
        }
    }

    public boolean sendAndReceive(final Source requestPayload, Result result) throws IOException {
        Source responsePayload = sendAndReceive(requestPayload);
        if (responsePayload != null) {
            try {
                Transformer transformer = createTransformer();
                transformer.transform(responsePayload, result);
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
        WebServiceMessage response = sendAndReceive(new WebServiceMessageCallback() {
            public void doInMessage(WebServiceMessage message) {
                try {
                    Transformer transformer = createTransformer();
                    transformer.transform(requestPayload, message.getPayloadResult());
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
        if (messageContext.hasResponse()) {
            return messageContext.getResponse();
        }
        else {
            return null;
        }
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
