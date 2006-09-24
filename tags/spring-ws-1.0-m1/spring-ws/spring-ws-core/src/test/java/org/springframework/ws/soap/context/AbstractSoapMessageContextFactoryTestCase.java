/*
 * Copyright (c) 2006, Your Corporation. All Rights Reserved.
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

package org.springframework.ws.soap.context;

import junit.framework.TestCase;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.soap.Attachment;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;

public abstract class AbstractSoapMessageContextFactoryTestCase extends TestCase {

    private MessageContextFactory contextFactory;

    protected void setUp() throws Exception {
        contextFactory = createSoapMessageContextFactory();
        if (contextFactory instanceof InitializingBean) {
            ((InitializingBean) contextFactory).afterPropertiesSet();
        }
    }

    protected abstract MessageContextFactory createSoapMessageContextFactory();

    public void testCreateMessageFromHttpServletRequest11() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("POST");
        byte[] contents = FileCopyUtils
                .copyToByteArray(AbstractSoapMessageContextFactoryTestCase.class.getResourceAsStream("soap11.xml"));
        servletRequest.setContent(contents);
        servletRequest.setContentType("text/xml");
        servletRequest.setCharacterEncoding("UTF-8");
        servletRequest.addHeader("SOAPAction", "\"Some-URI\"");

        MessageContext messageContext = contextFactory.createContext(servletRequest);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertNotNull("Request null", requestMessage);
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, requestMessage.getVersion());
        assertEquals("Invalid soap action", "\"Some-URI\"", requestMessage.getSoapAction());
    }

    public void testCreateMessageFromHttpServletRequest11WithAttachment() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("POST");
        byte[] contents = FileCopyUtils
                .copyToByteArray(
                        AbstractSoapMessageContextFactoryTestCase.class.getResourceAsStream("soap11-attachment.bin"));
        servletRequest.setContent(contents);
        servletRequest.setContentType(
                "multipart/related; type=\"text/xml\"; boundary=\"----=_Part_0_11416420.1149699787554\"");

        MessageContext messageContext = contextFactory.createContext(servletRequest);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, requestMessage.getVersion());
        Attachment attachment = requestMessage.getAttachment("interface21");
        assertNotNull("No attachment read", attachment);
    }

    public void testCreateMessageFromHttpServletRequest12() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("POST");
        byte[] contents = FileCopyUtils
                .copyToByteArray(AbstractSoapMessageContextFactoryTestCase.class.getResourceAsStream("soap12.xml"));
        servletRequest.setContent(contents);
        servletRequest.setContentType("application/soap+xml");
        servletRequest.setCharacterEncoding("UTF-8");
        servletRequest.addHeader("SOAPAction", "\"Some-URI\"");

        MessageContext messageContext = contextFactory.createContext(servletRequest);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertNotNull("Request null", requestMessage);
        assertEquals("Invalid soap version", SoapVersion.SOAP_12, requestMessage.getVersion());
        assertEquals("Invalid soap action", "\"Some-URI\"", requestMessage.getSoapAction());
    }

    public void testCreateMessageFromHttpServletRequest12WithAttachment() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("POST");
        byte[] contents = FileCopyUtils
                .copyToByteArray(
                        AbstractSoapMessageContextFactoryTestCase.class.getResourceAsStream("soap12-attachment.bin"));
        servletRequest.setContent(contents);
        servletRequest.setContentType(
                "multipart/related; type=\"application/soap+xml\"; boundary=\"----=_Part_0_11416420.1149699787554\"");

        MessageContext messageContext = contextFactory.createContext(servletRequest);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertEquals("Invalid soap version", SoapVersion.SOAP_12, requestMessage.getVersion());
        Attachment attachment = requestMessage.getAttachment("interface21");
        assertNotNull("No attachment read", attachment);
    }
}
