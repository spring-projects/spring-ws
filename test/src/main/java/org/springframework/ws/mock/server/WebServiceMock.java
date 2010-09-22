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

package org.springframework.ws.mock.server;

import java.io.IOException;
import javax.xml.transform.Source;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mock.support.PayloadDiffMatcher;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.xml.transform.ResourceSource;

import static org.springframework.ws.mock.support.Assert.fail;

/**
 * @author Arjen Poutsma
 */
public abstract class WebServiceMock {

    @SuppressWarnings("unchecked")
    public static ResponseActions receiveMessage(RequestCreator requestCreator) {
        final WebServiceTestContext testContext = WebServiceTestContextHolder.get();
        Assert.state(testContext != null, "No test context found. Did you annotate your test class with " +
                "@TestExecutionListeners(WebServiceTestExecutionListener.class) ?");

        try {
            WebServiceMessageFactory messageFactory = testContext.getMessageFactory();
            WebServiceMessage request = requestCreator.createRequest(messageFactory);

            MessageContext messageContext = new DefaultMessageContext(request, messageFactory);

            WebServiceMessageReceiver messageReceiver = testContext.getMessageReceiver();
            messageReceiver.receive(messageContext);

            return new ResponseActions() {
                public ResponseActions andExpect(ResponseMatcher responseMatcher) {
                    testContext.addResponseMatcher(responseMatcher);
                    return this;
                }
            };
        }
        catch (Exception ex) {
            fail(ex.getMessage());
        }
        return null;
    }

    // RequestCreators

    public static RequestCreator withPayload(Source payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadRequestCreator(payload);
    }

    public static RequestCreator withPayload(Resource payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadRequestCreator(createResourceSource(payload));
    }

    // ResponseMatchers

    public static ResponseMatcher payload(Source payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return createPayloadDiffMatcher(payload);
    }

    public static ResponseMatcher payload(Resource payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return createPayloadDiffMatcher(createResourceSource(payload));
    }

    private static ResponseMatcher createPayloadDiffMatcher(Source payload) {
        final PayloadDiffMatcher matcher = new PayloadDiffMatcher(payload);
        return new ResponseMatcher() {
            public void match(WebServiceMessage response) throws IOException, AssertionError {
                matcher.match(response);
            }
        };
    }

    /**
     * Expects any request.
     *
     * @return the request matcher
     */
    public static ResponseMatcher anything() {
        return new ResponseMatcher() {
            public void match(WebServiceMessage response) throws IOException, AssertionError {
            }
        };
    }

    private static ResourceSource createResourceSource(Resource resource) {
        try {
            return new ResourceSource(resource);
        }
        catch (IOException ex) {
            throw new IllegalArgumentException(resource + " could not be opened", ex);
        }
    }


}
