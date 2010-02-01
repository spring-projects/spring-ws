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

package org.springframework.ws.soap.security.wss4j;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Properties;
import javax.xml.namespace.QName;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.WsSecurityValidationException;
import org.springframework.ws.soap.security.wss4j.callback.SimplePasswordValidationCallbackHandler;

public abstract class Wss4jMessageInterceptorHeaderTestCase extends Wss4jTestCase {

    private Wss4jSecurityInterceptor interceptor;

    @Override
    protected void onSetup() throws Exception {
        Properties users = new Properties();
        users.setProperty("Bert", "Ernie");
        interceptor = new Wss4jSecurityInterceptor();
        interceptor.setValidateRequest(true);
        interceptor.setSecureResponse(true);
        interceptor.setValidationActions("UsernameToken");
        SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
        callbackHandler.setUsers(users);
        interceptor.setValidationCallbackHandler(callbackHandler);
        interceptor.afterPropertiesSet();
    }

    public void testValidateUsernameTokenPlainText() throws Exception {
        SoapMessage message = loadSoap11Message("usernameTokenPlainTextWithHeaders-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
        interceptor.validateMessage(message, messageContext);
        Object result = getMessage(message);
        assertNotNull("No result returned", result);

        for (Iterator i = message.getEnvelope().getHeader().examineAllHeaderElements(); i.hasNext();) {
            SoapHeaderElement element = (SoapHeaderElement) i.next();
            QName name = element.getName();
            if (name.getNamespaceURI()
                    .equals("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd")) {
                fail("Security Header not removed");
            }

        }

        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
                getDocument(message));
        assertXpathExists("header1 not found", "/SOAP-ENV:Envelope/SOAP-ENV:Header/header1", getDocument(message));
        assertXpathExists("header2 not found", "/SOAP-ENV:Envelope/SOAP-ENV:Header/header2", getDocument(message));

    }

    public void testEmptySecurityHeader() throws Exception {
        SoapMessage message = loadSoap11Message("emptySecurityHeader-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
        try {
            interceptor.validateMessage(message, messageContext);
            fail("validation must fail for an empty security header.");
        }
        catch (WsSecurityValidationException e) {
            // expected
        }
    }

    public void testPreserveCustomHeaders() throws Exception {
        interceptor.setSecurementActions("UsernameToken");
        interceptor.setSecurementUsername("Bert");
        interceptor.setSecurementPassword("Ernie");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        SoapMessage message = loadSoap11Message("customHeader-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
        message.writeTo(os);
        String document = os.toString("UTF-8");
        assertXpathEvaluatesTo("Header 1 does not exist", "test1", "/SOAP-ENV:Envelope/SOAP-ENV:Header/test:header1",
                document);
        assertXpathNotExists("Header 2 exist", "/SOAP-ENV:Envelope/SOAP-ENV:Header/test:header2", document);

        interceptor.secureMessage(message, messageContext);

        SoapHeaderElement element = message.getSoapHeader().addHeaderElement(new QName("http://test", "header2"));
        element.setText("test2");

        os = new ByteArrayOutputStream();
        message.writeTo(os);
        document = os.toString("UTF-8");
        assertXpathEvaluatesTo("Header 1 does not exist", "test1", "/SOAP-ENV:Envelope/SOAP-ENV:Header/test:header1",
                document);
        assertXpathEvaluatesTo("Header 2 does not exist", "test2", "/SOAP-ENV:Envelope/SOAP-ENV:Header/test:header2",
                document);

        os = new ByteArrayOutputStream();
        message.writeTo(os);
        document = os.toString("UTF-8");
        assertXpathEvaluatesTo("Header 1 does not exist", "test1", "/SOAP-ENV:Envelope/SOAP-ENV:Header/test:header1",
                document);
        assertXpathEvaluatesTo("Header 2 does not exist", "test2", "/SOAP-ENV:Envelope/SOAP-ENV:Header/test:header2",
                document);
    }
}
