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

package org.springframework.ws.samples.airline.client.saaj;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSProcessor;
import com.sun.xml.wss.XWSSProcessorFactory;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;

/**
 * Simple client that calls the WS-Security <code>GetFrequentFlyerMileage</code> operation using SAAJ and XWSS.
 *
 * @author Arjen Poutsma
 */
public class GetFrequentFlyerMileage {

    public static final String NAMESPACE_URI = "http://www.springframework.org/spring-ws/samples/airline/schemas/messages";

    public static final String PREFIX = "airline";

    private SOAPConnectionFactory connectionFactory;

    private MessageFactory messageFactory;

    private URL url;

    private XWSSProcessorFactory processorFactory;

    public GetFrequentFlyerMileage(String url) throws SOAPException, MalformedURLException, XWSSecurityException {
        connectionFactory = SOAPConnectionFactory.newInstance();
        messageFactory = MessageFactory.newInstance();
        processorFactory = XWSSProcessorFactory.newInstance();
        this.url = new URL(url);
    }

    private SOAPMessage createGetMileageRequest() throws SOAPException {
        SOAPMessage message = messageFactory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        Name getFlightsRequestName = envelope.createName("GetFrequentFlyerMileageRequest", GetFrequentFlyerMileage.PREFIX,
                GetFrequentFlyerMileage.NAMESPACE_URI);
        message.getSOAPBody().addBodyElement(getFlightsRequestName);
        return message;
    }

    private SOAPMessage secureMessage(SOAPMessage message, final String username, final String password)
            throws IOException, XWSSecurityException {
        CallbackHandler callbackHandler = new CallbackHandler() {
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof UsernameCallback) {
                        UsernameCallback callback = (UsernameCallback) callbacks[i];
                        callback.setUsername(username);
                    }
                    else if (callbacks[i] instanceof PasswordCallback) {
                        PasswordCallback callback = (PasswordCallback) callbacks[i];
                        callback.setPassword(password);
                    }
                    else {
                        throw new UnsupportedCallbackException(callbacks[i]);
                    }
                }
            }
        };
        InputStream policyStream = null;
        XWSSProcessor processor = null;
        try {
            policyStream = getClass().getResourceAsStream("securityPolicy.xml");
            processor = processorFactory.createProcessorForSecurityConfiguration(policyStream, callbackHandler);
        }
        finally {
            if (policyStream != null) {
                policyStream.close();
            }
        }
        ProcessingContext context = processor.createProcessingContext(message);
        return processor.secureOutboundMessage(context);
    }

    public void getMileage(String username, String password) throws SOAPException, IOException, XWSSecurityException {
        SOAPMessage request = createGetMileageRequest();
        request = secureMessage(request, username, password);
        SOAPConnection connection = connectionFactory.createConnection();
        SOAPMessage response = connection.call(request, url);
        if (!response.getSOAPBody().hasFault()) {
            SOAPBodyElement mileage = (SOAPBodyElement) response.getSOAPBody().getChildElements().next();
            System.out.println("'" + username + "' has " + mileage.getValue() + " frequent flyer miles");
        }
        else {
            SOAPFault fault = response.getSOAPBody().getFault();
            System.err.println("Received SOAP Fault");
            System.err.println("SOAP Fault Code:   " + fault.getFaultCode());
            System.err.println("SOAP Fault String: " + fault.getFaultString());
        }
    }
}
