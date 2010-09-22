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

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.WebServiceMessageReceiver;

/**
 * @author Arjen Poutsma
 */
class WebServiceTestContext {

    private final WebServiceMessageReceiver messageReceiver;

    private final WebServiceMessageFactory messageFactory;

    private final List<ResponseMatcher> responseMatchers = new ArrayList<ResponseMatcher>();

    public WebServiceTestContext(WebServiceMessageReceiver messageReceiver, WebServiceMessageFactory messageFactory) {
        this.messageReceiver = messageReceiver;
        this.messageFactory = messageFactory;
    }

    WebServiceMessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    WebServiceMessageFactory getMessageFactory() {
        return messageFactory;
    }

    void addResponseMatcher(ResponseMatcher responseMatcher) {
        Assert.notNull(responseMatcher, "'responseMatcher' must not be null");
        responseMatchers.add(responseMatcher);
    }
}
