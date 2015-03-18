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

package org.springframework.ws.soap.security.xwss;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSProcessor;
import com.sun.xml.wss.XWSSProcessorFactory;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.WssSoapFaultException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.security.AbstractWsSecurityInterceptor;
import org.springframework.ws.soap.security.WsSecurityValidationException;
import org.springframework.ws.soap.security.callback.CleanupCallback;
import org.springframework.ws.soap.security.xwss.callback.XwssCallbackHandlerChain;

/**
 * WS-Security endpoint interceptor	 that is based on Sun's XML and Web Services Security package (XWSS). This
 * WS-Security implementation is part of the Java Web Services Developer Pack (Java WSDP).
 *
 * <p>This interceptor needs a {@code CallbackHandler} to operate. This handler is used to retrieve certificates,
 * private keys, validate user credentials, etc. Refer to the XWSS Javadoc to learn more about the specific
 * {@code Callback}s fired by XWSS. You can also set multiple handlers, each of which will be used in turn.
 *
 * <p>Additionally, you must define a XWSS policy file by setting {@code policyConfiguration} property. The format of
 * the policy file is documented in the <a href="http://java.sun.com/webservices/docs/1.6/tutorial/doc/XWS-SecurityIntro4.html#wp529900">Java
 * Web Services Tutorial</a>.
 *
 * <p><b>Note</b> that this interceptor depends on SAAJ, and thus requires {@code SaajSoapMessage}s to operate. This
 * means that you must use a {@code SaajSoapMessageFactory} to create the SOAP messages.
 *
 * @author Arjen Poutsma
 * @see #setCallbackHandler(javax.security.auth.callback.CallbackHandler)
 * @see #setPolicyConfiguration(org.springframework.core.io.Resource)
 * @see com.sun.xml.wss.impl.callback.XWSSCallback
 * @see org.springframework.ws.soap.saaj.SaajSoapMessageFactory
 * @see <a href="https://xwss.dev.java.net/">XWSS</a>
 * @since 1.0.0
 */
public class XwsSecurityInterceptor extends AbstractWsSecurityInterceptor implements InitializingBean {

	private XWSSProcessor processor;

	private CallbackHandler callbackHandler;

	private Resource policyConfiguration;

	/**
	 * Sets the handler to resolve XWSS callbacks. Setting either this propery, or {@code callbackHandlers}, is
	 * required.
	 *
	 * @see com.sun.xml.wss.impl.callback.XWSSCallback
	 * @see #setCallbackHandlers(javax.security.auth.callback.CallbackHandler[])
	 */
	public void setCallbackHandler(CallbackHandler callbackHandler) {
		this.callbackHandler = callbackHandler;
	}

	/**
	 * Sets the handlers to resolve XWSS callbacks. Setting either this propery, or {@code callbackHandlers}, is
	 * required.
	 *
	 * @see com.sun.xml.wss.impl.callback.XWSSCallback
	 * @see #setCallbackHandler(javax.security.auth.callback.CallbackHandler)
	 */
	public void setCallbackHandlers(CallbackHandler[] callbackHandler) {
		this.callbackHandler = new XwssCallbackHandlerChain(callbackHandler);
	}

	/** Sets the policy configuration to use for XWSS. Required. */
	public void setPolicyConfiguration(Resource policyConfiguration) {
		this.policyConfiguration = policyConfiguration;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(policyConfiguration, "policyConfiguration is required");
		Assert.isTrue(policyConfiguration.exists(), "policyConfiguration [" + policyConfiguration + "] does not exist");
		Assert.notNull(callbackHandler, "callbackHandler is required");
		XWSSProcessorFactory processorFactory = XWSSProcessorFactory.newInstance();
		InputStream is = null;
		try {
			if (logger.isInfoEnabled()) {
				logger.info("Loading policy configuration from from '" + policyConfiguration + "'");
			}
			is = policyConfiguration.getInputStream();
			processor = processorFactory.createProcessorForSecurityConfiguration(is, callbackHandler);
		}
		finally {
			if (is != null) {
				is.close();
			}
		}
	}

	/**
	 * Secures the given SoapMessage message in accordance with the defined security policy.
	 *
	 * @param soapMessage the message to be secured
	 * @throws XwsSecuritySecurementException in case of errors
	 * @throws IllegalArgumentException		  when soapMessage is not a {@code SaajSoapMessage}
	 */
	@Override
	protected void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
			throws XwsSecuritySecurementException {
		Assert.isTrue(soapMessage instanceof SaajSoapMessage, "XwsSecurityInterceptor requires a SaajSoapMessage. " +
				"Use a SaajSoapMessageFactory to create the SOAP messages.");
		SaajSoapMessage saajSoapMessage = (SaajSoapMessage) soapMessage;
		try {
			ProcessingContext context = processor.createProcessingContext(saajSoapMessage.getSaajMessage());
			SOAPMessage result = processor.secureOutboundMessage(context);
			saajSoapMessage.setSaajMessage(result);
		}
		catch (XWSSecurityException ex) {
			throw new XwsSecuritySecurementException(ex.getMessage(), ex);
		}
		catch (WssSoapFaultException ex) {
			throw new XwsSecurityFaultException(ex.getFaultCode(), ex.getFaultString(), ex.getFaultActor());
		}
	}

	/**
	 * Validates the given SoapMessage message in accordance with the defined security policy.
	 *
	 * @param soapMessage the message to be validated
	 * @throws XwsSecurityValidationException in case of errors
	 * @throws IllegalArgumentException		  when soapMessage is not a {@code SaajSoapMessage}
	 */
	@Override
	protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
			throws WsSecurityValidationException {
		Assert.isTrue(soapMessage instanceof SaajSoapMessage, "XwsSecurityInterceptor requires a SaajSoapMessage. " +
				"Use a SaajSoapMessageFactory to create the SOAP messages.");
		SaajSoapMessage saajSoapMessage = (SaajSoapMessage) soapMessage;
		try {
			ProcessingContext context = processor.createProcessingContext(saajSoapMessage.getSaajMessage());
			SOAPMessage result = processor.verifyInboundMessage(context);
			saajSoapMessage.setSaajMessage(result);
		}
		catch (XWSSecurityException ex) {
			throw new XwsSecurityValidationException(ex.getMessage(), ex);
		}
		catch (WssSoapFaultException ex) {
			throw new XwsSecurityFaultException(ex.getFaultCode(), ex.getFaultString(), ex.getFaultActor());
		}
	}

	private SOAPMessage verifyInboundMessage(ProcessingContext context)
			throws XWSSecurityException {
		try {
			return processor.verifyInboundMessage(context);
		}
		catch (XWSSecurityException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof NullPointerException) {
				StackTraceElement[] stackTrace = cause.getStackTrace();
				if (stackTrace.length >= 1 &&
						Hashtable.class.getName().equals(stackTrace[0].getClassName())) {
					return verifyInboundMessage(context);
				}
			}
			throw ex;
		}
	}

	@Override
	protected void cleanUp() {
		if (callbackHandler != null) {
			try {
				CleanupCallback cleanupCallback = new CleanupCallback();
				callbackHandler.handle(new Callback[]{cleanupCallback});
			}
			catch (IOException ex) {
				logger.warn("Cleanup callback resulted in IOException", ex);
			}
			catch (UnsupportedCallbackException ex) {
				// ignore
			}
		}
	}
}
