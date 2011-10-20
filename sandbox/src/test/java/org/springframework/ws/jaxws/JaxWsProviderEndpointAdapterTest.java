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

package org.springframework.ws.jaxws;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import junit.framework.TestCase;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

public class JaxWsProviderEndpointAdapterTest extends TestCase {

    private JaxWsProviderEndpointAdapter adapter;

    private MessageContext messageContext;

    @Override
    protected void setUp() throws Exception {
        adapter = new JaxWsProviderEndpointAdapter();
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage request = messageFactory.createMessage();
        request.getSOAPBody().addBodyElement(new QName("http://springframework.org/spring-ws", "content"));
        messageContext =
                new DefaultMessageContext(new SaajSoapMessage(request), new SaajSoapMessageFactory(messageFactory));
    }

    public void testSupports() throws Exception {
        MyMessageProvider messageProvider = new MyMessageProvider();
        assertTrue("Does not support message provider", adapter.supports(messageProvider));
        MySourceProvider sourceProvider = new MySourceProvider();
        assertTrue("Does not support source provider", adapter.supports(sourceProvider));
        MyDefaultProvider defaultProvider = new MyDefaultProvider();
        assertTrue("Does not support source provider", adapter.supports(defaultProvider));
    }

    public void testInvokeMessageProvider() throws Exception {
        MyMessageProvider provider = new MyMessageProvider();
        adapter.invoke(messageContext, provider);
        assertTrue("No response", messageContext.hasResponse());
        SaajSoapMessage request = (SaajSoapMessage) messageContext.getRequest();
        SaajSoapMessage response = (SaajSoapMessage) messageContext.getResponse();
        assertEquals("Invalid response", request.getSaajMessage(), response.getSaajMessage());
    }

    public void testInvokeSourceProvider() throws Exception {
        MySourceProvider provider = new MySourceProvider();
        adapter.invoke(messageContext, provider);
        assertTrue("No response", messageContext.hasResponse());
    }

    public void testInvokeDefaultProvider() throws Exception {
        MyDefaultProvider provider = new MyDefaultProvider();
        adapter.invoke(messageContext, provider);
        assertTrue("No response", messageContext.hasResponse());
    }

    @WebServiceProvider
    @ServiceMode(Service.Mode.MESSAGE)
    private static class MyMessageProvider implements Provider<SOAPMessage> {

        public SOAPMessage invoke(SOAPMessage request) {
            return request;
        }
    }

    @WebServiceProvider
    @ServiceMode(value = Service.Mode.PAYLOAD)
    private static class MySourceProvider implements Provider<Source> {

        public Source invoke(Source request) {
            return request;
        }
    }

    @WebServiceProvider
    private static class MyDefaultProvider implements Provider<Source> {

        public Source invoke(Source request) {
            return request;
        }
    }


}
