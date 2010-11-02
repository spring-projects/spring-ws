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

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.ws.transport.WebServiceMessageReceiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.springframework.ws.test.support.Assert.fail;

/**
 * <strong>Main entry point for server-side Web service testing</strong>. Typically used to test a {@link
 * org.springframework.ws.server.MessageDispatcher MessageDispatcher} (including its endpoints, mappings, etc) by
 * creating request messages, and setting up expectations about response messages.
 * <p/>
 * The typical usage of this class is:
 * <ol>
 * <li>Create a {@code MockWebServiceClient} instance by using {@link #createClient(ApplicationContext)} or
 * {@link #createClient(WebServiceMessageReceiver, WebServiceMessageFactory)}</li>
 * <li>Send request messages by calling {@link #sendMessage(RequestCreator)}, possibly by using the default
 * {@link RequestCreator} implementations provided in {@link RequestCreators} (which can be statically imported).</li>
 * <li>Set up response expectations by calling {@link ResponseActions#andExpect(ResponseMatcher) andExpect(ResponseMatcher)},
 * possibly by using the default {@link ResponseMatcher} implementations provided in {@link ResponseMatchers}
 * (which can be statically imported). Multiple expectations can be set up by chaining {@code andExpect()} calls.</li>
 * </ol>
 * Note that because of the 'fluent' API offered by this class (and related classes), you can typically use the Code
 * Completion features (i.e. ctrl-space) in your IDE to set up the mocks.
 * <p/>
 * For example:
 * <blockquote><pre>
 * </pre></blockquote>
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
public class MockWebServiceClient {

    private static final Log logger = LogFactory.getLog(MockWebServiceClient.class);

    private final WebServiceMessageReceiver messageReceiver;

    private final WebServiceMessageFactory messageFactory;

    // Constructors

    private MockWebServiceClient(WebServiceMessageReceiver messageReceiver, WebServiceMessageFactory messageFactory) {
        Assert.notNull(messageReceiver, "'messageReceiver' must not be null");
        Assert.notNull(messageFactory, "'messageFactory' must not be null");
        this.messageReceiver = messageReceiver;
        this.messageFactory = messageFactory;
    }

    // Factory methods

    /**
     * Creates a {@code MockWebServiceClient} instance based on the given {@link WebServiceMessageReceiver} and {@link
     * WebServiceMessageFactory}.
     *
     * @param messageReceiver the message receiver, typically a {@link SoapMessageDispatcher}
     * @param messageFactory  the message factory
     * @return the created client
     */
    public static MockWebServiceClient createClient(WebServiceMessageReceiver messageReceiver,
                                                    WebServiceMessageFactory messageFactory) {
        return new MockWebServiceClient(messageReceiver, messageFactory);
    }

    /**
     * Creates a {@code MockWebServiceClient} instance based on the given {@link ApplicationContext}.
     *
     * This factory method works in a similar fashion as the standard
     * {@link org.springframework.ws.transport.http.MessageDispatcherServlet MessageDispatcherServlet}. That is:
     * <ul>
     * <li>If a {@link WebServiceMessageReceiver} is configured in the given application context, it will use that.
     * If no message receiver is configured, it will create a default {@link SoapMessageDispatcher}.</li>
     * <li>If a {@link WebServiceMessageFactory} is configured in the given application context, it will use that.
     * If no message factory is configured, it will create a default {@link SaajSoapMessageFactory}.</li>
     * </ul>
     *
     * @param applicationContext the application context to base the client on
     * @return the created client
     */
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

    /**
     * Sends a request message by using the given {@link RequestCreator}. Typically called by using the default request
     * creators provided by {@link RequestCreators}.
     *
     * @param requestCreator the request creator
     * @return the response actions
     * @see RequestCreators
     */
    public ResponseActions sendMessage(RequestCreator requestCreator) {
        Assert.notNull(requestCreator, "'requestCreator' must not be null");
        try {
            WebServiceMessage request = requestCreator.createRequest(messageFactory);
            MessageContext messageContext = new DefaultMessageContext(request, messageFactory);

            messageReceiver.receive(messageContext);

            return new MockWebServiceClientResponseActions(messageContext);
        }
        catch (Exception ex) {
            fail(ex.getMessage());
            return null;
        }
    }

    // ResponseActions

    private static class MockWebServiceClientResponseActions implements ResponseActions {

        private final MessageContext messageContext;

        private MockWebServiceClientResponseActions(MessageContext messageContext) {
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
    }


}
