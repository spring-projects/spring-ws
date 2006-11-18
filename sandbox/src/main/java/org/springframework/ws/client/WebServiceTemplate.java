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
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceException;
import org.springframework.ws.WebServiceMessage;

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

    public Object marshalAndSend(final Object payload) {
        checkMarshallerAndUnmarshaller();
        return send(new WebServiceMessageCallback() {
            public void doInWebServiceMessage(WebServiceMessage message) throws IOException {
                getMarshaller().marshal(payload, message.getPayloadResult());
            }
        }, new WebServiceMessageExtractor() {
            public Object extractData(WebServiceMessage message) throws IOException {
                return getUnmarshaller().unmarshal(message.getPayloadSource());
            }
        });
    }

    public Source send(final Source payload) {
        return (Source) send(new WebServiceMessageCallback() {
            public void doInWebServiceMessage(WebServiceMessage message) throws Exception {
                Transformer transformer = createTransformer();
                transformer.transform(payload, message.getPayloadResult());
            }
        }, new WebServiceMessageExtractor() {
            public Object extractData(WebServiceMessage message) throws WebServiceException {
                return message.getPayloadSource();
            }
        });
    }

    private void checkMarshallerAndUnmarshaller() throws IllegalStateException {
        if (getMarshaller() == null) {
            throw new IllegalStateException("No marshaller registered. Check configuration of WebServiceTemplate.");
        }
        if (getUnmarshaller() == null) {
            throw new IllegalStateException("No unmarshaller registered. Check configuration of WebServiceTemplate.");
        }
    }

    public WebServiceMessage send(WebServiceMessageCallback callback) {
        return (WebServiceMessage) send(callback, new WebServiceMessageExtractor() {
            public Object extractData(WebServiceMessage message) throws Exception {
                return message;
            }
        });
    }

    public Object send(WebServiceMessageCallback callback, WebServiceMessageExtractor extractor) {
        Assert.notNull(callback, "callback must not be null");
        Assert.notNull(extractor, "extractor must not be null");
        WebServiceMessage request = getMessageFactory().createWebServiceMessage();
        try {
            callback.doInWebServiceMessage(request);
            WebServiceMessage response = null; //getMessageSender().send(request);
            if (response == null) {
                return null;
            }
            else {
                return extractor.extractData(response);
            }
        }
        catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }
}
