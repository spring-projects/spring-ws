/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.client.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.WebServiceTransformerException;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.support.WebServiceAccessor;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.TransportException;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.support.DefaultStrategiesHelper;

/**
 * <strong>The central class for client-side Web services.</strong> It provides a message-driven approach to sending and
 * receiving {@link org.springframework.ws.WebServiceMessage} instances.
 * <p/>
 * Code using this class need only implement callback interfaces, provide {@link Source} objects to read data from, or
 * use the pluggable {@link Marshaller} support.
 * <p/>
 * This template uses a {@link SimpleFaultResolver} to handle responses that contain faults.
 *
 * @author Arjen Poutsma
 */
public class WebServiceTemplate extends WebServiceAccessor implements WebServiceOperations, ApplicationContextAware {

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    private FaultResolver faultResolver = new SimpleFaultResolver();

    private String defaultUri;

    /** Creates a new <code>WebServiceTemplate</code> using default settings. */
    public WebServiceTemplate() {
    }

    /**
     * Creates a new <code>WebServiceTemplate</code> based on the given message factory.
     *
     * @param messageFactory the message factory to use
     */
    public WebServiceTemplate(WebServiceMessageFactory messageFactory) {
        setMessageFactory(messageFactory);
    }

    /** Returns the default URI to be used on operations that do not have a URI parameter. */
    public String getDefaultUri() {
        return defaultUri;
    }

    /**
     * Set the default URI to be used on operations that do not have a URI parameter.
     *
     * @see #marshalSendAndReceive(Object)
     * @see #marshalSendAndReceive(Object,WebServiceMessageCallback)
     * @see #sendAndReceive(Source,Result)
     * @see #sendAndReceive(Source,WebServiceMessageCallback,Result)
     * @see #sendAndReceive(Source,SourceExtractor)
     * @see #sendAndReceive(Source,WebServiceMessageCallback,SourceExtractor)
     * @see #sendAndReceive(WebServiceMessageCallback,WebServiceMessageCallback)
     */
    public void setDefaultUri(String uri) {
        this.defaultUri = uri;
    }

    /** Returns the marshaller for this template. */
    public Marshaller getMarshaller() {
        return marshaller;
    }

    /** Sets the marshaller for this template. */
    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    /** Returns the unmarshaller for this template. */
    public Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    /** Sets the unmarshaller for this template. */
    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    /** Returns the fault resolver for this template. */
    public FaultResolver getFaultResolver() {
        return faultResolver;
    }

    /** Sets the fault resolver for this template. */
    public void setFaultResolver(FaultResolver faultResolver) {
        Assert.notNull(faultResolver, "faultResolver must not be null");
        this.faultResolver = faultResolver;
    }

    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Resource resource = new ClassPathResource(ClassUtils.getShortName(getClass()) + ".properties", getClass());
        DefaultStrategiesHelper defaultStrategiesHelper = new DefaultStrategiesHelper(resource);
        if (getMessageFactory() == null) {
            initWebServiceMessageFactory(defaultStrategiesHelper, applicationContext);
        }
        if (ObjectUtils.isEmpty(getMessageSenders())) {
            initWebServiceMessageSenders(defaultStrategiesHelper, applicationContext);
        }
    }

    private void initWebServiceMessageFactory(DefaultStrategiesHelper defaultStrategiesHelper,
                                              ApplicationContext applicationContext)
            throws BeanInitializationException {
        WebServiceMessageFactory messageFactory = (WebServiceMessageFactory) defaultStrategiesHelper
                .getDefaultStrategy(WebServiceMessageFactory.class, applicationContext);
        if (logger.isInfoEnabled()) {
            logger.info("Using default message factory [" + messageFactory + "]");
        }
        if (messageFactory instanceof InitializingBean) {
            try {
                ((InitializingBean) messageFactory).afterPropertiesSet();
            }
            catch (Exception ex) {
                throw new BeanInitializationException("Could not initialize message factory", ex);
            }
        }
        setMessageFactory(messageFactory);
    }

    private void initWebServiceMessageSenders(DefaultStrategiesHelper defaultStrategiesHelper,
                                              ApplicationContext applicationContext) {
        List messageSenders =
                defaultStrategiesHelper.getDefaultStrategies(WebServiceMessageSender.class, applicationContext);
        if (logger.isInfoEnabled()) {
            logger.info("Using default message senders " + messageSenders);
        }
        for (Iterator iterator = messageSenders.iterator(); iterator.hasNext();) {
            WebServiceMessageSender messageSender = (WebServiceMessageSender) iterator.next();
            if (messageSender instanceof InitializingBean) {
                try {
                    ((InitializingBean) messageSender).afterPropertiesSet();
                }
                catch (Exception ex) {
                    throw new BeanInitializationException("Could not initialize message factory", ex);
                }
            }
        }
        setMessageSenders(
                (WebServiceMessageSender[]) messageSenders.toArray(new WebServiceMessageSender[messageSenders.size()]));
    }

    /*
    * Marshalling methods
    */

    public Object marshalSendAndReceive(final Object requestPayload) {
        return marshalSendAndReceive(requestPayload, null);
    }

    public Object marshalSendAndReceive(String uri, final Object requestPayload) {
        return marshalSendAndReceive(requestPayload, null);
    }

    public Object marshalSendAndReceive(final Object requestPayload, final WebServiceMessageCallback requestCallback) {
        return marshalSendAndReceive(getDefaultUri(), requestPayload, requestCallback);
    }

    public Object marshalSendAndReceive(String uri,
                                        final Object requestPayload,
                                        final WebServiceMessageCallback requestCallback) {
        if (getMarshaller() == null) {
            throw new IllegalStateException("No marshaller registered. Check configuration of WebServiceTemplate.");
        }
        if (getUnmarshaller() == null) {
            throw new IllegalStateException("No unmarshaller registered. Check configuration of WebServiceTemplate.");
        }
        return sendAndReceive(uri, new WebServiceMessageCallback() {

            public void doInMessage(WebServiceMessage message) throws IOException {
                getMarshaller().marshal(requestPayload, message.getPayloadResult());
                if (requestCallback != null) {
                    requestCallback.doInMessage(message);
                }
            }
        }, new WebServiceMessageExtractor() {

            public Object extractData(WebServiceMessage message) throws IOException {
                return getUnmarshaller().unmarshal(message.getPayloadSource());
            }
        });
    }

    /*
    * Result-handling methods
    */

    public void sendAndReceive(Source requestPayload, Result responseResult) {
        sendAndReceive(requestPayload, null, responseResult);
    }

    public void sendAndReceive(String uri, Source requestPayload, Result responseResult) {
        sendAndReceive(uri, requestPayload, null, responseResult);
    }

    public void sendAndReceive(Source requestPayload,
                               WebServiceMessageCallback requestCallback,
                               final Result responseResult) {
        sendAndReceive(getDefaultUri(), requestPayload, requestCallback, responseResult);
    }

    public void sendAndReceive(String uri,
                               Source requestPayload,
                               WebServiceMessageCallback requestCallback,
                               final Result responseResult) {
        try {
            final Transformer transformer = createTransformer();
            doSendAndReceive(uri, transformer, requestPayload, requestCallback, new SourceExtractor() {

                public Object extractData(Source source) throws IOException {
                    try {
                        transformer.transform(source, responseResult);
                    }
                    catch (TransformerException ex) {
                        throw new WebServiceTransformerException("Could not transform payload", ex);
                    }
                    return null;
                }
            });
        }
        catch (TransformerConfigurationException ex) {
            throw new WebServiceTransformerException("Could not create transformer", ex);
        }
    }

    /*
    * Source-handling methods
    */

    public Object sendAndReceive(final Source requestPayload, final SourceExtractor responseExtractor) {
        return sendAndReceive(requestPayload, null, responseExtractor);
    }

    public Object sendAndReceive(String uri, final Source requestPayload, final SourceExtractor responseExtractor) {
        return sendAndReceive(uri, requestPayload, null, responseExtractor);
    }

    public Object sendAndReceive(final Source requestPayload,
                                 final WebServiceMessageCallback requestCallback,
                                 final SourceExtractor responseExtractor) {
        return sendAndReceive(getDefaultUri(), requestPayload, requestCallback, responseExtractor);
    }

    public Object sendAndReceive(String uri,
                                 final Source requestPayload,
                                 final WebServiceMessageCallback requestCallback,
                                 final SourceExtractor responseExtractor) {

        try {
            return doSendAndReceive(uri, createTransformer(), requestPayload, requestCallback, responseExtractor);
        }
        catch (TransformerConfigurationException ex) {
            throw new WebServiceTransformerException("Could not create transformer", ex);
        }
    }

    private Object doSendAndReceive(String uri,
                                    final Transformer transformer,
                                    final Source requestPayload,
                                    final WebServiceMessageCallback requestCallback,
                                    final SourceExtractor responseExtractor) {
        Assert.notNull(responseExtractor, "responseExtractor must not be null");
        return sendAndReceive(uri, new WebServiceMessageCallback() {
            public void doInMessage(WebServiceMessage message) throws IOException {
                try {
                    transformer.transform(requestPayload, message.getPayloadResult());
                    if (requestCallback != null) {
                        requestCallback.doInMessage(message);
                    }
                }
                catch (TransformerException ex) {
                    throw new WebServiceTransformerException("Could not transform payload to request message", ex);
                }
            }
        }, new SourceExtractorMessageExtractor(responseExtractor));
    }

    /*
    * WebServiceMessage-handling methods
    */

    public void sendAndReceive(WebServiceMessageCallback requestCallback, WebServiceMessageCallback responseCallback) {
        Assert.notNull(responseCallback, "responseCallback must not be null");
        sendAndReceive(requestCallback, new WebServiceMessageCallbackMessageExtractor(responseCallback));
    }

    public void sendAndReceive(String uri,
                               WebServiceMessageCallback requestCallback,
                               WebServiceMessageCallback responseCallback) {
        Assert.notNull(responseCallback, "responseCallback must not be null");
        sendAndReceive(uri, requestCallback, new WebServiceMessageCallbackMessageExtractor(responseCallback));
    }

    public Object sendAndReceive(WebServiceMessageCallback requestCallback,
                                 WebServiceMessageExtractor responseExtractor) {
        return sendAndReceive(getDefaultUri(), requestCallback, responseExtractor);
    }

    public Object sendAndReceive(String uri,
                                 WebServiceMessageCallback requestCallback,
                                 WebServiceMessageExtractor responseExtractor) {
        Assert.notNull(responseExtractor, "'responseExtractor' must not be null");
        Assert.hasLength(uri, "'uri' must not be empty");
        TransportContext previousTransportContext = TransportContextHolder.getTransportContext();
        WebServiceConnection connection = null;
        try {
            connection = createConnection(uri);
            TransportContextHolder.setTransportContext(new DefaultTransportContext(connection));
            WebServiceMessage request = getMessageFactory().createWebServiceMessage();
            if (requestCallback != null) {
                requestCallback.doInMessage(request);
            }
            sendRequest(connection, request);
            WebServiceMessage response = receiveResponse(connection);
            if (response != null) {
                if (!hasFault(connection, response)) {
                    // normal response
                    return responseExtractor.extractData(response);
                }
                else {
                    // fault response
                    getFaultResolver().resolveFault(response);
                }
            }
            return null;
        }
        catch (TransportException ex) {
            throw new WebServiceTransportException("Could not use transport: " + ex.getMessage(), ex);
        }
        catch (IOException ex) {
            throw new WebServiceIOException("I/O error: " + ex.getMessage(), ex);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (IOException ex) {
                    logger.debug("Could not close WebServiceConnection", ex);
                }
            }
            TransportContextHolder.setTransportContext(previousTransportContext);
        }
    }

    /** Sends the request in the given message context over the connection. */
    private void sendRequest(WebServiceConnection connection, WebServiceMessage request) throws IOException {
        if (logger.isTraceEnabled()) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            request.writeTo(os);
            logger.trace("WebServiceTemplate sends request [" + os.toString("UTF-8") + "]");
        }
        else if (logger.isDebugEnabled()) {
            logger.debug("WebServiceTemplate sends request [" + request + "]");
        }
        connection.send(request);
    }

    private WebServiceMessage receiveResponse(WebServiceConnection connection) throws IOException {
        WebServiceMessage response = connection.receive(getMessageFactory());
        if (response != null) {
            if (logger.isTraceEnabled()) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                response.writeTo(os);
                logger.trace("WebServiceTemplate receives response [" + os.toString("UTF-8") + "]");
            }
            else if (logger.isDebugEnabled()) {
                logger.debug("WebServiceTemplate receives response [" + response + "]");
            }
        }
        else if (logger.isDebugEnabled()) {
            logger.debug("WebServiceTemplate receives no response");
        }
        return response;
    }

    /** Determines whether the given connection or message context has a fault. */
    private boolean hasFault(WebServiceConnection connection, WebServiceMessage response) throws IOException {
        if (connection instanceof FaultAwareWebServiceConnection) {
            return ((FaultAwareWebServiceConnection) connection).hasFault();
        }
        else {
            return response.hasFault();
        }
    }

    /** Adapter to enable use of a WebServiceMessageCallback inside a WebServiceMessageExtractor. */
    private static class WebServiceMessageCallbackMessageExtractor implements WebServiceMessageExtractor {

        private final WebServiceMessageCallback callback;

        public WebServiceMessageCallbackMessageExtractor(WebServiceMessageCallback callback) {
            this.callback = callback;
        }

        public Object extractData(WebServiceMessage message) throws IOException {
            callback.doInMessage(message);
            return null;
        }
    }

    /** Adapter to enable use of a SourceExtractor inside a WebServiceMessageExtractor. */
    private static class SourceExtractorMessageExtractor implements WebServiceMessageExtractor {

        private final SourceExtractor sourceExtractor;

        public SourceExtractorMessageExtractor(SourceExtractor sourceExtractor) {
            this.sourceExtractor = sourceExtractor;
        }

        public Object extractData(WebServiceMessage message) throws IOException {
            return sourceExtractor.extractData(message.getPayloadSource());
        }
    }


}
