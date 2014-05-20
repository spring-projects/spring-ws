/*
 * Copyright 2005-2014 the original author or authors.
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
import java.util.ArrayList;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.WebServiceMessage;
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

@SuppressWarnings("unchecked")
public class WebServiceTemplateTest {

    private WebServiceTemplate template;

    private FaultAwareWebServiceConnection connectionMock;

    private MockWebServiceMessageFactory messageFactory;

    @Before
    public void setUp() throws Exception {
        messageFactory = new MockWebServiceMessageFactory();
        template = new WebServiceTemplate(messageFactory);
        connectionMock = createMock(FaultAwareWebServiceConnection.class);
        final URI expectedUri = new URI("http://www.springframework.org/spring-ws");
        expect(connectionMock.getUri()).andReturn(expectedUri).anyTimes();
        template.setMessageSender(new WebServiceMessageSender() {

            @Override
            public WebServiceConnection createConnection(URI uri) throws IOException {
                return connectionMock;
            }

            @Override
            public boolean supports(URI uri) {
                assertEquals("Invalid uri", expectedUri, uri);
                return true;
            }
        });
        template.setDefaultUri(expectedUri.toString());
    }

    @Test
    public void testMarshalAndSendNoMarshallerSet() throws Exception {
        connectionMock.close();

        replay(connectionMock);

        template.setMarshaller(null);
        try {
            template.marshalSendAndReceive(new Object());
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException ex) {
            // expected behavior
        }

        verify(connectionMock);
    }

    @Test
    public void testMarshalAndSendNoUnmarshallerSet() throws Exception {
        connectionMock.close();

        replay(connectionMock);

        template.setUnmarshaller(null);
        try {
            template.marshalSendAndReceive(new Object());
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException ex) {
            // expected behavior
        }

        verify(connectionMock);
    }

    @Test
    public void testSendAndReceiveMessageResponse() throws Exception {
        WebServiceMessageCallback requestCallback = createMock(WebServiceMessageCallback.class);
        requestCallback.doWithMessage(isA(WebServiceMessage.class));

        WebServiceMessageExtractor extractorMock = createMock(WebServiceMessageExtractor.class);
        Object extracted = new Object();
        expect(extractorMock.extractData(isA(WebServiceMessage.class))).andReturn(extracted);

        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.receive(messageFactory)).andReturn(new MockWebServiceMessage("<response/>"));
        expect(connectionMock.hasFault()).andReturn(false);
        connectionMock.close();

        replay(connectionMock, requestCallback, extractorMock);

        Object result = template.sendAndReceive(requestCallback, extractorMock);
        assertEquals("Invalid response", extracted, result);

        verify(connectionMock, requestCallback, extractorMock);
    }

    @Test
    public void testSendAndReceiveMessageNoResponse() throws Exception {
        WebServiceMessageExtractor extractorMock = createMock(WebServiceMessageExtractor.class);

        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.receive(messageFactory)).andReturn(null);
        connectionMock.close();

        replay(connectionMock, extractorMock);

        Object result = template.sendAndReceive(null, extractorMock);
        assertNull("Invalid response", result);

        verify(connectionMock, extractorMock);
    }

    @Test
    public void testSendAndReceiveMessageFault() throws Exception {
        WebServiceMessageExtractor extractorMock = createMock(WebServiceMessageExtractor.class);

        FaultMessageResolver faultMessageResolverMock = createMock(FaultMessageResolver.class);
        template.setFaultMessageResolver(faultMessageResolverMock);
        faultMessageResolverMock.resolveFault(isA(WebServiceMessage.class));

        MockWebServiceMessage response = new MockWebServiceMessage("<response/>");
        response.setFault(true);

        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.hasFault()).andReturn(true);
        expect(connectionMock.receive(messageFactory)).andReturn(response);
        connectionMock.close();

        replay(connectionMock, extractorMock, faultMessageResolverMock);

        Object result = template.sendAndReceive(null, extractorMock);
        assertNull("Invalid response", result);

        verify(connectionMock, extractorMock, faultMessageResolverMock);
    }

    @Test
    public void testSendAndReceiveConnectionError() throws Exception {
        WebServiceMessageExtractor extractorMock = createMock(WebServiceMessageExtractor.class);

        template.setFaultMessageResolver(null);

        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(true);
        expect(connectionMock.hasFault()).andReturn(false);
        String errorMessage = "errorMessage";
        expect(connectionMock.getErrorMessage()).andReturn(errorMessage);
        connectionMock.close();

        replay(connectionMock, extractorMock);

        try {
            template.sendAndReceive(null, extractorMock);
            fail("Expected WebServiceTransportException");
        }
        catch (WebServiceTransportException ex) {
            //expected
            assertEquals("Invalid exception message", errorMessage, ex.getMessage());
        }

        verify(connectionMock, extractorMock);
    }

    @Test
    public void testSendAndReceiveSourceResponse() throws Exception {
        SourceExtractor extractorMock = createMock(SourceExtractor.class);
        Object extracted = new Object();
        expect(extractorMock.extractData(isA(Source.class))).andReturn(extracted);

        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.receive(messageFactory)).andReturn(new MockWebServiceMessage("<response/>"));
        expect(connectionMock.hasFault()).andReturn(false);
        connectionMock.close();

        replay(connectionMock, extractorMock);

        Object result = template.sendSourceAndReceive(new StringSource("<request />"), extractorMock);
        assertEquals("Invalid response", extracted, result);

        verify(connectionMock, extractorMock);
    }

    @Test
    public void testSendAndReceiveSourceNoResponse() throws Exception {
        SourceExtractor extractorMock = createMock(SourceExtractor.class);

        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.receive(messageFactory)).andReturn(null);
        connectionMock.close();

        replay(connectionMock, extractorMock);

        Object result = template.sendSourceAndReceive(new StringSource("<request />"), extractorMock);
        assertNull("Invalid response", result);

        verify(connectionMock, extractorMock);
    }

    @Test
    public void testSendAndReceiveResultResponse() throws Exception {
        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.receive(messageFactory)).andReturn(new MockWebServiceMessage("<response/>"));
        expect(connectionMock.hasFault()).andReturn(false);
        connectionMock.close();

        replay(connectionMock);

        StringResult result = new StringResult();
        boolean b = template.sendSourceAndReceiveToResult(new StringSource("<request />"), result);
        assertTrue("Invalid result", b);

        verify(connectionMock);
    }

    @Test
    public void testSendAndReceiveResultNoResponse() throws Exception {
        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.receive(messageFactory)).andReturn(null);
        connectionMock.close();

        replay(connectionMock);

        StringResult result = new StringResult();
        boolean b = template.sendSourceAndReceiveToResult(new StringSource("<request />"), result);
        assertFalse("Invalid result", b);

        verify(connectionMock);
    }

    @Test
    public void testSendAndReceiveResultNoResponsePayload() throws Exception {
        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        WebServiceMessage response = createMock(WebServiceMessage.class);
        expect(connectionMock.receive(messageFactory)).andReturn(response);
        expect(connectionMock.hasFault()).andReturn(false);
        expect(response.getPayloadSource()).andReturn(null);
        connectionMock.close();

        replay(connectionMock, response);

        StringResult result = new StringResult();
        boolean b = template.sendSourceAndReceiveToResult(new StringSource("<request />"), result);
        assertTrue("Invalid result", b);

        verify(connectionMock, response);
    }


    @Test
    public void testSendAndReceiveMarshalResponse() throws Exception {
        Marshaller marshallerMock = createMock(Marshaller.class);
        template.setMarshaller(marshallerMock);
        marshallerMock.marshal(isA(Object.class), isA(Result.class));

        Unmarshaller unmarshallerMock = createMock(Unmarshaller.class);
        template.setUnmarshaller(unmarshallerMock);
        Object unmarshalled = new Object();
        expect(unmarshallerMock.unmarshal(isA(Source.class))).andReturn(unmarshalled);

        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.receive(messageFactory)).andReturn(new MockWebServiceMessage("<response/>"));
        expect(connectionMock.hasFault()).andReturn(false);
        connectionMock.close();

        replay(connectionMock, marshallerMock, unmarshallerMock);

        Object result = template.marshalSendAndReceive(new Object());
        assertEquals("Invalid result", unmarshalled, result);

        verify(connectionMock, marshallerMock, unmarshallerMock);
    }

    @Test
    public void testSendAndReceiveMarshalNoResponse() throws Exception {
        Marshaller marshallerMock = createMock(Marshaller.class);
        template.setMarshaller(marshallerMock);
        marshallerMock.marshal(isA(Object.class), isA(Result.class));

        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.receive(messageFactory)).andReturn(null);
        connectionMock.close();

        replay(connectionMock, marshallerMock);

        Object result = template.marshalSendAndReceive(new Object());
        assertNull("Invalid result", result);

        verify(connectionMock, marshallerMock);
    }

    @Test
    public void testSendAndReceiveCustomUri() throws Exception {
        final URI customUri = new URI("http://www.springframework.org/spring-ws/custom");
        template.setMessageSender(new WebServiceMessageSender() {

            @Override
            public WebServiceConnection createConnection(URI uri) throws IOException {
                return connectionMock;
            }

            @Override
            public boolean supports(URI uri) {
                assertEquals("Invalid uri", customUri, uri);
                return true;
            }
        });
        WebServiceMessageCallback requestCallback = createMock(WebServiceMessageCallback.class);
        requestCallback.doWithMessage(isA(WebServiceMessage.class));

        WebServiceMessageExtractor extractorMock = createMock(WebServiceMessageExtractor.class);
        Object extracted = new Object();
        expect(extractorMock.extractData(isA(WebServiceMessage.class))).andReturn(extracted);

        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.receive(messageFactory)).andReturn(new MockWebServiceMessage("<response/>"));
        expect(connectionMock.hasFault()).andReturn(false);
        connectionMock.close();

        replay(connectionMock, requestCallback, extractorMock);

        Object result = template.sendAndReceive(customUri.toString(), requestCallback, extractorMock);
        assertEquals("Invalid response", extracted, result);

        verify(connectionMock, requestCallback, extractorMock);
    }

    @Test
    public void testInterceptors() throws Exception {
        ClientInterceptor interceptorMock1 = createStrictMock("interceptor1", ClientInterceptor.class);
        ClientInterceptor interceptorMock2 = createStrictMock("interceptor2", ClientInterceptor.class);
        template.getInterceptors().add(interceptorMock1);
        template.getInterceptors().add(interceptorMock2);
        expect(interceptorMock1.handleRequest(isA(MessageContext.class))).andReturn(true);
        expect(interceptorMock2.handleRequest(isA(MessageContext.class))).andReturn(true);
        expect(interceptorMock2.handleResponse(isA(MessageContext.class))).andReturn(true);
        expect(interceptorMock1.handleResponse(isA(MessageContext.class))).andReturn(true);
        interceptorMock2.afterCompletion(isA(MessageContext.class), (Exception)isNull());
        interceptorMock1.afterCompletion(isA(MessageContext.class), (Exception)isNull());

        WebServiceMessageCallback requestCallback = createMock(WebServiceMessageCallback.class);
        requestCallback.doWithMessage(isA(WebServiceMessage.class));

        WebServiceMessageExtractor extractorMock = createMock(WebServiceMessageExtractor.class);
        Object extracted = new Object();
        expect(extractorMock.extractData(isA(WebServiceMessage.class))).andReturn(extracted);

        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.receive(messageFactory)).andReturn(new MockWebServiceMessage("<response/>"));
        expect(connectionMock.hasFault()).andReturn(false);
        connectionMock.close();

        replay(connectionMock, interceptorMock1, interceptorMock2, requestCallback, extractorMock);

        Object result = template.sendAndReceive(requestCallback, extractorMock);
        assertEquals("Invalid response", extracted, result);

        verify(connectionMock, interceptorMock1, interceptorMock2, requestCallback, extractorMock);
    }

    @Test
    public void testInterceptorsIntercepted() throws Exception {
        MessageContext messageContext = new DefaultMessageContext(messageFactory);

        ClientInterceptor interceptorMock1 = createStrictMock("interceptor1", ClientInterceptor.class);
        ClientInterceptor interceptorMock2 = createStrictMock("interceptor2", ClientInterceptor.class);
        template.getInterceptors().add(interceptorMock1);
        template.getInterceptors().add(interceptorMock2);
        expect(interceptorMock1.handleRequest(isA(MessageContext.class))).andReturn(false);
        expect(interceptorMock1.handleResponse(isA(MessageContext.class))).andReturn(true);
        interceptorMock1.afterCompletion(isA(MessageContext.class), (Exception)isNull());

        WebServiceMessageCallback requestCallback = createMock(WebServiceMessageCallback.class);
        requestCallback.doWithMessage(messageContext.getRequest());

        WebServiceMessageExtractor extractorMock = createMock(WebServiceMessageExtractor.class);
        Object extracted = new Object();
        expect(extractorMock.extractData(isA(WebServiceMessage.class))).andReturn(extracted);

        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.receive(messageFactory)).andReturn(new MockWebServiceMessage("<response/>"));
        expect(connectionMock.hasFault()).andReturn(false);

        replay(connectionMock, interceptorMock1, interceptorMock2, requestCallback, extractorMock);

        Object result = template.doSendAndReceive(messageContext, connectionMock, requestCallback, extractorMock);
        assertEquals("Invalid response", extracted, result);

        verify(connectionMock, interceptorMock1, interceptorMock2, requestCallback, extractorMock);
    }

    @Test
    public void testInterceptorsInterceptedCreateResponse() throws Exception {
        MessageContext messageContext = new DefaultMessageContext(messageFactory);
        // force creation of response
        messageContext.getResponse();

        ClientInterceptor interceptorMock1 = createStrictMock("interceptor1", ClientInterceptor.class);
        ClientInterceptor interceptorMock2 = createStrictMock("interceptor2", ClientInterceptor.class);
        template.getInterceptors().add(interceptorMock1);
        template.getInterceptors().add(interceptorMock2);
        expect(interceptorMock1.handleRequest(isA(MessageContext.class))).andReturn(false);
        expect(interceptorMock1.handleResponse(isA(MessageContext.class))).andReturn(true);
        interceptorMock1.afterCompletion(isA(MessageContext.class), (Exception)isNull());

        WebServiceMessageCallback requestCallback = createMock(WebServiceMessageCallback.class);
        requestCallback.doWithMessage(messageContext.getRequest());

        WebServiceMessageExtractor extractorMock = createMock(WebServiceMessageExtractor.class);
        Object extracted = new Object();
        expect(extractorMock.extractData(messageContext.getResponse())).andReturn(extracted);

        expect(connectionMock.hasFault()).andReturn(false);

        replay(connectionMock, interceptorMock1, interceptorMock2, requestCallback, extractorMock);

        Object result = template.doSendAndReceive(messageContext, connectionMock, requestCallback, extractorMock);
        assertEquals("Invalid response", extracted, result);

        verify(connectionMock, interceptorMock1, interceptorMock2, requestCallback, extractorMock);
    }

    @Test
    public void testDestinationResolver() throws Exception {
        DestinationProvider providerMock = createMock(DestinationProvider.class);
        template.setDestinationProvider(providerMock);
        final URI providerUri = new URI("http://www.springframework.org/spring-ws/destinationProvider");
        expect(providerMock.getDestination()).andReturn(providerUri);

        template.setMessageSender(new WebServiceMessageSender() {

            @Override
            public WebServiceConnection createConnection(URI uri) throws IOException {
                return connectionMock;
            }

            @Override
            public boolean supports(URI uri) {
                assertEquals("Invalid uri", providerUri, uri);
                return true;
            }
        });

        WebServiceMessageExtractor extractorMock = createMock(WebServiceMessageExtractor.class);

        reset(connectionMock);
        expect(connectionMock.getUri()).andReturn(providerUri);
        connectionMock.send(isA(WebServiceMessage.class));
        expect(connectionMock.hasError()).andReturn(false);
        expect(connectionMock.receive(messageFactory)).andReturn(null);
        connectionMock.close();

        replay(connectionMock, extractorMock, providerMock);

        Object result = template.sendAndReceive(null, extractorMock);
        assertNull("Invalid response", result);

        verify(connectionMock, extractorMock, providerMock);
    }


}
