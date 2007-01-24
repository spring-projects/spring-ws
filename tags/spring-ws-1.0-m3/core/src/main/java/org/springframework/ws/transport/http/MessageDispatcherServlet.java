package org.springframework.ws.transport.http;

import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.util.WebUtils;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.transport.support.DefaultStrategiesHelper;
import org.springframework.ws.wsdl.WsdlDefinition;

/**
 * Servlet for simplified dispatching of Web service messages. Delegates to a <code>MessageDispatcher</code> and a
 * <code>WebServiceMessageReceiverHandlerAdapter</code>.
 * <p/>
 * This servlet is a convenient alternative for a standard Spring-MVC <code>DispatcherServlet</code> with a separate
 * <code>WebServiceMessageReceiverHandlerAdapter</code> and a <code>MessageDispatcher</code>.
 * <p/>
 * This servlet automatically detects <code>EndpointAdapter</code>s, <code>EndpointMapping</code>s, and
 * <code>EndpointExceptionResolver</code>s, by type (when the corresponding detectAll* property is enabled) or by type
 * (when the property is disabled). For instance, when the <code>detectAllEndpointAdapters</code> propery is
 * <code>true</code> (the default), all endpoint adapters defined in the web application context are registered with the
 * message dispatcher. When it is set to <code>false</code>, this servlet tries to find a bean with the
 * "endpointAdapter" bean name in the context.
 *
 * @author Arjen Poutsma
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.ws.server.MessageDispatcher
 * @see org.springframework.ws.transport.http.WebServiceMessageReceiverHandlerAdapter
 */
public class MessageDispatcherServlet extends FrameworkServlet {

    /**
     * Well-known name for the <code>WebServiceMessageFactory</code> object in the bean factory for this namespace.
     */
    public static final String WEB_SERVICE_MESSAGE_FACTORY_BEAN_NAME = "messageFactory";

    /**
     * Well-known name for the <code>WebServiceMessageReceiver</code> object in the bean factory for this namespace.
     */
    public static final String MESSAGE_RECEIVER_BEAN_NAME = "messageReceiver";

    /**
     * Name of the class path resource (relative to the MessageDispatcherServlet class) that defines
     * MessageDispatcherServlet's default strategy names.
     */
    private static final String DEFAULT_STRATEGIES_PATH = "MessageDispatcherServlet.properties";

    /**
     * Suffix of a WSDL request uri.
     */
    private static final String WSDL_SUFFIX_NAME = ".wsdl";

    private final DefaultStrategiesHelper defaultStrategiesHelper;

    /**
     * The <code>WebServiceMessageReceiverHandlerAdapter</code> used by this servlet.
     */
    private WebServiceMessageReceiverHandlerAdapter messageReceiverHandlerAdapter =
            new WebServiceMessageReceiverHandlerAdapter();

    /**
     * The <code>WsdlDefinitionHandlerAdapter</code> used by this servlet.
     */
    private WsdlDefinitionHandlerAdapter wsdlDefinitionHandlerAdapter = new WsdlDefinitionHandlerAdapter();

    /**
     * The <code>WebServiceMessageReceiver</code> used by this servlet.
     */
    private WebServiceMessageReceiver messageReceiver;

    /**
     * Keys are bean names, values are <code>WsdlDefinition</code>s.
     */
    private Map wsdlDefinitions;

    /**
     * Public constructor, necessary for some Web application servers.
     */
    public MessageDispatcherServlet() {
        defaultStrategiesHelper = new DefaultStrategiesHelper(
                new ClassPathResource(DEFAULT_STRATEGIES_PATH, MessageDispatcherServlet.class));
    }

    /**
     * Returns the <code>WebServiceMessageReceiver</code> used by this servlet.
     */
    protected WebServiceMessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    protected void initFrameworkServlet() throws ServletException, BeansException {
        initWebServiceMessageFactory();
        initMessageReceiver();
        initWsdlDefinitions();
    }

    protected long getLastModified(HttpServletRequest httpServletRequest) {
        WsdlDefinition definition = getWsdlDefinition(httpServletRequest);
        if (definition != null) {
            return wsdlDefinitionHandlerAdapter.getLastModified(httpServletRequest, definition);
        }
        else {
            return messageReceiverHandlerAdapter.getLastModified(httpServletRequest, messageReceiver);
        }
    }

    protected void doService(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        WsdlDefinition definition = getWsdlDefinition(httpServletRequest);
        if (definition != null) {
            wsdlDefinitionHandlerAdapter.handle(httpServletRequest, httpServletResponse, definition);
        }
        else {
            messageReceiverHandlerAdapter.handle(httpServletRequest, httpServletResponse, messageReceiver);
        }
    }

    /**
     * Determines the <code>WsdlDefinition</code> for a given request, or <code>null</code> if none is found.
     * <p/>
     * Default implementation checks whether the request method is GET, whether the request uri ends with ".wsdl", and
     * if there is a <code>WsdlDefinition</code> with the same name as the filename in the request uri.
     *
     * @param request the <code>HttpServletRequest</code>
     * @return a definition, or <code>null</code>
     */
    protected WsdlDefinition getWsdlDefinition(HttpServletRequest request) {
        if ("GET".equals(request.getMethod()) && request.getRequestURI().endsWith(WSDL_SUFFIX_NAME)) {
            String fileName = WebUtils.extractFilenameFromUrlPath(request.getRequestURI());
            return (WsdlDefinition) wsdlDefinitions.get(fileName);
        }
        else {
            return null;
        }
    }

    private void initWebServiceMessageFactory() throws BeansException {
        WebServiceMessageFactory messageFactory;
        try {
            messageFactory = (WebServiceMessageFactory) getWebApplicationContext()
                    .getBean(WEB_SERVICE_MESSAGE_FACTORY_BEAN_NAME, WebServiceMessageFactory.class);
        }
        catch (NoSuchBeanDefinitionException ignored) {
            messageFactory = (WebServiceMessageFactory) defaultStrategiesHelper
                    .getDefaultStrategy(WebServiceMessageFactory.class, getWebApplicationContext());
            if (logger.isInfoEnabled()) {
                logger.info("Unable to locate WebServiceMessageFactory with name '" +
                        WEB_SERVICE_MESSAGE_FACTORY_BEAN_NAME + "': using default [" + messageFactory + "]");
            }
            if (messageFactory instanceof InitializingBean) {
                try {
                    ((InitializingBean) messageFactory).afterPropertiesSet();
                }
                catch (Exception ex) {
                    throw new BeanInitializationException("Could not invoke afterPropertiesSet() on message factory",
                            ex);
                }
            }
        }
        messageReceiverHandlerAdapter.setMessageFactory(messageFactory);
    }

    private void initMessageReceiver() {
        try {
            messageReceiver = (WebServiceMessageReceiver) getWebApplicationContext()
                    .getBean(MESSAGE_RECEIVER_BEAN_NAME, WebServiceMessageReceiver.class);
        }
        catch (NoSuchBeanDefinitionException ex) {
            messageReceiver = (WebServiceMessageReceiver) defaultStrategiesHelper
                    .getDefaultStrategy(WebServiceMessageReceiver.class, getWebApplicationContext());
            if (messageReceiver instanceof BeanNameAware) {
                ((BeanNameAware) messageReceiver).setBeanName(getServletName());
            }
            if (logger.isInfoEnabled()) {
                logger.info("Unable to locate MessageDispatcher with name '" + MESSAGE_RECEIVER_BEAN_NAME +
                        "': using default [" + messageReceiver + "]");
            }
        }
    }

    private void initWsdlDefinitions() {
        // Find all WsdlDefinitions in the ApplicationContext, incuding ancestor contexts.
        wsdlDefinitions = BeanFactoryUtils
                .beansOfTypeIncludingAncestors(getWebApplicationContext(), WsdlDefinition.class, true, false);
    }
}
