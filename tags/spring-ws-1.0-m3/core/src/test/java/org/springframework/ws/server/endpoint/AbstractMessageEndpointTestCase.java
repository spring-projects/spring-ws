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

package org.springframework.ws.server.endpoint;

import javax.xml.transform.Source;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.transform.StringSource;

public abstract class AbstractMessageEndpointTestCase extends AbstractEndpointTestCase {

    private MessageEndpoint endpoint;

    protected final void setUp() throws Exception {
        endpoint = createResponseEndpoint();
    }

    public void testNoResponse() throws Exception {
        endpoint = createNoResponseEndpoint();
        StringSource requestSource = new StringSource(REQUEST);

        MessageContext context =
                new DefaultMessageContext(new MockWebServiceMessage(requestSource), new MockWebServiceMessageFactory());
        endpoint.invoke(context);
        assertFalse("Response message created", context.hasResponse());
    }

    protected final void testSource(Source requestSource) throws Exception {
        MessageContext context =
                new DefaultMessageContext(new MockWebServiceMessage(requestSource), new MockWebServiceMessageFactory());
        endpoint.invoke(context);
        assertTrue("No response message created", context.hasResponse());
        assertXMLEqual(RESPONSE, ((MockWebServiceMessage) context.getResponse()).getPayloadAsString());
    }

    protected abstract MessageEndpoint createNoResponseEndpoint();

    protected abstract MessageEndpoint createResponseEndpoint();

}
