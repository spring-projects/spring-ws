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

package org.springframework.ws.mock.server;

import java.util.Map;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.ClassUtils;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.ws.transport.WebServiceMessageReceiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Arjen Poutsma
 */
public class WebServiceTestExecutionListener extends AbstractTestExecutionListener {

    private static final Log logger = LogFactory.getLog(WebServiceTestExecutionListener.class);

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        ApplicationContext applicationContext = testContext.getApplicationContext();
        WebServiceMessageReceiver messageReceiver = getMessageReceiver(applicationContext);
        WebServiceMessageFactory messageFactory = getMessageFactory(applicationContext);
        WebServiceTestContext context = new WebServiceTestContext(messageReceiver, messageFactory);
        WebServiceTestContextHolder.set(context);
    }

    private WebServiceMessageReceiver getMessageReceiver(ApplicationContext applicationContext) {
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

    private WebServiceMessageFactory getMessageFactory(ApplicationContext applicationContext) throws Exception {
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

    private <T> T getStrategy(ApplicationContext applicationContext, Class<T> strategyInterface) {
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
            throw new BeanInitializationException("Could not find exactly 1 Message Dispatcher in application context");
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        WebServiceTestContextHolder.clear();
    }
}
