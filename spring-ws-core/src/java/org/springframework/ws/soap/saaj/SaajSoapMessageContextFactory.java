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

package org.springframework.ws.soap.saaj;

import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.soap.SoapMessageCreationException;

/**
 * SAAJ-specific implementation of the <code>MessageContextFactory</code> interface. Creates a
 * <code>SaajSoapMessageContext</code>.
 *
 * @author Arjen Poutsma
 * @see SaajSoapMessageContext
 */
public class SaajSoapMessageContextFactory implements MessageContextFactory, InitializingBean {

    private MessageFactory messageFactory;

    public SaajSoapMessageContextFactory() {
        try {
            this.messageFactory = MessageFactory.newInstance();
        }
        catch (SOAPException ex) {
            throw new SoapMessageCreationException("Could not create MessageFactory: " + ex.getMessage(), ex);
        }
    }

    public SaajSoapMessageContextFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    public MessageContext createContext(HttpServletRequest request) throws IOException {
        MimeHeaders mimeHeaders = new MimeHeaders();
        for (Enumeration enumeration = request.getHeaderNames(); enumeration.hasMoreElements();) {
            String headerName = (String) enumeration.nextElement();
            String headerValue = request.getHeader(headerName);
            StringTokenizer tokenizer = new StringTokenizer(headerValue, ",");
            while (tokenizer.hasMoreTokens()) {
                mimeHeaders.addHeader(headerName, tokenizer.nextToken().trim());
            }
        }
        mimeHeaders.addHeader("Content-Type", request.getContentType());
        mimeHeaders.addHeader("Content-Length", Integer.toString(request.getContentLength()));
        try {
            SOAPMessage requestMessage = messageFactory.createMessage(mimeHeaders, request.getInputStream());
            return new SaajSoapMessageContext(requestMessage, messageFactory);
        }
        catch (SOAPException ex) {
            throw new SoapMessageCreationException(
                    "Could not create message from HttpServletRequest: " + ex.getMessage(), ex);
        }
    }

    public void setMessageFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(messageFactory, "messageFactory is required");
    }
}
