/*
 * Copyright 2005 the original author or authors.
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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLTestCase;
import org.easymock.MockControl;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mock.MockMessageContext;
import org.springframework.ws.mock.MockWebServiceMessage;

public class PayloadEndpointAdapterTest extends XMLTestCase {

    private PayloadEndpointAdapter adapter;

    private PayloadEndpoint endpointMock;

    private MockControl endpointControl;

    protected void setUp() throws Exception {
        adapter = new PayloadEndpointAdapter();
        endpointControl = MockControl.createControl(PayloadEndpoint.class);
        endpointMock = (PayloadEndpoint) endpointControl.getMock();
    }

    public void testSupports() throws Exception {
        assertTrue("PayloadEndpointAdapter does not support PayloadEndpoint", adapter.supports(endpointMock));
    }

    public void testInvoke() throws Exception {
        MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        PayloadEndpoint endpoint = new PayloadEndpoint() {
            public Source invoke(Source request) throws Exception {
                StringWriter writer = new StringWriter();
                transformer.transform(request, new StreamResult(writer));
                assertXMLEqual("Invalid request", "<request/>", writer.toString());
                return new StreamSource(new StringReader("<response/>"));
            }
        };
        endpoint.invoke(request.getPayloadSource());
        MessageContext messageContext = new MockMessageContext(request);
        adapter.invoke(messageContext, endpoint);
        MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();
        assertNotNull("No response created", response);
        assertXMLEqual("Invalid payload", "<response/>", response.getPayloadAsString());
    }

    public void testInvokeNoResponse() throws Exception {
        MessageContext messageContext = new MockMessageContext();
        endpointMock.invoke(messageContext.getRequest().getPayloadSource());
        endpointControl.setMatcher(MockControl.ALWAYS_MATCHER);
        endpointControl.setReturnValue(null);
        endpointControl.replay();
        adapter.invoke(messageContext, endpointMock);
        endpointControl.verify();
        assertNull("SOAPMessage has body elements", messageContext.getResponse());
    }

}
