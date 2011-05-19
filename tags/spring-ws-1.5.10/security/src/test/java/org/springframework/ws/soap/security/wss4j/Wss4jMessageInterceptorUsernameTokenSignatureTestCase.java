/*
 * Copyright 2008 the original author or authors.
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

import org.w3c.dom.Document;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;

public abstract class Wss4jMessageInterceptorUsernameTokenSignatureTestCase extends Wss4jTestCase {

    public void testAddUsernameTokenSignature() throws Exception {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
        interceptor.setSecurementActions("UsernameTokenSignature");
        interceptor.setSecurementUsername("Bert");
        interceptor.setSecurementPassword("Ernie");
        interceptor.afterPropertiesSet();
        SoapMessage message = loadSoap11Message("empty-soap.xml");
        MessageContext context = getSoap11MessageContext(message);
        interceptor.secureMessage(message, context);

        Document doc = getDocument(message);
        assertXpathEvaluatesTo("Invalid Username", "Bert",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", doc);
        assertXpathExists("Invalid Password",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest']/text()",
                doc);
    }
}
