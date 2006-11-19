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

package org.springframework.ws.transport.http;

import java.net.URL;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.saaj.saaj13.Saaj13SoapMessage;

public class CommonsHttpMessageSenderTest extends AbstractHttpWebServiceMessageSenderTestCase {

    private CommonsHttpMessageSender sender;

    private MessageFactory messageFactory;

    protected void onSetUp() throws Exception {
        sender = new CommonsHttpMessageSender();
        sender.setUrl(new URL(URL));
        messageFactory = MessageFactory.newInstance();
    }

    public void testSend() throws Exception {
        SOAPMessage message = messageFactory.createMessage();
        message.getMimeHeaders().addHeader(HEADER_NAME, HEADER_VALUE);
        Saaj13SoapMessage request = new Saaj13SoapMessage(message);
        MessageContext context = new DefaultMessageContext(request, new SaajSoapMessageFactory(messageFactory));
//        AxiomSoapMessageFactory soapMessageFactory = new AxiomSoapMessageFactory();
//        MessageContext context = new DefaultMessageContext(soapMessageFactory);
        sender.send(context);
        assertTrue("No response", context.hasResponse());

        context.getResponse().writeTo(System.out);
    }
}
