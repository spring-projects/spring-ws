/*
 * Copyright 2005-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.server.endpoint.interceptor;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

@Ignore
public class SoapEnvelopeLoggingInterceptorTest {

	private SoapEnvelopeLoggingInterceptor interceptor;

	private CountingAppender appender;

	private MessageContext messageContext;

	@Before
	public void setUp() throws Exception {
		interceptor = new SoapEnvelopeLoggingInterceptor();
		appender = new SoapEnvelopeLoggingInterceptorTest.CountingAppender();
		BasicConfigurator.configure(appender);
		Logger.getRootLogger().setLevel(Level.DEBUG);
		SaajSoapMessageFactory factory = new SaajSoapMessageFactory();
		factory.afterPropertiesSet();
		messageContext = new DefaultMessageContext(factory);
		appender.reset();
	}

	@After
	public void tearDown() throws Exception {
		BasicConfigurator.resetConfiguration();
		ClassPathResource resource = new ClassPathResource("log4j.properties");
		PropertyConfigurator.configure(resource.getURL());
	}

	@Test
	public void testHandleRequestDisabled() throws Exception {
		interceptor.setLogRequest(false);
		int eventCount = appender.getCount();
		interceptor.handleRequest(messageContext, null);
		Assert.assertEquals("interceptor logged when disabled", appender.getCount(), eventCount);
	}

	@Test
	public void testHandleRequestEnabled() throws Exception {
		int eventCount = appender.getCount();
		interceptor.handleRequest(messageContext, null);
		Assert.assertTrue("interceptor did not log", appender.getCount() > eventCount);
	}

	@Test
	public void testHandleResponseDisabled() throws Exception {
		messageContext.getResponse();
		interceptor.setLogResponse(false);
		int eventCount = appender.getCount();
		interceptor.handleResponse(messageContext, null);
		Assert.assertEquals("interceptor logged when disabled", appender.getCount(), eventCount);
	}

	@Test
	public void testHandleResponseEnabled() throws Exception {
		messageContext.getResponse();
		int eventCount = appender.getCount();
		interceptor.handleResponse(messageContext, null);
		Assert.assertTrue("interceptor did not log", appender.getCount() > eventCount);
	}

	@Test
	public void testHandleFaultDisabled() throws Exception {
		messageContext.getResponse();
		interceptor.setLogFault(false);
		int eventCount = appender.getCount();
		interceptor.handleFault(messageContext, null);
		Assert.assertEquals("interceptor logged when disabled", appender.getCount(), eventCount);
	}

	@Test
	public void testHandleFaultEnabled() throws Exception {
		messageContext.getResponse();
		int eventCount = appender.getCount();
		interceptor.handleResponse(messageContext, null);
		Assert.assertTrue("interceptor did not log", appender.getCount() > eventCount);
	}

	private static class CountingAppender extends AppenderSkeleton {

		private int count;

		public int getCount() {
			return count;
		}

		public void reset() {
			count = 0;
		}

		@Override
		protected void append(LoggingEvent loggingEvent) {
			count++;
		}

		@Override
		public boolean requiresLayout() {
			return false;
		}

		@Override
		public void close() {
		}
	}
}
