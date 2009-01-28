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

package org.springframework.ws.soap.server.endpoint.mapping;

import javax.xml.soap.MessageFactory;

import junit.framework.TestCase;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

public class SoapActionEndpointMappingTest extends TestCase {

    private SoapActionEndpointMapping mapping;

    private MessageContext context;

    protected void setUp() throws Exception {
        mapping = new SoapActionEndpointMapping();
        context = new DefaultMessageContext(new SaajSoapMessageFactory(MessageFactory.newInstance()));
    }

    public void testGetLookupKeyForMessage() throws Exception {
        String soapAction = "http://springframework.org/spring-ws/SoapAction";
        ((SoapMessage) context.getRequest()).setSoapAction(soapAction);
        assertEquals("Invalid lookup key", soapAction, mapping.getLookupKeyForMessage(context));
    }

    public void testGetLookupKeyForMessageQuoted() throws Exception {
        String soapAction = "http://springframework.org/spring-ws/SoapAction";
        ((SoapMessage) context.getRequest()).setSoapAction(soapAction);
        assertEquals("Invalid lookup key", soapAction, mapping.getLookupKeyForMessage(context));
    }

    public void testValidateLookupKey() throws Exception {
        assertTrue("Soapaction not valid", mapping.validateLookupKey("SoapAction"));
    }
}