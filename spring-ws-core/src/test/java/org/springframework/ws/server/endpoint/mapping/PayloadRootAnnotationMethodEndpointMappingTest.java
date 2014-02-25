/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.PayloadRoots;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;

import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("payloadRootAnnotationMethodEndpointMapping.xml")
public class PayloadRootAnnotationMethodEndpointMappingTest  {

    @Autowired
    private PayloadRootAnnotationMethodEndpointMapping mapping;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void registrationSingle() throws NoSuchMethodException {
        MethodEndpoint endpoint = mapping.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request"));
        assertNotNull("MethodEndpoint not registered", endpoint);
        Method doIt = MyEndpoint.class.getMethod("doIt", Source.class);
        MethodEndpoint expected = new MethodEndpoint("endpoint", applicationContext, doIt);
        assertEquals("Invalid endpoint registered", expected, endpoint);
    }

    @Test
    public void registrationMultiple() throws NoSuchMethodException {
	    Method doItMultiple = MyEndpoint.class.getMethod("doItMultiple", Source.class);
	    MethodEndpoint expected = new MethodEndpoint("endpoint", applicationContext, doItMultiple);

        MethodEndpoint endpoint = mapping.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request1"));
        assertNotNull("MethodEndpoint not registered", endpoint);
        assertEquals("Invalid endpoint registered", expected, endpoint);

	    endpoint = mapping.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request2"));
        assertNotNull("MethodEndpoint not registered", endpoint);
        assertEquals("Invalid endpoint registered", expected, endpoint);
    }

	@Test
	public void registrationInvalid() {
        assertNull("Invalid endpoint registered",
                mapping.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Invalid")));
    }

    @Test
    public void invoke() throws Exception {

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

        MyEndpoint endpoint = applicationContext.getBean("endpoint", MyEndpoint.class);
        assertTrue("doIt() not invoked on endpoint", endpoint.isDoItInvoked());

        LogAspect aspect = (LogAspect) applicationContext.getBean("logAspect");
        assertTrue("log() not invoked on aspect", aspect.isLogInvoked());
    }

    @Endpoint
    public static class MyEndpoint {

        private static final org.apache.commons.logging.Log logger = LogFactory.getLog(MyEndpoint.class);

        private boolean doItInvoked = false;

        public boolean isDoItInvoked() {
            return doItInvoked;
        }

        @PayloadRoot(localPart = "Request", namespace = "http://springframework.org/spring-ws")
        @Log
        public void doIt(@RequestPayload Source payload) {
            doItInvoked = true;
            logger.info("In doIt()");
        }

	    @PayloadRoots({
			    @PayloadRoot(localPart = "Request1", namespace = "http://springframework.org/spring-ws"),
			    @PayloadRoot(localPart = "Request2", namespace = "http://springframework.org/spring-ws")
	    })
	     public void doItMultiple(@RequestPayload Source payload) {
	         doItInvoked = true;
	         logger.info("In doIt()");
	     }

    }

    static class OtherBean {

        @PayloadRoot(localPart = "Invalid", namespace = "http://springframework.org/spring-ws")
        public void doIt() {

        }

    }
}