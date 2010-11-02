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

package org.springframework.ws.test.server;

import java.io.IOException;
import java.util.Map;
import javax.xml.transform.Source;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.ws.test.support.PayloadDiffMatcher;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.xml.transform.ResourceSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.springframework.ws.test.support.Assert.fail;

/**
 * @author Arjen Poutsma
 */
public class MockWebServiceClient {

    private static final Log logger = LogFactory.getLog(MockWebServiceClient.class);

    private final WebServiceMessageReceiver messageReceiver;

    private final WebServiceMessageFactory messageFactory;

    private MockWebServiceClient(WebServiceMessageReceiver messageReceiver, WebServiceMessageFactory messageFactory) {
        Assert.notNull(messageReceiver, "'messageReceiver' must not be null");
        Assert.notNull(messageFactory, "'messageFactory' must not be null");
        this.messageReceiver = messageReceiver;
        this.messageFactory = messageFactory;
    }

    // Constructors

    public static MockWebServiceClient createClient(WebServiceMessageReceiver messageReceiver,
                                                    WebServiceMessageFactory messageFactory) {
        return new MockWebServiceClient(messageReceiver, messageFactory);
    }

    // Factory methods

    public static MockWebServiceClient createClient(ApplicationContext applicationContext) {
        WebServiceMessageReceiver messageReceiver = getMessageReceiver(applicationContext);
        WebServiceMessageFactory messageFactory = getMessageFactory(applicationContext);
        return new MockWebServiceClient(messageReceiver, messageFactory);
    }

    private static WebServiceMessageReceiver getMessageReceiver(ApplicationContext applicationContext) {
        WebServiceMessageReceiver messageReceiver = getStrategy(applicationContext, WebServiceMessageReceiver.class);
        if (messageReceiver == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No WebServiceMessageReceiver found, using default");
            }
            SoapMessageDispatcher soapMessageDispatcher = new SoapMessageDispatcher();
            soapMessageDispatcher.setApplicationContext(applicationContext);
            messageReceiver = soapMessageDispatcher;
        }
        return messageReceiver;
    }

    private static WebServiceMessageFactory getMessageFactory(ApplicationContext applicationContext) {
        WebServiceMessageFactory messageFactory = getStrategy(applicationContext, WebServiceMessageFactory.class);
        if (messageFactory == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No WebServiceMessageFactory found, using default");
            }
            SaajSoapMessageFactory saajSoapMessageFactory = new SaajSoapMessageFactory();
            saajSoapMessageFactory.afterPropertiesSet();
            messageFactory = saajSoapMessageFactory;
        }
        return messageFactory;
    }

    private static <T> T getStrategy(ApplicationContext applicationContext, Class<T> strategyInterface) {
        Map<String, T> map = applicationContext.getBeansOfType(strategyInterface);
        if (map.isEmpty()) {
            return null;
        }
        else if (map.size() == 1) {
            Map.Entry<String, T> entry = map.entrySet().iterator().next();
            if (logger.isDebugEnabled()) {
                logger.debug("Using " + ClassUtils.getShortName(strategyInterface) + " [" + entry.getKey() + "]");
            }
            return entry.getValue();
        }
        else {
            throw new BeanInitializationException(
                    "Could not find exactly 1 " + ClassUtils.getShortName(strategyInterface) +
                            " in application context");
        }
    }

    // Sending

    public ResponseActions sendMessage(RequestCreator requestCreator) {
        Assert.notNull(requestCreator, "'requestCreator' must not be null");
        try {
            WebServiceMessage request = requestCreator.createRequest(messageFactory);
            MessageContext messageContext = new DefaultMessageContext(request, messageFactory);

            messageReceiver.receive(messageContext);

            return new MockWebServiceExchange(messageContext);
        }
        catch (Exception ex) {
            fail(ex.getMessage());
            return null;
        }
    }

    public ResponseActions sendPayload(Source payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return sendMessage(new PayloadRequestCreator(payload));
    }

    public ResponseActions sendPayload(Resource payload) throws IOException {
        Assert.notNull(payload, "'payload' must not be null");
        return sendMessage(new PayloadRequestCreator(new ResourceSource(payload)));
    }

    private class MockWebServiceExchange implements ResponseActions {

        private final MessageContext messageContext;

        private MockWebServiceExchange(MessageContext messageContext) {
            Assert.notNull(messageContext, "'messageContext' must not be null");
            this.messageContext = messageContext;
        }

        public ResponseActions andExpect(ResponseMatcher responseMatcher) {
            WebServiceMessage response = messageContext.getResponse();
            if (response == null) {
                fail("No response received");
                return null;
            }
            try {
                responseMatcher.match(response);
                return this;
            }
            catch (IOException ex) {
                fail(ex.getMessage());
                return null;
            }
        }

        public ResponseActions andExpectPayload(Source payload) {
            final PayloadDiffMatcher matcher = new PayloadDiffMatcher(payload);
            return andExpect(new ResponseMatcher() {
                public void match(WebServiceMessage response) throws IOException, AssertionError {
                    matcher.match(response);
                }
            });
        }

        public ResponseActions andExpectPayload(Resource payload) throws IOException {
            return andExpectPayload(new ResourceSource(payload));
        }
    }


}
