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

package org.springframework.ws.test.server;

import java.io.IOException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Factory methods for {@link RequestCreator} classes. Typically used to provide input for {@link
 * MockWebServiceClient#sendMessage(RequestCreator)}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class RequestCreators {

    private RequestCreators() {
    }

    /**
     * Create a request with the given {@link Source} XML as payload.
     *
     * @param payload the request payload
     * @return the request creator
     */
    public static RequestCreator withPayload(Source payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadRequestCreator(payload);
    }

    /**
     * Create a request with the given {@link Resource} XML as payload.
     *
     * @param payload the request payload
     * @return the request creator
     */
    public static RequestCreator withPayload(Resource payload) throws IOException {
        Assert.notNull(payload, "'payload' must not be null");
        return withPayload(new ResourceSource(payload));
    }

    /**
     * Abstract base class for the {@link RequestCreator} interface.
     * <p/>
     * Creates a response using the given {@link org.springframework.ws.WebServiceMessageFactory}, and passes it on to
     * {@link #doWithRequest(org.springframework.ws.WebServiceMessage)}.
     */
    private static abstract class AbstractRequestCreator implements RequestCreator {

        public final WebServiceMessage createRequest(WebServiceMessageFactory messageFactory) throws IOException {
            WebServiceMessage request = messageFactory.createWebServiceMessage();
            doWithRequest(request);
            return request;
        }

        protected abstract void doWithRequest(WebServiceMessage request) throws IOException;

    }


    /**
     * Implementation of {@link RequestCreator} that creates a request based on a {@link javax.xml.transform.Source}.
     */
    private static class PayloadRequestCreator extends AbstractRequestCreator {

        private final Source payload;

        private TransformerHelper transformerHelper = new TransformerHelper();

        PayloadRequestCreator(Source payload) {
            this.payload = payload;
        }

        @Override
        protected void doWithRequest(WebServiceMessage request) throws IOException {
            try {
                transformerHelper.transform(payload, request.getPayloadResult());
            }
            catch (TransformerException ex) {
                throw new AssertionError("Could not transform request payload to message: " + ex.getMessage());
            }
        }
    }

}
