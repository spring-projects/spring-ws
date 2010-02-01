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

package org.springframework.ws.client.core;

import java.io.IOException;
import java.net.URI;

import org.custommonkey.xmlunit.XMLTestCase;
import org.easymock.MockControl;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.support.destination.DestinationProvider;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class WebServiceTemplateTest extends XMLTestCase {

    private WebServiceTemplate template;

    private MockControl connectionControl;

    private FaultAwareWebServiceConnection connectionMock;

    private MockWebServiceMessageFactory messageFactory;

    @Override
    protected void setUp() throws Exception {
        template = new WebServiceTemplate();
        messageFactory = new MockWebServiceMessageFactory();
        template.setMessageFactory(messageFactory);
        connectionControl = MockControl.createStrictControl(FaultAwareWebServiceConnection.class);
        connectionMock = (FaultAwareWebServiceConnection) connectionControl.getMock();
        final URI expectedUri = new URI("http://www.springframework.org/spring-ws");
        connectionControl.expectAndDefaultReturn(connectionMock.getUri(), expectedUri);
        template.setMessageSender(new WebServiceMessageSender() {

            public WebServiceConnection createConnection(URI uri) throws IOException {
                return connectionMock;
            }

            public boolean supports(URI uri) {
                assertEquals("Invalid uri", expectedUri, uri);
                return true;
            }
        });
        template.setDefaultUri(expectedUri.toString());
    }

    public void testMarshalAndSendNoMarshallerSet() throws Exception {
        connectionMock.close();
        connectionControl.replay();
        template.setMarshaller(null);
        try {
            template.marshalSendAndReceive(new Object());
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException ex) {
            // expected behavior
        }
        connectionControl.verify();
    }

    public void testMarshalAndSendNoUnmarshallerSet() throws Exception {
        connectionMock.close();
        connectionControl.replay();
        template.setUnmarshaller(null);
        try {
            template.marshalSendAndReceive(new Object());
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException ex) {
            // expected behavior
        }
        connectionControl.verify();
    }

    public void testSendAndReceiveMessageResponse() throws Exception {
        MockControl callbackControl = MockControl.createControl(WebServiceMessageCallback.class);
        WebServiceMessageCallback requestCallback = (WebServiceMessageCallback) callbackControl.getMock();
        requestCallback.doWithMessage(null);
        callbackControl.setMatcher(MockControl.ALWAYS_MATCHER);
        callbackControl.replay();

        MockControl extractorControl = MockControl.createControl(WebServiceMessageExtractor.class);
        WebServiceMessageExtractor extractorMock = (WebServiceMessageExtractor) extractorControl.getMock();
        extractorMock.extractData(null);
        extractorControl.setMatcher(MockControl.ALWAYS_MATCHER);
        Object extracted = new Object();
        extractorControl.setReturnValue(extracted);
        extractorControl.replay();

        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), false);
        connectionControl
                .expectAndReturn(connectionMock.receive(messageFactory), new MockWebServiceMessage("<response/>"));
        connectionControl.expectAndReturn(connectionMock.hasFault(), false);
        connectionMock.close();
        connectionControl.replay();

        Object result = template.sendAndReceive(requestCallback, extractorMock);
        assertEquals("Invalid response", extracted, result);

        callbackControl.verify();
        extractorControl.verify();
        connectionControl.verify();
    }

    public void testSendAndReceiveMessageNoResponse() throws Exception {
        MockControl extractorControl = MockControl.createControl(WebServiceMessageExtractor.class);
        WebServiceMessageExtractor extractorMock = (WebServiceMessageExtractor) extractorControl.getMock();
        extractorControl.replay();

        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), false);
        connectionControl.expectAndReturn(connectionMock.receive(messageFactory), null);
        connectionMock.close();
        connectionControl.replay();

        Object result = template.sendAndReceive(null, extractorMock);
        assertNull("Invalid response", result);
        extractorControl.verify();
        connectionControl.verify();
    }

    public void testSendAndReceiveMessageFault() throws Exception {
        MockControl extractorControl = MockControl.createControl(WebServiceMessageExtractor.class);
        WebServiceMessageExtractor extractorMock = (WebServiceMessageExtractor) extractorControl.getMock();
        extractorControl.replay();

        MockControl faultResolverControl = MockControl.createControl(FaultMessageResolver.class);
        FaultMessageResolver faultMessageResolverMock = (FaultMessageResolver) faultResolverControl.getMock();
        template.setFaultMessageResolver(faultMessageResolverMock);
        faultMessageResolverMock.resolveFault(null);
        faultResolverControl.setMatcher(MockControl.ALWAYS_MATCHER);
        faultResolverControl.replay();

        MockWebServiceMessage response = new MockWebServiceMessage("<response/>");
        response.setFault(true);

        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), true);
        connectionControl.expectAndReturn(connectionMock.hasFault(), true);
        connectionControl
                .expectAndReturn(connectionMock.receive(messageFactory), response);
        connectionControl.expectAndReturn(connectionMock.hasFault(), true);
        connectionMock.close();
        connectionControl.replay();

        Object result = template.sendAndReceive(null, extractorMock);
        assertNull("Invalid response", result);

        extractorControl.verify();
        connectionControl.verify();
        faultResolverControl.verify();
    }

    public void testSendAndReceiveConnectionError() throws Exception {
        MockControl extractorControl = MockControl.createControl(WebServiceMessageExtractor.class);
        WebServiceMessageExtractor extractorMock = (WebServiceMessageExtractor) extractorControl.getMock();
        extractorControl.replay();

        template.setFaultMessageResolver(null);

        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), true);
        connectionControl.expectAndReturn(connectionMock.hasFault(), false);
        String errorMessage = "errorMessage";
        connectionControl.expectAndReturn(connectionMock.getErrorMessage(), errorMessage);
        connectionMock.close();
        connectionControl.replay();

        try {
            template.sendAndReceive(null, extractorMock);
            fail("Expected WebServiceTransportException");
        }
        catch (WebServiceTransportException ex) {
            //expected
            assertEquals("Invalid exception message", errorMessage, ex.getMessage());
        }

        extractorControl.verify();
        connectionControl.verify();
    }

    public void testSendAndReceiveSourceResponse() throws Exception {
        MockControl extractorControl = MockControl.createControl(SourceExtractor.class);
        SourceExtractor extractorMock = (SourceExtractor) extractorControl.getMock();
        extractorMock.extractData(null);
        extractorControl.setMatcher(MockControl.ALWAYS_MATCHER);
        Object extracted = new Object();
        extractorControl.setReturnValue(extracted);
        extractorControl.replay();

        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), false);
        connectionControl
                .expectAndReturn(connectionMock.receive(messageFactory), new MockWebServiceMessage("<response/>"));
        connectionControl.expectAndReturn(connectionMock.hasFault(), false);
        connectionMock.close();
        connectionControl.replay();

        Object result = template.sendSourceAndReceive(new StringSource("<request />"), extractorMock);
        assertEquals("Invalid response", extracted, result);

        extractorControl.verify();
        connectionControl.verify();
    }

    public void testSendAndReceiveSourceNoResponse() throws Exception {
        MockControl extractorControl = MockControl.createControl(SourceExtractor.class);
        SourceExtractor extractorMock = (SourceExtractor) extractorControl.getMock();
        extractorControl.replay();

        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), false);
        connectionControl.expectAndReturn(connectionMock.receive(messageFactory), null);
        connectionMock.close();
        connectionControl.replay();

        Object result = template.sendSourceAndReceive(new StringSource("<request />"), extractorMock);
        assertNull("Invalid response", result);

        extractorControl.verify();
        connectionControl.verify();
    }

    public void testSendAndReceiveResultResponse() throws Exception {
        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), false);
        connectionControl
                .expectAndReturn(connectionMock.receive(messageFactory), new MockWebServiceMessage("<response/>"));
        connectionControl.expectAndReturn(connectionMock.hasFault(), false);
        connectionMock.close();
        connectionControl.replay();

        StringResult result = new StringResult();
        boolean b = template.sendSourceAndReceiveToResult(new StringSource("<request />"), result);
        assertTrue("Invalid result", b);

        connectionControl.verify();
    }

    public void testSendAndReceiveResultNoResponse() throws Exception {
        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), false);
        connectionControl
                .expectAndReturn(connectionMock.receive(messageFactory), null);
        connectionMock.close();
        connectionControl.replay();

        StringResult result = new StringResult();
        boolean b = template.sendSourceAndReceiveToResult(new StringSource("<request />"), result);
        assertFalse("Invalid result", b);

        connectionControl.verify();
    }

    public void testSendAndReceiveMarshalResponse() throws Exception {
        MockControl marshallerControl = MockControl.createControl(Marshaller.class);
        Marshaller marshallerMock = (Marshaller) marshallerControl.getMock();
        template.setMarshaller(marshallerMock);
        marshallerMock.marshal(null, null);
        marshallerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        marshallerControl.replay();

        MockControl unmarshallerControl = MockControl.createControl(Unmarshaller.class);
        Unmarshaller unmarshallerMock = (Unmarshaller) unmarshallerControl.getMock();
        template.setUnmarshaller(unmarshallerMock);
        unmarshallerMock.unmarshal(null);
        unmarshallerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        Object unmarshalled = new Object();
        unmarshallerControl.setReturnValue(unmarshalled);
        unmarshallerControl.replay();

        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), false);
        connectionControl
                .expectAndReturn(connectionMock.receive(messageFactory), new MockWebServiceMessage("<response/>"));
        connectionControl.expectAndReturn(connectionMock.hasFault(), false);
        connectionMock.close();
        connectionControl.replay();

        Object result = template.marshalSendAndReceive(new Object());
        assertEquals("Invalid result", unmarshalled, result);

        connectionControl.verify();
        marshallerControl.verify();
        unmarshallerControl.verify();
    }

    public void testSendAndReceiveMarshalNoResponse() throws Exception {
        MockControl marshallerControl = MockControl.createControl(Marshaller.class);
        Marshaller marshallerMock = (Marshaller) marshallerControl.getMock();
        template.setMarshaller(marshallerMock);
        marshallerMock.marshal(null, null);
        marshallerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        marshallerControl.replay();

        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), false);
        connectionControl.expectAndReturn(connectionMock.receive(messageFactory), null);
        connectionMock.close();
        connectionControl.replay();

        Object result = template.marshalSendAndReceive(new Object());
        assertNull("Invalid result", result);

        connectionControl.verify();
        marshallerControl.verify();
    }

    public void testSendAndReceiveCustomUri() throws Exception {
        final URI customUri = new URI("http://www.springframework.org/spring-ws/custom");
        template.setMessageSender(new WebServiceMessageSender() {

            public WebServiceConnection createConnection(URI uri) throws IOException {
                return connectionMock;
            }

            public boolean supports(URI uri) {
                assertEquals("Invalid uri", customUri, uri);
                return true;
            }
        });
        MockControl callbackControl = MockControl.createControl(WebServiceMessageCallback.class);
        WebServiceMessageCallback requestCallback = (WebServiceMessageCallback) callbackControl.getMock();
        requestCallback.doWithMessage(null);
        callbackControl.setMatcher(MockControl.ALWAYS_MATCHER);
        callbackControl.replay();

        MockControl extractorControl = MockControl.createControl(WebServiceMessageExtractor.class);
        WebServiceMessageExtractor extractorMock = (WebServiceMessageExtractor) extractorControl.getMock();
        extractorMock.extractData(null);
        extractorControl.setMatcher(MockControl.ALWAYS_MATCHER);
        Object extracted = new Object();
        extractorControl.setReturnValue(extracted);
        extractorControl.replay();

        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), false);
        connectionControl
                .expectAndReturn(connectionMock.receive(messageFactory), new MockWebServiceMessage("<response/>"));
        connectionControl.expectAndReturn(connectionMock.hasFault(), false);
        connectionMock.close();
        connectionControl.replay();

        Object result = template.sendAndReceive(customUri.toString(), requestCallback, extractorMock);
        assertEquals("Invalid response", extracted, result);

        callbackControl.verify();
        extractorControl.verify();
        connectionControl.verify();
    }

    public void testInterceptors() throws Exception {
        MockControl interceptorControl = MockControl.createControl(ClientInterceptor.class);
        interceptorControl.setDefaultMatcher(MockControl.ALWAYS_MATCHER);
        ClientInterceptor interceptorMock1 = (ClientInterceptor) interceptorControl.getMock();
        ClientInterceptor interceptorMock2 = (ClientInterceptor) interceptorControl.getMock();
        template.setInterceptors(new ClientInterceptor[]{interceptorMock1, interceptorMock2});
        interceptorControl.expectAndReturn(interceptorMock1.handleRequest(null), true);
        interceptorControl.expectAndReturn(interceptorMock2.handleRequest(null), true);
        interceptorControl.expectAndReturn(interceptorMock2.handleResponse(null), true);
        interceptorControl.expectAndReturn(interceptorMock1.handleResponse(null), true);
        interceptorControl.replay();

        MockControl callbackControl = MockControl.createControl(WebServiceMessageCallback.class);
        WebServiceMessageCallback requestCallback = (WebServiceMessageCallback) callbackControl.getMock();
        requestCallback.doWithMessage(null);
        callbackControl.setMatcher(MockControl.ALWAYS_MATCHER);
        callbackControl.replay();

        MockControl extractorControl = MockControl.createControl(WebServiceMessageExtractor.class);
        WebServiceMessageExtractor extractorMock = (WebServiceMessageExtractor) extractorControl.getMock();
        extractorMock.extractData(null);
        extractorControl.setMatcher(MockControl.ALWAYS_MATCHER);
        Object extracted = new Object();
        extractorControl.setReturnValue(extracted);
        extractorControl.replay();

        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), false);
        connectionControl
                .expectAndReturn(connectionMock.receive(messageFactory), new MockWebServiceMessage("<response/>"));
        connectionControl.expectAndReturn(connectionMock.hasFault(), false);
        connectionMock.close();
        connectionControl.replay();

        Object result = template.sendAndReceive(requestCallback, extractorMock);
        assertEquals("Invalid response", extracted, result);

        callbackControl.verify();
        extractorControl.verify();
        connectionControl.verify();
        interceptorControl.verify();
    }

    public void testInterceptorsIntercepted() throws Exception {
        MessageContext messageContext = new DefaultMessageContext(messageFactory);

        MockControl interceptorControl = MockControl.createControl(ClientInterceptor.class);
        ClientInterceptor interceptorMock1 = (ClientInterceptor) interceptorControl.getMock();
        ClientInterceptor interceptorMock2 = (ClientInterceptor) interceptorControl.getMock();
        template.setInterceptors(new ClientInterceptor[]{interceptorMock1, interceptorMock2});
        interceptorControl.expectAndReturn(interceptorMock1.handleRequest(messageContext), false);
        interceptorControl.expectAndReturn(interceptorMock1.handleResponse(messageContext), true);
        interceptorControl.replay();

        MockControl callbackControl = MockControl.createControl(WebServiceMessageCallback.class);
        WebServiceMessageCallback requestCallback = (WebServiceMessageCallback) callbackControl.getMock();
        requestCallback.doWithMessage(messageContext.getRequest());
        callbackControl.replay();

        MockControl extractorControl = MockControl.createControl(WebServiceMessageExtractor.class);
        WebServiceMessageExtractor extractorMock = (WebServiceMessageExtractor) extractorControl.getMock();
        extractorMock.extractData(null);
        extractorControl.setMatcher(MockControl.ALWAYS_MATCHER);
        Object extracted = new Object();
        extractorControl.setReturnValue(extracted);
        extractorControl.replay();

        connectionMock.send(messageContext.getRequest());
        connectionControl.expectAndReturn(connectionMock.hasError(), false);
        connectionControl
                .expectAndReturn(connectionMock.receive(messageFactory), new MockWebServiceMessage("<response/>"));
        connectionControl.expectAndReturn(connectionMock.hasFault(), false);
        connectionControl.replay();

        Object result = template.doSendAndReceive(messageContext, connectionMock, requestCallback, extractorMock);
        assertEquals("Invalid response", extracted, result);

        callbackControl.verify();
        extractorControl.verify();
        connectionControl.verify();
        interceptorControl.verify();
    }

    public void testInterceptorsInterceptedCreateResponse() throws Exception {
        MessageContext messageContext = new DefaultMessageContext(messageFactory);
        // force creation of response
        messageContext.getResponse();

        MockControl interceptorControl = MockControl.createControl(ClientInterceptor.class);
        ClientInterceptor interceptorMock1 = (ClientInterceptor) interceptorControl.getMock();
        ClientInterceptor interceptorMock2 = (ClientInterceptor) interceptorControl.getMock();
        template.setInterceptors(new ClientInterceptor[]{interceptorMock1, interceptorMock2});
        interceptorControl.expectAndReturn(interceptorMock1.handleRequest(messageContext), false);
        interceptorControl.expectAndReturn(interceptorMock1.handleResponse(messageContext), true);
        interceptorControl.replay();

        MockControl callbackControl = MockControl.createControl(WebServiceMessageCallback.class);
        WebServiceMessageCallback requestCallback = (WebServiceMessageCallback) callbackControl.getMock();
        requestCallback.doWithMessage(messageContext.getRequest());
        callbackControl.replay();

        MockControl extractorControl = MockControl.createControl(WebServiceMessageExtractor.class);
        WebServiceMessageExtractor extractorMock = (WebServiceMessageExtractor) extractorControl.getMock();
        Object extracted = new Object();
        extractorControl.expectAndReturn(extractorMock.extractData(messageContext.getResponse()), extracted);
        extractorControl.replay();

        connectionControl.expectAndReturn(connectionMock.hasFault(), false);
        connectionControl.replay();

        Object result = template.doSendAndReceive(messageContext, connectionMock, requestCallback, extractorMock);
        assertEquals("Invalid response", extracted, result);

        callbackControl.verify();
        extractorControl.verify();
        connectionControl.verify();
        interceptorControl.verify();
    }

    public void testDestinationResolver() throws Exception {
        MockControl providerControl = MockControl.createControl(DestinationProvider.class);
        DestinationProvider providerMock = (DestinationProvider) providerControl.getMock();
        final URI providerUri = new URI("http://www.springframework.org/spring-ws/destinationProvider");
        providerControl.expectAndReturn(providerMock.getDestination(), providerUri);
        template.setDestinationProvider(providerMock);
        providerControl.replay();

        template.setMessageSender(new WebServiceMessageSender() {

            public WebServiceConnection createConnection(URI uri) throws IOException {
                return connectionMock;
            }

            public boolean supports(URI uri) {
                assertEquals("Invalid uri", providerUri, uri);
                return true;
            }
        });

        MockControl extractorControl = MockControl.createControl(WebServiceMessageExtractor.class);
        WebServiceMessageExtractor extractorMock = (WebServiceMessageExtractor) extractorControl.getMock();
        extractorControl.replay();

        connectionControl.reset();
        connectionControl.expectAndDefaultReturn(connectionMock.getUri(), providerUri);
        connectionMock.send(null);
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionControl.expectAndReturn(connectionMock.hasError(), false);
        connectionControl.expectAndReturn(connectionMock.receive(messageFactory), null);
        connectionMock.close();
        connectionControl.replay();

        Object result = template.sendAndReceive(null, extractorMock);
        assertNull("Invalid response", result);
        extractorControl.verify();
        connectionControl.verify();
        providerControl.verify();
    }


}
