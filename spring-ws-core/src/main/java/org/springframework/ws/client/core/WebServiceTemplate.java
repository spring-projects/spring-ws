/*
 * Copyright 2005-2014 the original author or authors.
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
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.WebServiceTransformerException;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.support.WebServiceAccessor;
import org.springframework.ws.client.support.destination.DestinationProvider;
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
 * <p>
 * Code using this class need only implement callback interfaces, provide {@link Source} objects to read data from, or
 * use the pluggable {@link Marshaller} support. For invoking the {@link #marshalSendAndReceive marshalling methods},
 * the {@link #setMarshaller(Marshaller) marshaller} and {@link #setUnmarshaller(Unmarshaller) unmarshaller} properties
 * must be set.
 * <p>
 * This template uses a {@link SoapFaultMessageResolver} to handle fault response messages. Another
 * {@link FaultMessageResolver} can be defined with with {@link #setFaultMessageResolver(FaultMessageResolver)
 * faultMessageResolver} property. If this property is set to {@code null}, no fault resolving is performed.
 * <p>
 * This template uses the following algorithm for sending and receiving.
 * <ol>
 * <li>Call {@link #createConnection(URI) createConnection()}.</li>
 * <li>Call {@link WebServiceMessageFactory#createWebServiceMessage() createWebServiceMessage()} on the registered
 * message factory to create a request message.</li>
 * <li>Invoke {@link WebServiceMessageCallback#doWithMessage(WebServiceMessage) doWithMessage()} on the request
 * callback, if any. This step stores content in the request message, based on {@code Source}, marshalling, etc.</li>
 * <li>Invoke {@link ClientInterceptor#handleRequest(MessageContext) handleRequest()} on the registered
 * {@link #setInterceptors(ClientInterceptor[]) interceptors}. Interceptors are executed in order. If any of the
 * interceptors creates a response message in the message context, skip to step 7.</li>
 * <li>Call {@link WebServiceConnection#send(WebServiceMessage) send()} on the connection.</li>
 * <li>Call {@link #hasError(WebServiceConnection,WebServiceMessage) hasError()} to check if the connection has an
 * error. For an HTTP transport, a status code other than {@code 2xx} indicates an error. However, since a status code
 * of 500 can also indicate a SOAP fault, the template verifies whether the error is not a fault.</li>
 * <ul>
 * <li>If the connection has an error, call the {@link #handleError handleError()} method, which by default throws a
 * {@link WebServiceTransportException}.</li>
 * <li>If the connection has no error, continue with the next step.</li>
 * </ul>
 * <li>Invoke {@link WebServiceConnection#receive(WebServiceMessageFactory) receive} on the connection to read the
 * response message, if any.</li>
 * <ul>
 * <li>If no response was received, return {@code null} or {@code false}</li>
 * <li>Call {@link #hasFault(WebServiceConnection,WebServiceMessage) hasFault()} to determine whether the response has a
 * fault. If it has, call {@link ClientInterceptor#handleFault(MessageContext)} and the {@link #handleFault
 * handleFault()} method.</li>
 * <li>Otherwise, invoke {@link ClientInterceptor#handleResponse(MessageContext)} and
 * {@link WebServiceMessageExtractor#extractData(WebServiceMessage) extractData()} on the response extractor, or
 * {@link WebServiceMessageCallback#doWithMessage(WebServiceMessage) doWithMessage} on the response callback.</li>
 * </ul>
 * <li>Call to {@link WebServiceConnection#close() close} on the connection.</li>
 * </ol>
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class WebServiceTemplate extends WebServiceAccessor implements WebServiceOperations {

	/** Log category to use for message tracing. */
	public static final String MESSAGE_TRACING_LOG_CATEGORY = "org.springframework.ws.client.MessageTracing";

	/** Additional logger to use for sent message tracing. */
	protected static final Log sentMessageTracingLogger = LogFactory
			.getLog(WebServiceTemplate.MESSAGE_TRACING_LOG_CATEGORY + ".sent");

	/** Additional logger to use for received message tracing. */
	protected static final Log receivedMessageTracingLogger = LogFactory
			.getLog(WebServiceTemplate.MESSAGE_TRACING_LOG_CATEGORY + ".received");

	private Marshaller marshaller;

	private Unmarshaller unmarshaller;

	private FaultMessageResolver faultMessageResolver;

	private boolean checkConnectionForError = true;

	private boolean checkConnectionForFault = true;

	private ClientInterceptor[] interceptors;

	private DestinationProvider destinationProvider;

	/** Creates a new {@code WebServiceTemplate} using default settings. */
	public WebServiceTemplate() {
		initDefaultStrategies();
	}

	/**
	 * Creates a new {@code WebServiceTemplate} based on the given message factory.
	 *
	 * @param messageFactory the message factory to use
	 */
	public WebServiceTemplate(WebServiceMessageFactory messageFactory) {
		setMessageFactory(messageFactory);
		initDefaultStrategies();
	}

	/**
	 * Creates a new {@code WebServiceTemplate} with the given marshaller. If the given {@link Marshaller} also implements
	 * the {@link Unmarshaller} interface, it is used for both marshalling and unmarshalling. Otherwise, an exception is
	 * thrown.
	 * <p>
	 * Note that all {@link Marshaller} implementations in Spring also implement the {@link Unmarshaller} interface, so
	 * that you can safely use this constructor.
	 *
	 * @param marshaller object used as marshaller and unmarshaller
	 * @throws IllegalArgumentException when {@code marshaller} does not implement the {@link Unmarshaller} interface
	 * @since 2.0.3
	 */
	public WebServiceTemplate(Marshaller marshaller) {
		Assert.notNull(marshaller, "marshaller must not be null");
		if (!(marshaller instanceof Unmarshaller)) {
			throw new IllegalArgumentException("Marshaller [" + marshaller + "] does not implement the Unmarshaller "
					+ "interface. Please set an Unmarshaller explicitly by using the "
					+ "WebServiceTemplate(Marshaller, Unmarshaller) constructor.");
		} else {
			this.setMarshaller(marshaller);
			this.setUnmarshaller((Unmarshaller) marshaller);
		}
		initDefaultStrategies();
	}

	/**
	 * Creates a new {@code MarshallingMethodEndpointAdapter} with the given marshaller and unmarshaller.
	 *
	 * @param marshaller the marshaller to use
	 * @param unmarshaller the unmarshaller to use
	 * @since 2.0.3
	 */
	public WebServiceTemplate(Marshaller marshaller, Unmarshaller unmarshaller) {
		Assert.notNull(marshaller, "marshaller must not be null");
		Assert.notNull(unmarshaller, "unmarshaller must not be null");
		this.setMarshaller(marshaller);
		this.setUnmarshaller(unmarshaller);
		initDefaultStrategies();
	}

	/** Returns the default URI to be used on operations that do not have a URI parameter. */
	public String getDefaultUri() {
		if (destinationProvider != null) {
			URI uri = destinationProvider.getDestination();
			return uri != null ? uri.toString() : null;
		} else {
			return null;
		}
	}

	/**
	 * Set the default URI to be used on operations that do not have a URI parameter.
	 * <p>
	 * Typically, either this property is set, or {@link #setDestinationProvider(DestinationProvider)}, but not both.
	 *
	 * @see #marshalSendAndReceive(Object)
	 * @see #marshalSendAndReceive(Object,WebServiceMessageCallback)
	 * @see #sendSourceAndReceiveToResult(Source,Result)
	 * @see #sendSourceAndReceiveToResult(Source,WebServiceMessageCallback,Result)
	 * @see #sendSourceAndReceive(Source,SourceExtractor)
	 * @see #sendSourceAndReceive(Source,WebServiceMessageCallback,SourceExtractor)
	 * @see #sendAndReceive(WebServiceMessageCallback,WebServiceMessageCallback)
	 */
	public void setDefaultUri(final String uri) {
		destinationProvider = new DestinationProvider() {

			public URI getDestination() {
				return URI.create(uri);
			}
		};
	}

	/** Returns the destination provider used on operations that do not have a URI parameter. */
	public DestinationProvider getDestinationProvider() {
		return destinationProvider;
	}

	/**
	 * Set the destination provider URI to be used on operations that do not have a URI parameter.
	 * <p>
	 * Typically, either this property is set, or {@link #setDefaultUri(String)}, but not both.
	 *
	 * @see #marshalSendAndReceive(Object)
	 * @see #marshalSendAndReceive(Object,WebServiceMessageCallback)
	 * @see #sendSourceAndReceiveToResult(Source,Result)
	 * @see #sendSourceAndReceiveToResult(Source,WebServiceMessageCallback,Result)
	 * @see #sendSourceAndReceive(Source,SourceExtractor)
	 * @see #sendSourceAndReceive(Source,WebServiceMessageCallback,SourceExtractor)
	 * @see #sendAndReceive(WebServiceMessageCallback,WebServiceMessageCallback)
	 */
	public void setDestinationProvider(DestinationProvider destinationProvider) {
		this.destinationProvider = destinationProvider;
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
	 * Sets the fault resolver for this template. Default is the
	 * {@link org.springframework.ws.soap.client.core.SoapFaultMessageResolver SoapFaultMessageResolver}, but may be set
	 * to {@code null} to disable fault handling.
	 */
	public void setFaultMessageResolver(FaultMessageResolver faultMessageResolver) {
		this.faultMessageResolver = faultMessageResolver;
	}

	/**
	 * Indicates whether the {@linkplain WebServiceConnection#hasError() connection} should be checked for error
	 * indicators ({@code true}), or whether these should be ignored ({@code false}). The default is {@code true}.
	 * <p>
	 * When using an HTTP transport, this property defines whether to check the HTTP response status code is in the 2xx
	 * Successful range. Both the SOAP specification and the WS-I Basic Profile define that a Web service must return a
	 * "200 OK" or "202 Accepted" HTTP status code for a normal response. Setting this property to {@code false} allows
	 * this template to deal with non-conforming services.
	 *
	 * @see #hasError(WebServiceConnection, WebServiceMessage)
	 * @see <a href="http://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383529">SOAP 1.1 specification</a>
	 * @see <a href="http://www.ws-i.org/Profiles/BasicProfile-1.1.html#HTTP_Success_Status_Codes">WS-I Basic Profile</a>
	 */
	public void setCheckConnectionForError(boolean checkConnectionForError) {
		this.checkConnectionForError = checkConnectionForError;
	}

	/**
	 * Indicates whether the {@linkplain FaultAwareWebServiceConnection#hasFault() connection} should be checked for fault
	 * indicators ({@code true}), or whether we should rely on the {@link FaultAwareWebServiceMessage#hasFault() message}
	 * only ({@code false}). The default is {@code true}.
	 * <p>
	 * When using an HTTP transport, this property defines whether to check the HTTP response status code for fault
	 * indicators. Both the SOAP specification and the WS-I Basic Profile define that a Web service must return a "500
	 * Internal Server Error" HTTP status code if the response envelope is a Fault. Setting this property to {@code false}
	 * allows this template to deal with non-conforming services.
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
	 * @return array of endpoint interceptors, or {@code null} if none
	 */
	public ClientInterceptor[] getInterceptors() {
		return interceptors;
	}

	/**
	 * Sets the client interceptors to apply to all web service invocations made by this template.
	 *
	 * @param interceptors array of endpoint interceptors, or {@code null} if none
	 */
	public final void setInterceptors(ClientInterceptor[] interceptors) {
		this.interceptors = interceptors;
	}

	/**
	 * Initialize the default implementations for the template's strategies: {@link SoapFaultMessageResolver},
	 * {@link org.springframework.ws.soap.saaj.SaajSoapMessageFactory}, and {@link HttpUrlConnectionMessageSender}.
	 *
	 * @throws BeanInitializationException in case of initalization errors
	 * @see #setFaultMessageResolver(FaultMessageResolver)
	 * @see #setMessageFactory(WebServiceMessageFactory)
	 * @see #setMessageSender(WebServiceMessageSender)
	 */
	protected void initDefaultStrategies() {
		DefaultStrategiesHelper strategiesHelper = new DefaultStrategiesHelper(WebServiceTemplate.class);
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
		WebServiceMessageFactory messageFactory = helper.getDefaultStrategy(WebServiceMessageFactory.class);
		setMessageFactory(messageFactory);
	}

	private void initMessageSenders(DefaultStrategiesHelper helper) {
		List<WebServiceMessageSender> messageSenders = helper.getDefaultStrategies(WebServiceMessageSender.class);
		setMessageSenders(messageSenders.toArray(new WebServiceMessageSender[messageSenders.size()]));
	}

	private void initFaultMessageResolver(DefaultStrategiesHelper helper) throws BeanInitializationException {
		FaultMessageResolver faultMessageResolver = helper.getDefaultStrategy(FaultMessageResolver.class);
		setFaultMessageResolver(faultMessageResolver);
	}

	//
	// Marshalling methods
	//

	@Override
	public Object marshalSendAndReceive(final Object requestPayload) {
		return marshalSendAndReceive(requestPayload, null);
	}

	@Override
	public Object marshalSendAndReceive(String uri, final Object requestPayload) {
		return marshalSendAndReceive(uri, requestPayload, null);
	}

	@Override
	public Object marshalSendAndReceive(final Object requestPayload, final WebServiceMessageCallback requestCallback) {
		return marshalSendAndReceive(getDefaultUri(), requestPayload, requestCallback);
	}

	@Override
	public Object marshalSendAndReceive(String uri, final Object requestPayload,
			final WebServiceMessageCallback requestCallback) {
		return sendAndReceive(uri, new WebServiceMessageCallback() {

			public void doWithMessage(WebServiceMessage request) throws IOException, TransformerException {
				if (requestPayload != null) {
					Marshaller marshaller = getMarshaller();
					if (marshaller == null) {
						throw new IllegalStateException("No marshaller registered. Check configuration of WebServiceTemplate.");
					}
					MarshallingUtils.marshal(marshaller, requestPayload, request);
					if (requestCallback != null) {
						requestCallback.doWithMessage(request);
					}
				}
			}
		}, new WebServiceMessageExtractor<Object>() {

			public Object extractData(WebServiceMessage response) throws IOException {
				Unmarshaller unmarshaller = getUnmarshaller();
				if (unmarshaller == null) {
					throw new IllegalStateException("No unmarshaller registered. Check configuration of WebServiceTemplate.");
				}
				return MarshallingUtils.unmarshal(unmarshaller, response);
			}
		});
	}

	//
	// Result-handling methods
	//

	@Override
	public boolean sendSourceAndReceiveToResult(Source requestPayload, Result responseResult) {
		return sendSourceAndReceiveToResult(requestPayload, null, responseResult);
	}

	@Override
	public boolean sendSourceAndReceiveToResult(String uri, Source requestPayload, Result responseResult) {
		return sendSourceAndReceiveToResult(uri, requestPayload, null, responseResult);
	}

	@Override
	public boolean sendSourceAndReceiveToResult(Source requestPayload, WebServiceMessageCallback requestCallback,
			final Result responseResult) {
		return sendSourceAndReceiveToResult(getDefaultUri(), requestPayload, requestCallback, responseResult);
	}

	@Override
	public boolean sendSourceAndReceiveToResult(String uri, Source requestPayload,
			WebServiceMessageCallback requestCallback, final Result responseResult) {
		try {
			final Transformer transformer = createTransformer();
			Boolean retVal = doSendAndReceive(uri, transformer, requestPayload, requestCallback,
					new SourceExtractor<Boolean>() {

						public Boolean extractData(Source source) throws IOException, TransformerException {
							if (source != null) {
								transformer.transform(source, responseResult);
							}
							return Boolean.TRUE;
						}
					});
			return retVal != null && retVal;
		} catch (TransformerConfigurationException ex) {
			throw new WebServiceTransformerException("Could not create transformer", ex);
		}
	}

	//
	// Source-handling methods
	//

	@Override
	public <T> T sendSourceAndReceive(final Source requestPayload, final SourceExtractor<T> responseExtractor) {
		return sendSourceAndReceive(requestPayload, null, responseExtractor);
	}

	@Override
	public <T> T sendSourceAndReceive(String uri, final Source requestPayload,
			final SourceExtractor<T> responseExtractor) {
		return sendSourceAndReceive(uri, requestPayload, null, responseExtractor);
	}

	@Override
	public <T> T sendSourceAndReceive(final Source requestPayload, final WebServiceMessageCallback requestCallback,
			final SourceExtractor<T> responseExtractor) {
		return sendSourceAndReceive(getDefaultUri(), requestPayload, requestCallback, responseExtractor);
	}

	@Override
	public <T> T sendSourceAndReceive(String uri, final Source requestPayload,
			final WebServiceMessageCallback requestCallback, final SourceExtractor<T> responseExtractor) {

		try {
			return doSendAndReceive(uri, createTransformer(), requestPayload, requestCallback, responseExtractor);
		} catch (TransformerConfigurationException ex) {
			throw new WebServiceTransformerException("Could not create transformer", ex);
		}
	}

	private <T> T doSendAndReceive(String uri, final Transformer transformer, final Source requestPayload,
			final WebServiceMessageCallback requestCallback, final SourceExtractor<T> responseExtractor) {
		Assert.notNull(responseExtractor, "responseExtractor must not be null");
		return sendAndReceive(uri, new WebServiceMessageCallback() {
			public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
				transformer.transform(requestPayload, message.getPayloadResult());
				if (requestCallback != null) {
					requestCallback.doWithMessage(message);
				}
			}
		}, new SourceExtractorMessageExtractor<T>(responseExtractor));
	}

	//
	// WebServiceMessage-handling methods
	//

	@Override
	public boolean sendAndReceive(WebServiceMessageCallback requestCallback, WebServiceMessageCallback responseCallback) {
		return sendAndReceive(getDefaultUri(), requestCallback, responseCallback);
	}

	@Override
	public boolean sendAndReceive(String uri, WebServiceMessageCallback requestCallback,
			WebServiceMessageCallback responseCallback) {
		Assert.notNull(responseCallback, "responseCallback must not be null");
		Boolean result = sendAndReceive(uri, requestCallback,
				new WebServiceMessageCallbackMessageExtractor(responseCallback));
		return result != null && result;
	}

	@Override
	public <T> T sendAndReceive(WebServiceMessageCallback requestCallback,
			WebServiceMessageExtractor<T> responseExtractor) {
		return sendAndReceive(getDefaultUri(), requestCallback, responseExtractor);
	}

	@Override
	public <T> T sendAndReceive(String uriString, WebServiceMessageCallback requestCallback,
			WebServiceMessageExtractor<T> responseExtractor) {
		Assert.notNull(responseExtractor, "'responseExtractor' must not be null");
		Assert.hasLength(uriString, "'uri' must not be empty");
		TransportContext previousTransportContext = TransportContextHolder.getTransportContext();
		WebServiceConnection connection = null;
		try {
			connection = createConnection(URI.create(uriString));
			TransportContextHolder.setTransportContext(new DefaultTransportContext(connection));
			MessageContext messageContext = new DefaultMessageContext(getMessageFactory());

			return doSendAndReceive(messageContext, connection, requestCallback, responseExtractor);
		} catch (TransportException ex) {
			throw new WebServiceTransportException("Could not use transport: " + ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new WebServiceIOException("I/O error: " + ex.getMessage(), ex);
		} finally {
			TransportUtils.closeConnection(connection);
			TransportContextHolder.setTransportContext(previousTransportContext);
		}
	}

	/**
	 * Sends and receives a {@link MessageContext}. Sends the {@link MessageContext#getRequest() request message}, and
	 * received to the {@link MessageContext#getResponse() repsonse message}. Invocates the defined
	 * {@link #setInterceptors(ClientInterceptor[]) interceptors} as part of the process.
	 *
	 * @param messageContext the message context
	 * @param connection the connection to use
	 * @param requestCallback the requestCallback to be used for manipulating the request message
	 * @param responseExtractor object that will extract results
	 * @return an arbitrary result object, as returned by the {@code WebServiceMessageExtractor}
	 * @throws WebServiceClientException if there is a problem sending or receiving the message
	 * @throws IOException in case of I/O errors
	 */
	@SuppressWarnings("unchecked")
	protected <T> T doSendAndReceive(MessageContext messageContext, WebServiceConnection connection,
			WebServiceMessageCallback requestCallback, WebServiceMessageExtractor<T> responseExtractor) throws IOException {
		int interceptorIndex = -1;
		try {
			if (requestCallback != null) {
				requestCallback.doWithMessage(messageContext.getRequest());
			}
			// Apply handleRequest of registered interceptors
			boolean intercepted = false;
			if (interceptors != null) {
				for (int i = 0; i < interceptors.length; i++) {
					interceptorIndex = i;
					if (!interceptors[i].handleRequest(messageContext)) {
						intercepted = true;
						break;
					}
				}
			}
			// no send/receive if an interceptor has set a response or if the chain
			// has been interrupted
			if (!messageContext.hasResponse() && !intercepted) {
				sendRequest(connection, messageContext.getRequest());
				if (hasError(connection, messageContext.getRequest())) {
					triggerAfterCompletion(interceptorIndex, messageContext, null);
					return (T) handleError(connection, messageContext.getRequest());
				}
				WebServiceMessage response = connection.receive(getMessageFactory());
				messageContext.setResponse(response);
			}
			logResponse(messageContext);
			if (messageContext.hasResponse()) {
				if (!hasFault(connection, messageContext.getResponse())) {
					triggerHandleResponse(interceptorIndex, messageContext);
					triggerAfterCompletion(interceptorIndex, messageContext, null);
					return responseExtractor.extractData(messageContext.getResponse());
				} else {
					triggerHandleFault(interceptorIndex, messageContext);
					triggerAfterCompletion(interceptorIndex, messageContext, null);
					return (T) handleFault(connection, messageContext);
				}
			} else {
				triggerAfterCompletion(interceptorIndex, messageContext, null);
				return null;
			}
		} catch (TransformerException ex) {
			triggerAfterCompletion(interceptorIndex, messageContext, ex);
			throw new WebServiceTransformerException("Transformation error: " + ex.getMessage(), ex);
		} catch (RuntimeException ex) {
			// Trigger after-completion for thrown exception.
			triggerAfterCompletion(interceptorIndex, messageContext, ex);
			throw ex;
		} catch (IOException ex) {
			// Trigger after-completion for thrown exception.
			triggerAfterCompletion(interceptorIndex, messageContext, ex);
			throw ex;
		}
	}

	/** Sends the request in the given message context over the connection. */
	private void sendRequest(WebServiceConnection connection, WebServiceMessage request) throws IOException {
		if (sentMessageTracingLogger.isTraceEnabled()) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			request.writeTo(os);
			sentMessageTracingLogger.trace("Sent request [" + os.toString("UTF-8") + "]");
		} else if (sentMessageTracingLogger.isDebugEnabled()) {
			sentMessageTracingLogger.debug("Sent request [" + request + "]");
		}
		connection.send(request);
	}

	/**
	 * Determines whether the given connection or message context has an error.
	 * <p>
	 * This implementation checks the {@link WebServiceConnection#hasError() connection} first. If it indicates an error,
	 * it makes sure that it is not a {@link FaultAwareWebServiceConnection#hasFault() fault}.
	 *
	 * @param connection the connection (possibly a {@link FaultAwareWebServiceConnection}
	 * @param request the response message (possibly a {@link FaultAwareWebServiceMessage}
	 * @return {@code true} if the connection has an error; {@code false} otherwise
	 * @throws IOException in case of I/O errors
	 */
	protected boolean hasError(WebServiceConnection connection, WebServiceMessage request) throws IOException {
		if (checkConnectionForError && connection.hasError()) {
			// could be a fault
			if (checkConnectionForFault && connection instanceof FaultAwareWebServiceConnection) {
				FaultAwareWebServiceConnection faultConnection = (FaultAwareWebServiceConnection) connection;
				return !(faultConnection.hasFault() && request instanceof FaultAwareWebServiceMessage);
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles an error on the given connection. The default implementation throws a {@link WebServiceTransportException}.
	 *
	 * @param connection the erroneous connection
	 * @param request the corresponding request message
	 * @return the object to be returned from
	 *         {@link #sendAndReceive(String,WebServiceMessageCallback, WebServiceMessageExtractor)}, if any
	 */
	protected Object handleError(WebServiceConnection connection, WebServiceMessage request) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Received error for request [" + request + "]");
		}
		throw new WebServiceTransportException(connection.getErrorMessage());
	}

	private void logResponse(MessageContext messageContext) throws IOException {
		if (messageContext.hasResponse()) {
			if (receivedMessageTracingLogger.isTraceEnabled()) {
				ByteArrayOutputStream requestStream = new ByteArrayOutputStream();
				messageContext.getRequest().writeTo(requestStream);
				ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
				messageContext.getResponse().writeTo(responseStream);
				receivedMessageTracingLogger.trace("Received response [" + responseStream.toString("UTF-8") + "] for request ["
						+ requestStream.toString("UTF-8") + "]");
			} else if (receivedMessageTracingLogger.isDebugEnabled()) {
				receivedMessageTracingLogger.debug("Received response [" + messageContext.getResponse() + "] for request ["
						+ messageContext.getRequest() + "]");
			}
		} else {
			if (receivedMessageTracingLogger.isDebugEnabled()) {
				receivedMessageTracingLogger.debug("Received no response for request [" + messageContext.getRequest() + "]");
			}
		}
	}

	/**
	 * Determines whether the given connection or message has a fault.
	 * <p>
	 * This implementation checks the {@link FaultAwareWebServiceConnection#hasFault() connection} if the
	 * {@link #setCheckConnectionForFault(boolean) checkConnectionForFault} property is true, and defaults to the
	 * {@link FaultAwareWebServiceMessage#hasFault() message} otherwise.
	 *
	 * @param connection the connection (possibly a {@link FaultAwareWebServiceConnection}
	 * @param response the response message (possibly a {@link FaultAwareWebServiceMessage}
	 * @return {@code true} if either the connection or the message has a fault; {@code false} otherwise
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
	 * Trigger handleResponse on the defined ClientInterceptors. Will just invoke said method on all interceptors whose
	 * handleRequest invocation returned {@code true}, in addition to the last interceptor who returned {@code false}.
	 *
	 * @param interceptorIndex index of last interceptor that was called
	 * @param messageContext the message context, whose request and response are filled
	 * @see ClientInterceptor#handleResponse(MessageContext)
	 * @see ClientInterceptor#handleFault(MessageContext)
	 */
	private void triggerHandleResponse(int interceptorIndex, MessageContext messageContext) {
		if (messageContext.hasResponse() && interceptors != null) {
			for (int i = interceptorIndex; i >= 0; i--) {
				if (!interceptors[i].handleResponse(messageContext)) {
					break;
				}
			}
		}
	}

	/**
	 * Trigger handleFault on the defined ClientInterceptors. Will just invoke said method on all interceptors whose
	 * handleRequest invocation returned {@code true}, in addition to the last interceptor who returned {@code false}.
	 *
	 * @param interceptorIndex index of last interceptor that was called
	 * @param messageContext the message context, whose request and response are filled
	 * @see ClientInterceptor#handleResponse(MessageContext)
	 * @see ClientInterceptor#handleFault(MessageContext)
	 */
	private void triggerHandleFault(int interceptorIndex, MessageContext messageContext) {
		if (messageContext.hasResponse() && interceptors != null) {
			for (int i = interceptorIndex; i >= 0; i--) {
				if (!interceptors[i].handleFault(messageContext)) {
					break;
				}
			}
		}
	}

	/**
	 * Trigger afterCompletion callbacks on the mapped ClientInterceptors. Will just invoke afterCompletion for all
	 * interceptors whose handleRequest invocation has successfully completed and returned true, in addition to the last
	 * interceptor who returned {@code false}.
	 * 
	 * @param interceptorIndex index of last interceptor that successfully completed
	 * @param messageContext the message context
	 * @param ex Exception thrown on handler execution, or {@code null} if none
	 * @see ClientInterceptor#afterCompletion
	 */
	private void triggerAfterCompletion(int interceptorIndex, MessageContext messageContext, Exception ex)
			throws WebServiceClientException {
		if (interceptors != null) {
			for (int i = interceptorIndex; i >= 0; i--) {
				interceptors[i].afterCompletion(messageContext, ex);
			}
		}
	}

	/**
	 * Handles an fault in the given response message. The default implementation invokes the {@link FaultMessageResolver
	 * fault resolver} if registered, or invokes {@link #handleError(WebServiceConnection, WebServiceMessage)} otherwise.
	 *
	 * @param connection the faulty connection
	 * @param messageContext the message context
	 * @return the object to be returned from
	 *         {@link #sendAndReceive(String,WebServiceMessageCallback, WebServiceMessageExtractor)}, if any
	 */
	protected Object handleFault(WebServiceConnection connection, MessageContext messageContext) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Received Fault message for request [" + messageContext.getRequest() + "]");
		}
		if (getFaultMessageResolver() != null) {
			getFaultMessageResolver().resolveFault(messageContext.getResponse());
			return null;
		} else {
			return handleError(connection, messageContext.getRequest());
		}
	}

	/** Adapter to enable use of a WebServiceMessageCallback inside a WebServiceMessageExtractor. */
	private static class WebServiceMessageCallbackMessageExtractor implements WebServiceMessageExtractor<Boolean> {

		private final WebServiceMessageCallback callback;

		private WebServiceMessageCallbackMessageExtractor(WebServiceMessageCallback callback) {
			this.callback = callback;
		}

		@Override
		public Boolean extractData(WebServiceMessage message) throws IOException, TransformerException {
			callback.doWithMessage(message);
			return Boolean.TRUE;
		}
	}

	/** Adapter to enable use of a SourceExtractor inside a WebServiceMessageExtractor. */
	private static class SourceExtractorMessageExtractor<T> implements WebServiceMessageExtractor<T> {

		private final SourceExtractor<T> sourceExtractor;

		private SourceExtractorMessageExtractor(SourceExtractor<T> sourceExtractor) {
			this.sourceExtractor = sourceExtractor;
		}

		@Override
		public T extractData(WebServiceMessage message) throws IOException, TransformerException {
			return sourceExtractor.extractData(message.getPayloadSource());
		}
	}

}
