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
import java.net.URI;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.WebServiceTransformerException;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.support.WebServiceAccessor;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.client.core.SoapFaultMessageResolver;
import org.springframework.ws.support.DefaultStrategiesHelper;
import org.springframework.ws.support.MarshallingUtils;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.TransportException;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;
import org.springframework.ws.transport.support.TransportUtils;

/**
 * <strong>The central class for client-side Web services.</strong> It provides a message-driven approach to sending and
 * receiving {@link WebServiceMessage} instances.
 * <p/>
 * Code using this class need only implement callback interfaces, provide {@link Source} objects to read data from, or
 * use the pluggable {@link Marshaller} support. For invoking the {@link #marshalSendAndReceive marshalling methods},
 * the {@link #setMarshaller(Marshaller) marshaller} and {@link #setUnmarshaller(Unmarshaller) unmarshaller} properties
 * must be set.
 * <p/>
 * This template uses a {@link SoapFaultMessageResolver} to handle fault response messages. Another {@link
 * FaultMessageResolver} can be defined with with {@link #setFaultMessageResolver(FaultMessageResolver)
 * faultMessageResolver} property. If this property is set to <code>null</code>, no fault resolving is performed.
 * <p/>
 * This template uses the following algorithm for sending and receiving. <ol> <li>Call to {@link #createConnection(URI)
 * createConnection()}.</li> <li>Call to {@link WebServiceMessageFactory#createWebServiceMessage()
 * createWebServiceMessage()} on the registered message factory to create a request message.</li> <li>Invoke {@link
 * WebServiceMessageCallback#doWithMessage(WebServiceMessage) doWithMessage()} on the request callback, if any. This
 * step stores content in the request message, based on <code>Source</code>, marshalling, etc.</li> <li>Call {@link
 * WebServiceConnection#send(WebServiceMessage) send()} on the connection.</li> <li>Call {@link
 * #hasError(WebServiceConnection,WebServiceMessage) hasError()} to check if the connection has an error. For an HTTP
 * transport, a status code other than <code>2xx</code> indicates an error. However, since a status code of 500 can also
 * indicate a SOAP fault, the template verifies whether the error is not a fault.</li> <ul> <li>If the connection has an
 * error, call the {@link #handleError handleError()} method, which by default throws a {@link
 * WebServiceTransportException}.</li> <li>If the connection has no error, continue with the next step. </ul> <li>Invoke
 * {@link WebServiceConnection#receive(WebServiceMessageFactory) receive} on the connection to read the response
 * message, if any.</li> <ul> <li>If no response was received, return <code>null</code> or <code>false</code></li>
 * <li>Call {@link #hasFault(WebServiceConnection,WebServiceMessage) hasFault()} to determine whether the response has a
 * fault. If it has, call the {@link #handleFault handleFault()} method.</li> <li>Otherwise, invoke {@link
 * WebServiceMessageExtractor#extractData(WebServiceMessage) extractData()} on the response extractor, or {@link
 * WebServiceMessageCallback#doWithMessage(WebServiceMessage) doWithMessage} on the response callback.</li> </ul>
 * <li>Call to {@link WebServiceConnection#close() close} on the connection.</li> </ol>
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class WebServiceTemplate extends WebServiceAccessor implements WebServiceOperations {

    /** Log category to use for message tracing. */
    public static final String MESSAGE_TRACING_LOG_CATEGORY = "org.springframework.ws.client.MessageTracing";

    /** Additional logger to use for message tracing. */
    protected static final Log messageTracingLogger =
            LogFactory.getLog(WebServiceTemplate.MESSAGE_TRACING_LOG_CATEGORY);

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    private FaultMessageResolver faultMessageResolver;

    private String defaultUri;

    private boolean checkConnectionForFault = true;

    private ClientInterceptor[] interceptors;

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
        setMessageFactory(messageFactory);
        initDefaultStrategies();
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
     * @see #sendSourceAndReceiveToResult(Source,Result)
     * @see #sendSourceAndReceiveToResult(Source,WebServiceMessageCallback,Result)
     * @see #sendSourceAndReceive(Source,SourceExtractor)
     * @see #sendSourceAndReceive(Source,WebServiceMessageCallback,SourceExtractor)
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
     * Indicates whether the {@link FaultAwareWebServiceConnection#hasFault() connection} should be checked for fault
     * indicators (<code>true</code>), or whether we should rely on the {@link FaultAwareWebServiceMessage#hasFault()
     * message} only (<code>false</code>). The default is <code>true</code>.
     * <p/>
     * When using a HTTP transport, this property defines whether to check the HTTP response status code for fault
     * indicators. Both the SOAP specification and the WS-I Basic Profile define that a Web service must return a "500
     * Internal Server Error" HTTP status code if the response envelope is a Fault. Setting this property to
     * <code>false</code> allows this template to deal with non-conformant services.
     *
     * @see #hasFault(WebServiceConnection,WebServiceMessage)
     * @see <a href="http://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383529">SOAP 1.1 specification</a>
     * @see <a href="http://www.ws-i.org/Profiles/BasicProfile-1.1.html#HTTP_Server_Error_Status_Codes">WS-I Basic
     *      Profile</a>
     */
    public void setCheckConnectionForFault(boolean checkConnectionForFault) {
        this.checkConnectionForFault = checkConnectionForFault;
    }

    /**
     * Returns the client interceptors to apply to all web service invocations made by this template.
     *
     * @return array of endpoint interceptors, or <code>null</code> if none
     */
    public ClientInterceptor[] getInterceptors() {
        return interceptors;
    }

    /**
     * Sets the client interceptors to apply to all web service invocations made by this template.
     *
     * @param interceptors array of endpoint interceptors, or <code>null</code> if none
     */
    public final void setInterceptors(ClientInterceptor[] interceptors) {
        this.interceptors = interceptors;
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

    //
    // Marshalling methods
    //

    public Object marshalSendAndReceive(final Object requestPayload) {
        return marshalSendAndReceive(requestPayload, null);
    }

    public Object marshalSendAndReceive(String uri, final Object requestPayload) {
        return marshalSendAndReceive(uri, requestPayload, null);
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

            public void doWithMessage(WebServiceMessage request) throws IOException, TransformerException {
                MarshallingUtils.marshal(getMarshaller(), requestPayload, request);
                if (requestCallback != null) {
                    requestCallback.doWithMessage(request);
                }
            }
        }, new WebServiceMessageExtractor() {

            public Object extractData(WebServiceMessage response) throws IOException {
                return MarshallingUtils.unmarshal(getUnmarshaller(), response);
            }
        });
    }

    //
    // Result-handling methods
    //

    public boolean sendSourceAndReceiveToResult(Source requestPayload, Result responseResult) {
        return sendSourceAndReceiveToResult(requestPayload, null, responseResult);
    }

    public boolean sendSourceAndReceiveToResult(String uri, Source requestPayload, Result responseResult) {
        return sendSourceAndReceiveToResult(uri, requestPayload, null, responseResult);
    }

    public boolean sendSourceAndReceiveToResult(Source requestPayload,
                                                WebServiceMessageCallback requestCallback,
                                                final Result responseResult) {
        return sendSourceAndReceiveToResult(getDefaultUri(), requestPayload, requestCallback, responseResult);
    }

    public boolean sendSourceAndReceiveToResult(String uri,
                                                Source requestPayload,
                                                WebServiceMessageCallback requestCallback,
                                                final Result responseResult) {
        try {
            final Transformer transformer = createTransformer();
            Boolean retVal = (Boolean) doSendAndReceive(uri, transformer, requestPayload, requestCallback,
                    new SourceExtractor() {

                        public Object extractData(Source source) throws IOException, TransformerException {
                            transformer.transform(source, responseResult);
                            return Boolean.TRUE;
                        }
                    });
            return retVal != null && retVal.booleanValue();
        }
        catch (TransformerConfigurationException ex) {
            throw new WebServiceTransformerException("Could not create transformer", ex);
        }
    }

    //
    // Source-handling methods
    //

    public Object sendSourceAndReceive(final Source requestPayload, final SourceExtractor responseExtractor) {
        return sendSourceAndReceive(requestPayload, null, responseExtractor);
    }

    public Object sendSourceAndReceive(String uri,
                                       final Source requestPayload,
                                       final SourceExtractor responseExtractor) {
        return sendSourceAndReceive(uri, requestPayload, null, responseExtractor);
    }

    public Object sendSourceAndReceive(final Source requestPayload,
                                       final WebServiceMessageCallback requestCallback,
                                       final SourceExtractor responseExtractor) {
        return sendSourceAndReceive(getDefaultUri(), requestPayload, requestCallback, responseExtractor);
    }

    public Object sendSourceAndReceive(String uri,
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
            public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
                transformer.transform(requestPayload, message.getPayloadResult());
                if (requestCallback != null) {
                    requestCallback.doWithMessage(message);
                }
            }
        }, new SourceExtractorMessageExtractor(responseExtractor));
    }

    //
    // WebServiceMessage-handling methods
    //

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

    public Object sendAndReceive(String uriString,
                                 WebServiceMessageCallback requestCallback,
                                 WebServiceMessageExtractor responseExtractor) {
        Assert.notNull(responseExtractor, "'responseExtractor' must not be null");
        Assert.hasLength(uriString, "'uri' must not be empty");
        TransportContext previousTransportContext = TransportContextHolder.getTransportContext();
        WebServiceConnection connection = null;
        try {
            connection = createConnection(URI.create(uriString));
            TransportContextHolder.setTransportContext(new DefaultTransportContext(connection));
            MessageContext messageContext = new DefaultMessageContext(getMessageFactory());

            return doSendAndReceive(messageContext, connection, requestCallback, responseExtractor);
        }
        catch (TransportException ex) {
            throw new WebServiceTransportException("Could not use transport: " + ex.getMessage(), ex);
        }
        catch (IOException ex) {
            throw new WebServiceIOException("I/O error: " + ex.getMessage(), ex);
        }
        finally {
            TransportUtils.closeConnection(connection);
            TransportContextHolder.setTransportContext(previousTransportContext);
        }
    }

    /**
     * Sends and receives a {@link MessageContext}. Sends the {@link MessageContext#getRequest() request message}, and
     * received to the {@link MessageContext#getResponse() repsonse message}. Invocates the defined {@link
     * #setInterceptors(ClientInterceptor[]) interceptors} as part of the process.
     *
     * @param messageContext    the message context
     * @param connection        the connection to use
     * @param requestCallback   the requestCallback to be used for manipulating the request message
     * @param responseExtractor object that will extract results
     * @return an arbitrary result object, as returned by the <code>WebServiceMessageExtractor</code>
     * @throws WebServiceClientException if there is a problem sending or receiving the message
     * @throws IOException               in case of I/O errors
     */
    protected Object doSendAndReceive(MessageContext messageContext,
                                      WebServiceConnection connection,
                                      WebServiceMessageCallback requestCallback,
                                      WebServiceMessageExtractor responseExtractor) throws IOException {
        try {
            if (requestCallback != null) {
                requestCallback.doWithMessage(messageContext.getRequest());
            }
            // Apply handleRequest of registered interceptors
            int interceptorIndex = -1;
            if (interceptors != null) {
                for (int i = 0; i < interceptors.length; i++) {
                    interceptorIndex = i;
                    if (!interceptors[i].handleRequest(messageContext)) {
                        break;
                    }
                }
            }
            // if an interceptor has set a response, we don't send/receive
            if (!messageContext.hasResponse()) {
                sendRequest(connection, messageContext.getRequest());
                if (hasError(connection, messageContext.getRequest())) {
                    return handleError(connection, messageContext.getRequest());
                }
                WebServiceMessage response = connection.receive(getMessageFactory());
                messageContext.setResponse(response);
            }
            logResponse(messageContext);
            triggerHandleResponse(interceptorIndex, messageContext);
            if (messageContext.hasResponse()) {
                if (hasFault(connection, messageContext.getResponse())) {
                    return handleFault(connection, messageContext.getRequest(), messageContext.getResponse());
                }
                else {
                    return responseExtractor.extractData(messageContext.getResponse());
                }
            }
            else {
                return null;
            }
        }
        catch (TransformerException ex) {
            throw new WebServiceTransformerException("Transformation error: " + ex.getMessage(), ex);
        }
    }

    /**
     * Determines whether the given connection or message context has an error.
     * <p/>
     * This implementation checks the {@link WebServiceConnection#hasError() connection} first. If it indicates an
     * error, it makes sure that it is not a {@link FaultAwareWebServiceConnection#hasFault() fault}.
     *
     * @param connection the connection (possibly a {@link FaultAwareWebServiceConnection}
     * @param request    the response message (possibly a {@link FaultAwareWebServiceMessage}
     * @return <code>true</code> if the connection has an error; <code>false</code> otherwise
     * @throws IOException in case of I/O errors
     */
    protected boolean hasError(WebServiceConnection connection, WebServiceMessage request) throws IOException {
        if (connection.hasError()) {
            // this could be a fault rather than an error
            if (connection instanceof FaultAwareWebServiceConnection) {
                FaultAwareWebServiceConnection faultConnection = (FaultAwareWebServiceConnection) connection;
                if (faultConnection.hasFault() && request instanceof FaultAwareWebServiceMessage) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Handles an error on the given connection. The default implementation throws a {@link
     * WebServiceTransportException}.
     *
     * @param connection the erronous connection
     * @param request    the corresponding request message
     * @return the object to be returned from {@link #sendAndReceive(String,WebServiceMessageCallback,
     *         WebServiceMessageExtractor)}, if any
     */
    protected Object handleError(WebServiceConnection connection, WebServiceMessage request) throws IOException {
        throw new WebServiceTransportException(connection.getErrorMessage());
    }

    /**
     * Determines whether the given connection or message has a fault.
     * <p/>
     * This implementation checks the {@link FaultAwareWebServiceConnection#hasFault() connection} if the {@link
     * #setCheckConnectionForFault(boolean) checkConnectionForFault} property is true, and defaults to the {@link
     * FaultAwareWebServiceMessage#hasFault() message} otherwise.
     *
     * @param connection the connection (possibly a {@link FaultAwareWebServiceConnection}
     * @param response   the response message (possibly a {@link FaultAwareWebServiceMessage}
     * @return <code>true</code> if either the connection or the message has a fault; <code>false</code> otherwise
     * @throws IOException in case of I/O errors
     */
    protected boolean hasFault(WebServiceConnection connection, WebServiceMessage response) throws IOException {
        if (checkConnectionForFault && connection instanceof FaultAwareWebServiceConnection) {
            // check whether the connection has a fault (i.e. status code 500 in HTTP)
            FaultAwareWebServiceConnection faultConnection = (FaultAwareWebServiceConnection) connection;
            if (!faultConnection.hasFault()) {
                return false;
            }
        }
        if (response instanceof FaultAwareWebServiceMessage) {
            // either the connection has a fault, or checkConnectionForFault is false: let's verify the fault
            FaultAwareWebServiceMessage faultMessage = (FaultAwareWebServiceMessage) response;
            return faultMessage.hasFault();
        }
        return false;
    }

    /**
     * Handles an fault in the given response message. The default implementation invokes the {@link
     * FaultMessageResolver fault resolver} if registered, or invokes {@link #handleError(WebServiceConnection,
     * WebServiceMessage)} otherwise.
     *
     * @param connection the erronous connection
     * @param request    the corresponding request message
     * @param response   the fault response message
     * @return the object to be returned from {@link #sendAndReceive(String,WebServiceMessageCallback,
     *         WebServiceMessageExtractor)}, if any
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
        if (messageTracingLogger.isTraceEnabled()) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            request.writeTo(os);
            messageTracingLogger.trace("Sent request [" + os.toString("UTF-8") + "]");
        }
        else if (messageTracingLogger.isDebugEnabled()) {
            messageTracingLogger.debug("Sent request [" + request + "]");
        }
        connection.send(request);
    }

    private void logResponse(MessageContext messageContext) throws IOException {
        if (messageContext.hasResponse()) {
            if (messageTracingLogger.isTraceEnabled()) {
                ByteArrayOutputStream requestStream = new ByteArrayOutputStream();
                messageContext.getRequest().writeTo(requestStream);
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                messageContext.getResponse().writeTo(responseStream);
                messageTracingLogger.trace("Received response [" + responseStream.toString("UTF-8") +
                        "] for request [" + requestStream.toString("UTF-8") + "]");
            }
            else if (messageTracingLogger.isDebugEnabled()) {
                messageTracingLogger.debug("Received response [" + messageContext.getResponse() + "] for request [" +
                        messageContext.getRequest() + "]");
            }
        }
        else {
            if (logger.isDebugEnabled()) {
                messageTracingLogger
                        .debug("Received no response for request [" + messageContext.getRequest() + "]");
            }
        }
    }

    /**
     * Trigger handleResponse or handleFault on the defined ClientInterceptors. Will just invoke said method on all
     * interceptors whose handleRequest invocation returned <code>true</code>, in addition to the last interceptor who
     * returned <code>false</code>.
     *
     * @param interceptorIndex index of last interceptor that was called
     * @param messageContext   the message context, whose request and response are filled
     * @see ClientInterceptor#handleResponse(MessageContext)
     * @see ClientInterceptor#handleFault(MessageContext)
     */
    private void triggerHandleResponse(int interceptorIndex, MessageContext messageContext) {
        if (messageContext.hasResponse() && !ObjectUtils.isEmpty(interceptors)) {
            boolean hasFault = false;
            WebServiceMessage response = messageContext.getResponse();
            if (response instanceof FaultAwareWebServiceMessage) {
                hasFault = ((FaultAwareWebServiceMessage) response).hasFault();
            }
            boolean resume = true;
            for (int i = interceptorIndex; resume && i >= 0; i--) {
                if (!hasFault) {
                    resume = interceptors[i].handleResponse(messageContext);
                }
                else {
                    resume = interceptors[i].handleFault(messageContext);
                }
            }
        }
    }

    /** Adapter to enable use of a WebServiceMessageCallback inside a WebServiceMessageExtractor. */
    private static class WebServiceMessageCallbackMessageExtractor implements WebServiceMessageExtractor {

        private final WebServiceMessageCallback callback;

        public WebServiceMessageCallbackMessageExtractor(WebServiceMessageCallback callback) {
            this.callback = callback;
        }

        public Object extractData(WebServiceMessage message) throws IOException, TransformerException {
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

        public Object extractData(WebServiceMessage message) throws IOException, TransformerException {
            return sourceExtractor.extractData(message.getPayloadSource());
        }
    }


}
