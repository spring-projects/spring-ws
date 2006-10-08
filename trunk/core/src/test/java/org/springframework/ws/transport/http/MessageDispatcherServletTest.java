package org.springframework.ws.transport.http;

import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import junit.framework.TestCase;
import org.springframework.beans.BeansException;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.ws.MessageDispatcher;
import org.springframework.ws.endpoint.PayloadEndpointAdapter;
import org.springframework.ws.endpoint.mapping.PayloadRootQNameEndpointMapping;
import org.springframework.ws.soap.endpoint.SimpleSoapExceptionResolver;

public class MessageDispatcherServletTest extends TestCase {

    private ServletConfig config;

    private MessageDispatcherServlet servlet;

    protected void setUp() throws Exception {
        config = new MockServletConfig(new MockServletContext(), "spring-ws");
        servlet = new MessageDispatcherServlet();
    }

    private void assertStrategies(Class expectedClass, List actual) {
        assertEquals("Invalid amount of strategies", 1, actual.size());
        Object strategy = actual.get(0);
        assertTrue("Invalid strategy", expectedClass.isAssignableFrom(strategy.getClass()));
    }

    public void testBeanNameStrategies() throws ServletException {
        servlet.setDetectAllEndpointAdapters(false);
        servlet.setDetectAllEndpointMappings(false);
        servlet.setDetectAllEndpointExceptionResolvers(false);
        servlet.setContextClass(BeanNameWebApplicationContext.class);
        servlet.init(config);
        MessageDispatcher messageDispatcher = servlet.getMessageDispatcher();
        assertNotNull("No messageDispatcher created", messageDispatcher);
        assertStrategies(PayloadRootQNameEndpointMapping.class, messageDispatcher.getEndpointMappings());
        assertStrategies(PayloadEndpointAdapter.class, messageDispatcher.getEndpointAdapters());
        assertStrategies(SimpleSoapExceptionResolver.class, messageDispatcher.getEndpointExceptionResolvers());
    }

    public void testConfiguredMessageDispatcher() throws ServletException {
        servlet.setContextClass(MessageDispatcherWebApplicationContext.class);
        servlet.init(config);
        MessageDispatcher messageDispatcher = servlet.getMessageDispatcher();
        assertNotNull("No messageDispatcher created", messageDispatcher);
        assertNull("Default strategies loaded", messageDispatcher.getEndpointMappings());
        assertNull("Default strategies loaded", messageDispatcher.getEndpointExceptionResolvers());
    }

    public void testDefaultStrategies() throws ServletException {
        servlet.setContextClass(StaticWebApplicationContext.class);
        servlet.init(config);
        MessageDispatcher messageDispatcher = servlet.getMessageDispatcher();
        assertNotNull("No messageDispatcher created", messageDispatcher);
    }

    public void testDetectedStrategies() throws ServletException {
        servlet.setContextClass(DetectWebApplicationContext.class);
        servlet.init(config);
        MessageDispatcher messageDispatcher = servlet.getMessageDispatcher();
        assertNotNull("No messageDispatcher created", messageDispatcher);
        assertStrategies(PayloadRootQNameEndpointMapping.class, messageDispatcher.getEndpointMappings());
        assertStrategies(PayloadEndpointAdapter.class, messageDispatcher.getEndpointAdapters());
        assertStrategies(SimpleSoapExceptionResolver.class, messageDispatcher.getEndpointExceptionResolvers());
    }

    private static class DetectWebApplicationContext extends StaticWebApplicationContext {

        public void refresh() throws BeansException, IllegalStateException {
            registerSingleton("payloadMapping", PayloadRootQNameEndpointMapping.class);
            registerSingleton("payloadAdapter", PayloadEndpointAdapter.class);
            registerSingleton("simpleExceptionResolver", SimpleSoapExceptionResolver.class);
            super.refresh();
        }
    }

    private static class BeanNameWebApplicationContext extends StaticWebApplicationContext {

        public void refresh() throws BeansException, IllegalStateException {
            registerSingleton(MessageDispatcherServlet.ENDPOINT_MAPPING_BEAN_NAME,
                    PayloadRootQNameEndpointMapping.class);
            registerSingleton(MessageDispatcherServlet.ENDPOINT_ADAPTER_BEAN_NAME, PayloadEndpointAdapter.class);
            registerSingleton(MessageDispatcherServlet.ENDPOINT_EXCEPTION_RESOLVER_BEAN_NAME,
                    SimpleSoapExceptionResolver.class);
            super.refresh();
        }
    }

    private static class MessageDispatcherWebApplicationContext extends StaticWebApplicationContext {

        public void refresh() throws BeansException, IllegalStateException {
            registerSingleton(MessageDispatcherServlet.MESSAGE_DISPATCHER_BEAN_NAME, MessageDispatcher.class);
            registerSingleton(MessageDispatcherServlet.ENDPOINT_MAPPING_BEAN_NAME,
                    PayloadRootQNameEndpointMapping.class);
            registerSingleton(MessageDispatcherServlet.ENDPOINT_ADAPTER_BEAN_NAME, PayloadEndpointAdapter.class);
            registerSingleton(MessageDispatcherServlet.ENDPOINT_EXCEPTION_RESOLVER_BEAN_NAME,
                    SimpleSoapExceptionResolver.class);
            super.refresh();
        }
    }
}