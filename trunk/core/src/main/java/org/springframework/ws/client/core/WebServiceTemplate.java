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
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.WebServiceTransformerException;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.support.WebServiceAccessor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.TransportException;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
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

    private DefaultStrategiesHelper defaultStrategiesHelper;

    /** Creates a new <code>WebServiceTemplate</code> using default settings. */
    public WebServiceTemplate() {
        Resource resource = new ClassPathResource(ClassUtils.getShortName(getClass()) + ".properties", getClass());
        defaultStrategiesHelper = new DefaultStrategiesHelper(resource);
    }

    /**
     * Creates a new <code>WebServiceTemplate</code> based on the given message factory and message sender.
     *
     * @param messageFactory the message factory to use
     * @param messageSender  the message sender to use
     */
    public WebServiceTemplate(WebServiceMessageFactory messageFactory, WebServiceMessageSender messageSender) {
        this();
        setMessageFactory(messageFactory);
        setMessageSender(messageSender);
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

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (getMessageFactory() == null) {
            initWebServiceMessageFactory(applicationContext);
        }
    }

    private void initWebServiceMessageFactory(ApplicationContext applicationContext)
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

    /*
    * Marshalling methods
    */

    public Object marshalSendAndReceive(final Object requestPayload) {
        return marshalSendAndReceive(requestPayload, null);
    }

    public Object marshalSendAndReceive(final Object requestPayload, final WebServiceMessageCallback requestCallback) {
        if (getMarshaller() == null) {
            throw new IllegalStateException("No marshaller registered. Check configuration of WebServiceTemplate.");
        }
        if (getUnmarshaller() == null) {
            throw new IllegalStateException("No unmarshaller registered. Check configuration of WebServiceTemplate.");
        }
        return sendAndReceive(new WebServiceMessageCallback() {

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

    public void sendAndReceive(Source requestPayload,
                               WebServiceMessageCallback requestCallback,
                               final Result responseResult) {
        try {
            final Transformer transformer = createTransformer();
            doSendAndReceive(transformer, requestPayload, requestCallback, new SourceExtractor() {

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

    public Object sendAndReceive(final Source requestPayload,
                                 final WebServiceMessageCallback requestCallback,
                                 final SourceExtractor responseExtractor) {

        try {
            return doSendAndReceive(createTransformer(), requestPayload, requestCallback, responseExtractor);
        }
        catch (TransformerConfigurationException ex) {
            throw new WebServiceTransformerException("Could not create transformer", ex);
        }
    }

    private Object doSendAndReceive(final Transformer transformer,
                                    final Source requestPayload,
                                    final WebServiceMessageCallback requestCallback,
                                    final SourceExtractor responseExtractor) {
        Assert.notNull(responseExtractor, "responseExtractor must not be null");
        return sendAndReceive(new WebServiceMessageCallback() {
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

    public Object sendAndReceive(WebServiceMessageCallback requestCallback,
                                 WebServiceMessageExtractor responseExtractor) {
        Assert.notNull(responseExtractor, "response extractor must not be null");
        MessageContext messageContext = createMessageContext();
        WebServiceConnection connection = null;
        try {
            connection = getMessageSender().createConnection();
            WebServiceMessage request = messageContext.getRequest();
            if (requestCallback != null) {
                requestCallback.doInMessage(request);
            }
            sendRequest(connection, messageContext);
            TransportInputStream tis = connection.getTransportInputStream();
            if (tis != null) {
                try {
                    messageContext.readResponse(tis);
                    logResponse(messageContext);
                    if (messageContext.hasResponse()) {
                        WebServiceMessage response = messageContext.getResponse();
                        if (!hasFault(connection, messageContext)) {
                            // normal response
                            return responseExtractor.extractData(response);
                        }
                        else {
                            // fault response
                            getFaultResolver().resolveFault(response);
                        }
                    }
                }
                finally {
                    tis.close();
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
        }
    }

    /** Sends the request in the given message context over the connection. */
    private void sendRequest(WebServiceConnection connection, MessageContext messageContext) throws IOException {
        if (logger.isTraceEnabled()) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            messageContext.getRequest().writeTo(os);
            logger.trace("WebServiceTemplate sends request [" + os.toString("UTF-8") + "]");
        }
        else if (logger.isDebugEnabled()) {
            logger.debug("WebServiceTemplate sends request [" + messageContext.getRequest() + "]");
        }
        TransportOutputStream tos = connection.getTransportOutputStream();
        try {
            messageContext.getRequest().writeTo(tos);
            tos.flush();
        }
        finally {
            tos.close();
        }
    }

    /** Determines whether the given connection or message context has a fault. */
    private boolean hasFault(WebServiceConnection connection, MessageContext messageContext) throws IOException {
        if (connection instanceof FaultAwareWebServiceConnection) {
            return ((FaultAwareWebServiceConnection) connection).hasFault();
        }
        else {
            return messageContext.getResponse().hasFault();
        }
    }

    private void logResponse(MessageContext messageContext) throws IOException {
        if (messageContext.hasResponse()) {
            if (logger.isTraceEnabled()) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                messageContext.getResponse().writeTo(os);
                logger.trace("WebServiceTemplate receives response [" + os.toString("UTF-8") + "]");
            }
            else if (logger.isDebugEnabled()) {
                logger.debug("WebServiceTemplate receives response [" + messageContext.getRequest() + "]");
            }
        }
        else if (logger.isDebugEnabled()) {
            logger.debug("WebServiceTemplate receives no response for request [" + messageContext.getRequest() + "]");
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
