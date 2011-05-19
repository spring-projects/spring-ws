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

package org.springframework.ws.server.endpoint.mapping;

import java.lang.reflect.Method;
import java.util.Collections;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointAdapter;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.PayloadMethodEndpointAdapter;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("applicationContext.xml")
public class PayloadRootAnnotationMethodEndpointMappingTest  {

    @Autowired
    private PayloadRootAnnotationMethodEndpointMapping mapping;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testRegistration() throws NoSuchMethodException {
        MethodEndpoint endpoint = mapping.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request"));
        assertNotNull("MethodEndpoint not registered", endpoint);
        Method doIt = PayloadRootEndpoint.class.getMethod("doIt", Source.class);
        MethodEndpoint expected = new MethodEndpoint("endpoint", applicationContext, doIt);
        assertEquals("Invalid endpoint registered", expected, endpoint);

        assertNull("Invalid endpoint registered",
                mapping.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request2")));
    }

    @Test
    public void testInvoke() throws Exception {

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage request = messageFactory.createMessage();
        request.getSOAPBody().addBodyElement(QName.valueOf("{http://springframework.org/spring-ws}Request"));
        MessageContext messageContext =
                new DefaultMessageContext(new SaajSoapMessage(request), new SaajSoapMessageFactory(messageFactory));
        DefaultMethodEndpointAdapter adapter = new DefaultMethodEndpointAdapter();
        adapter.afterPropertiesSet();

        MessageDispatcher messageDispatcher = new SoapMessageDispatcher();
        messageDispatcher.setApplicationContext(applicationContext);
        messageDispatcher.setEndpointMappings(Collections.<EndpointMapping>singletonList(mapping));
        messageDispatcher.setEndpointAdapters(Collections.<EndpointAdapter>singletonList(adapter));

        messageDispatcher.receive(messageContext);

        PayloadRootEndpoint endpoint = (PayloadRootEndpoint) applicationContext.getBean("endpoint");
        assertTrue("doIt() not invoked on endpoint", endpoint.isDoItInvoked());

        LogAspect aspect = (LogAspect) applicationContext.getBean("logAspect");
        assertTrue("log() not invoked on aspect", aspect.isLogInvoked());
    }

}