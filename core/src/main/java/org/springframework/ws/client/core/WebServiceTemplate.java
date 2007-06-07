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
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.BeanInitializationException;
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
import org.springframework.ws.soap.client.core.SoapFaultMessageResolver;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.TransportException;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;
import org.springframework.ws.transport.support.DefaultStrategiesHelper;

/**
 * <strong>The central class for client-side Web services.</strong> It provides a message-driven approach to sending and
 * receiving {@link WebServiceMessage} instances.
 * <p/>
 * Code using this class need only implement callback interfaces, provide {@link Source} objects to read data from, or
 * use the pluggable {@link Marshaller} support.
 * <p/>
 * This template uses a {@link SoapFaultMessageResolver} to handle fault response messages. Another {@link
 * FaultMessageResolver} can be defined with with {@link #setFaultMessageResolver(FaultMessageResolver)
 * faultMessageResolver}  property. If this property is set to <code>null</code>, no fault resolving is performed.
 *
 * @author Arjen Poutsma
 */
public class WebServiceTemplate extends WebServiceAccessor implements WebServiceOperations {

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    private FaultMessageResolver faultMessageResolver;

    private String defaultUri;

    /** Creates a new <code>WebServiceTemplate</code> using default settings. */
    public WebServiceTemplate() {
        initDefaultStrategies();
    }

    /**
     * Creates a new <code>WebServiceTemplate</code> based on the given message factory.
     *
     * @param messageFactory the message factory to use
     */
    public WebServiceTemplate(WebServiceMessageFactory messageFactory) {
        this();
        setMessageFactory(messageFactory);
        afterPropertiesSet();
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
        defaultUri = uri;
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

    /** Returns the fault message resolver for this template. */
    public FaultMessageResolver getFaultMessageResolver() {
        return faultMessageResolver;
    }

    /**
     * Sets the fault resolver for this template. Default is the {@link SimpleFaultMessageResolver}, but may be set to
     * <code>null</code> to disable fault handling.
     */
    public void setFaultMessageResolver(FaultMessageResolver faultMessageResolver) {
        this.faultMessageResolver = faultMessageResolver;
    }

    /**
     * Initialize the default implementations for the template's strategies: {@link SoapFaultMessageResolver}, {@link
     * org.springframework.ws.soap.saaj.SaajSoapMessageFactory}, and {@link HttpUrlConnectionMessageSender}.
     *
     * @throws BeanInitializationException in case of initalization errors
     * @see #setFaultMessageResolver(FaultMessageResolver)
     * @see #setMessageFactory(WebServiceMessageFactory)
     * @see #setMessageSender(WebServiceMessageSender)
     */
    protected void initDefaultStrategies() {
        Resource resource = new ClassPathResource(ClassUtils.getShortName(getClass()) + ".properties", getClass());
        DefaultStrategiesHelper strategiesHelper = new DefaultStrategiesHelper(resource);
        if (getMessageFactory() == null) {
            initMessageFactory(strategiesHelper);
        }
        if (ObjectUtils.isEmpty(getMessageSenders())) {
            initMessageSenders(strategiesHelper);
        }
        if (getFaultMessageResolver() == null) {
            initFaultMessageResolver(strategiesHelper);
        }
    }

    private void initMessageFactory(DefaultStrategiesHelper helper) throws BeanInitializationException {
        WebServiceMessageFactory messageFactory = (WebServiceMessageFactory) helper
                .getDefaultStrategy(WebServiceMessageFactory.class);
        setMessageFactory(messageFactory);
    }

    private void initMessageSenders(DefaultStrategiesHelper helper) {
        List messageSenders = helper.getDefaultStrategies(WebServiceMessageSender.class);
        setMessageSenders(
                (WebServiceMessageSender[]) messageSenders.toArray(new WebServiceMessageSender[messageSenders.size()]));
    }

    private void initFaultMessageResolver(DefaultStrategiesHelper helper) throws BeanInitializationException {
        FaultMessageResolver faultMessageResolver =
                (FaultMessageResolver) helper.getDefaultStrategy(FaultMessageResolver.class);
        setFaultMessageResolver(faultMessageResolver);
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

            public void doWithMessage(WebServiceMessage message) throws IOException {
                getMarshaller().marshal(requestPayload, message.getPayloadResult());
                if (requestCallback != null) {
                    requestCallback.doWithMessage(message);
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

    public boolean sendAndReceive(Source requestPayload, Result responseResult) {
        return sendAndReceive(requestPayload, null, responseResult);
    }

    public boolean sendAndReceive(String uri, Source requestPayload, Result responseResult) {
        return sendAndReceive(uri, requestPayload, null, responseResult);
    }

    public boolean sendAndReceive(Source requestPayload,
                                  WebServiceMessageCallback requestCallback,
                                  final Result responseResult) {
        return sendAndReceive(getDefaultUri(), requestPayload, requestCallback, responseResult);
    }

    public boolean sendAndReceive(String uri,
                                  Source requestPayload,
                                  WebServiceMessageCallback requestCallback,
                                  final Result responseResult) {
        try {
            final Transformer transformer = createTransformer();
            Boolean retVal = (Boolean) doSendAndReceive(uri, transformer, requestPayload, requestCallback,
                    new SourceExtractor() {

                        public Object extractData(Source source) throws IOException {
                            try {
                                transformer.transform(source, responseResult);
                            }
                            catch (TransformerException ex) {
                                throw new WebServiceTransformerException("Could not transform payload", ex);
                            }
                            return Boolean.TRUE;
                        }
                    });
            return retVal != null && retVal.booleanValue();
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
            public void doWithMessage(WebServiceMessage message) throws IOException {
                try {
                    transformer.transform(requestPayload, message.getPayloadResult());
                    if (requestCallback != null) {
                        requestCallback.doWithMessage(message);
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

    public boolean sendAndReceive(WebServiceMessageCallback requestCallback,
                                  WebServiceMessageCallback responseCallback) {
        return sendAndReceive(getDefaultUri(), requestCallback, responseCallback);
    }

    public boolean sendAndReceive(String uri,
                                  WebServiceMessageCallback requestCallback,
                                  WebServiceMessageCallback responseCallback) {
        Assert.notNull(responseCallback, "responseCallback must not be null");
        Boolean result = (Boolean) sendAndReceive(uri, requestCallback,
                new WebServiceMessageCallbackMessageExtractor(responseCallback));
        return result != null && result.booleanValue();
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
                requestCallback.doWithMessage(request);
            }
            sendRequest(connection, request);
            if (connection.hasError()) {
                return handleError(connection, request);
            }
            WebServiceMessage response = connection.receive(getMessageFactory());
            if (response != null) {
                if (hasFault(connection, response)) {
                    return handleFault(connection, request, response);
                }
                else {
                    logResponse(request, response);
                    return responseExtractor.extractData(response);
                }
            }
            else {
                logger.debug("Received no response for request [" + request + "]");
                return null;
            }
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

    /**
     * Determines whether the given connection or message context has a fault. Default implementation checks whether the
     * connection is a {@link FaultAwareWebServiceConnection}, and calls returns {@link
     * FaultAwareWebServiceConnection#hasFault()} if so. Otherwise, {@link WebServiceMessage#hasFault()} is returned
     * (which required a full message parse).
     *
     * @param connection the connection (possibly a {@link FaultAwareWebServiceConnection}
     * @param response   the response message
     * @return <code>true</code> if either the connection or the message has a fault; <code>false</code> otherwise
     * @throws IOException in case of I/O errors
     */
    protected boolean hasFault(WebServiceConnection connection, WebServiceMessage response) throws IOException {
        if (connection instanceof FaultAwareWebServiceConnection) {
            return ((FaultAwareWebServiceConnection) connection).hasFault();
        }
        else {
            return response.hasFault();
        }
    }

    /**
     * Handles an error on the given connection. The default implementation throws a {@link
     * WebServiceTransportException}.
     *
     * @param connection the erronous connection
     * @param request    the corresponding request message
     * @return the object to be returned from {@link #sendAndReceive(String,WebServiceMessageCallback,
     *WebServiceMessageExtractor)}, if any
     */
    protected Object handleError(WebServiceConnection connection, WebServiceMessage request) throws IOException {
        logger.debug("Received " + connection.getErrorMessage() + " error for request [" + request + "]");
        throw new WebServiceTransportException(connection.getErrorMessage());
    }

    /**
     * Handles an fault in the given response message. The default implementation invokes the {@link
     * FaultMessageResolver fault resolver} if registered, or invokes {@link #handleError(WebServiceConnection,
     *WebServiceMessage)} otherwise.
     *
     * @param connection the erronous connection
     * @param request    the corresponding request message
     * @param response   the fault response message
     * @return the object to be returned from {@link #sendAndReceive(String,WebServiceMessageCallback,
     *WebServiceMessageExtractor)}, if any
     */
    protected Object handleFault(WebServiceConnection connection, WebServiceMessage request, WebServiceMessage response)
            throws IOException {
        if (getFaultMessageResolver() != null) {
            logger.debug("Received Fault message for request [" + request + "]");
            getFaultMessageResolver().resolveFault(response);
            return null;
        }
        else {
            return handleError(connection, request);
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

    private void logResponse(WebServiceMessage request, WebServiceMessage response) throws IOException {
        if (logger.isTraceEnabled()) {
            ByteArrayOutputStream requestStream = new ByteArrayOutputStream();
            request.writeTo(requestStream);
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            response.writeTo(responseStream);
            logger.trace("Received response [" + responseStream.toString("UTF-8") + "] for request [" +
                    requestStream.toString("UTF-8") + "]");
        }
        else if (logger.isDebugEnabled()) {
            logger.debug("Received response [" + response + "] for request [" + request + "]");
        }
    }

    /** Adapter to enable use of a WebServiceMessageCallback inside a WebServiceMessageExtractor. */
    private static class WebServiceMessageCallbackMessageExtractor implements WebServiceMessageExtractor {

        private final WebServiceMessageCallback callback;

        public WebServiceMessageCallbackMessageExtractor(WebServiceMessageCallback callback) {
            this.callback = callback;
        }

        public Object extractData(WebServiceMessage message) throws IOException {
            callback.doWithMessage(message);
            return Boolean.TRUE;
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
