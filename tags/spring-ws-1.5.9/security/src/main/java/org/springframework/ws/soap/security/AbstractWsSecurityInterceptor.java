/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.ws.soap.security;

import java.util.Locale;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointExceptionResolver;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;
import org.springframework.ws.soap.soap11.Soap11Body;

/**
 * Interceptor base class for interceptors that handle WS-Security. Can be used on the server side, registered in a
 * {@link org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping#setInterceptors(org.springframework.ws.server.EndpointInterceptor[])
 * endpoint mapping}; or on the client side, on the {@link org.springframework.ws.client.core.WebServiceTemplate#setInterceptors(ClientInterceptor[])
 * web service template}.
 * <p/>
 * Subclasses of this base class can be configured to secure incoming and secure outgoing messages. By default, both are
 * on.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractWsSecurityInterceptor implements SoapEndpointInterceptor, ClientInterceptor {

    /** Logger available to subclasses. */
    protected final Log logger = LogFactory.getLog(getClass());

    protected static final QName WS_SECURITY_NAME =
            new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");

    private boolean secureResponse = true;

    private boolean validateRequest = true;

    private boolean secureRequest = true;

    private boolean validateResponse = true;

    private EndpointExceptionResolver exceptionResolver;

    /** Indicates whether server-side incoming request are to be validated. Defaults to <code>true</code>. */
    public void setValidateRequest(boolean validateRequest) {
        this.validateRequest = validateRequest;
    }

    /** Indicates whether server-side outgoing responses are to be secured. Defaults to <code>true</code>. */
    public void setSecureResponse(boolean secureResponse) {
        this.secureResponse = secureResponse;
    }

    /** Indicates whether client-side outgoing requests are to be secured. Defaults to <code>true</code>. */
    public void setSecureRequest(boolean secureRequest) {
        this.secureRequest = secureRequest;
    }

    /** Indicates whether client-side incoming responses are to be validated. Defaults to <code>true</code>. */
    public void setValidateResponse(boolean validateResponse) {
        this.validateResponse = validateResponse;
    }

    /** Provide an {@link EndpointExceptionResolver} for resolving validation exceptions. */
    public void setExceptionResolver(EndpointExceptionResolver exceptionResolver) {
        this.exceptionResolver = exceptionResolver;
    }

    /*
     * Server-side
     */

    /**
     * Validates a server-side incoming request. Delegates to {@link #validateMessage(org.springframework.ws.soap.SoapMessage,org.springframework.ws.context.MessageContext)}
     * if the {@link #setValidateRequest(boolean) validateRequest} property is <code>true</code>.
     *
     * @param messageContext the message context, containing the request to be validated
     * @param endpoint       chosen endpoint to invoke
     * @return <code>true</code> if the request was valid; <code>false</code> otherwise.
     * @throws Exception in case of errors
     * @see #validateMessage(org.springframework.ws.soap.SoapMessage,org.springframework.ws.context.MessageContext)
     */
    public final boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        if (validateRequest) {
            Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest());
            try {
                validateMessage((SoapMessage) messageContext.getRequest(), messageContext);
                return true;
            }
            catch (WsSecurityValidationException ex) {
                return handleValidationException(ex, messageContext);
            }
            catch (WsSecurityFaultException ex) {
                return handleFaultException(ex, messageContext);
            }
        }
        else {
            return true;
        }
    }

    /**
     * Secures a server-side outgoing response. Delegates to {@link #secureMessage(org.springframework.ws.soap.SoapMessage,org.springframework.ws.context.MessageContext)}
     * if the {@link #setSecureResponse(boolean) secureResponse} property is <code>true</code>.
     *
     * @param messageContext the message context, containing the response to be secured
     * @param endpoint       chosen endpoint to invoke
     * @return <code>true</code> if the response was secured; <code>false</code> otherwise.
     * @throws Exception in case of errors
     * @see #secureMessage(org.springframework.ws.soap.SoapMessage,org.springframework.ws.context.MessageContext)
     */
    public final boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        boolean result = true;
        try {
            if (secureResponse) {
                Assert.isTrue(messageContext.hasResponse(), "MessageContext contains no response");
                Assert.isInstanceOf(SoapMessage.class, messageContext.getResponse());
                try {
                    secureMessage((SoapMessage) messageContext.getResponse(), messageContext);
                }
                catch (WsSecuritySecurementException ex) {
                    result = handleSecurementException(ex, messageContext);
                }
                catch (WsSecurityFaultException ex) {
                    result = handleFaultException(ex, messageContext);
                }
            }
        }
        finally {
            if (!result) {
                messageContext.clearResponse();
            }
            cleanUp();
        }
        return result;
    }

    /** Returns <code>true</code>, i.e. fault responses are not secured. */
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        cleanUp();
        return true;
    }

    public boolean understands(SoapHeaderElement headerElement) {
        return WS_SECURITY_NAME.equals(headerElement.getName());
    }

    /*
     * Client-side
     */

    /**
     * Secures a client-side outgoing request. Delegates to {@link #secureMessage(org.springframework.ws.soap.SoapMessage,org.springframework.ws.context.MessageContext)}
     * if the {@link #setSecureRequest(boolean) secureRequest} property is <code>true</code>.
     *
     * @param messageContext the message context, containing the request to be secured
     * @return <code>true</code> if the response was secured; <code>false</code> otherwise.
     * @throws Exception in case of errors
     * @see #secureMessage(org.springframework.ws.soap.SoapMessage,org.springframework.ws.context.MessageContext)
     */
    public final boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        if (secureRequest) {
            Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest());
            try {
                secureMessage((SoapMessage) messageContext.getRequest(), messageContext);
                return true;
            }
            catch (WsSecuritySecurementException ex) {
                return handleSecurementException(ex, messageContext);
            }
            catch (WsSecurityFaultException ex) {
                return handleFaultException(ex, messageContext);
            }
        }
        else {
            return true;
        }
    }

    /**
     * Validates a client-side incoming response. Delegates to {@link #validateMessage(org.springframework.ws.soap.SoapMessage,org.springframework.ws.context.MessageContext)}
     * if the {@link #setValidateResponse(boolean) validateResponse} property is <code>true</code>.
     *
     * @param messageContext the message context, containing the response to be validated
     * @return <code>true</code> if the request was valid; <code>false</code> otherwise.
     * @throws Exception in case of errors
     * @see #validateMessage(org.springframework.ws.soap.SoapMessage,org.springframework.ws.context.MessageContext)
     */
    public final boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        if (validateResponse) {
            Assert.isTrue(messageContext.hasResponse(), "MessageContext contains no response");
            Assert.isInstanceOf(SoapMessage.class, messageContext.getResponse());
            try {
                validateMessage((SoapMessage) messageContext.getResponse(), messageContext);
                return true;
            }
            catch (WsSecurityValidationException ex) {
                return handleValidationException(ex, messageContext);
            }
            catch (WsSecurityFaultException ex) {
                return handleFaultException(ex, messageContext);
            }
        }
        else {
            return true;
        }
    }

    /** Returns <code>true</code>, i.e. fault responses are not validated. */
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    /**
     * Handles an securement exception. Default implementation logs the given exception, and returns
     * <code>false</code>.
     *
     * @param ex             the validation exception
     * @param messageContext the message context
     * @return <code>true</code> to continue processing the message, <code>false</code> (the default) otherwise
     */
    protected boolean handleSecurementException(WsSecuritySecurementException ex, MessageContext messageContext) {
        if (logger.isErrorEnabled()) {
            logger.error("Could not secure response: " + ex.getMessage(), ex);
        }
        return false;
    }

    /**
     * Handles an invalid SOAP message. Default implementation logs the given exception, delegates to the set {@link
     * #setExceptionResolver(EndpointExceptionResolver) exceptionResolver} if any, or creates a SOAP 1.1 Client or SOAP
     * 1.2 Sender Fault with the exception message as fault string, and returns <code>false</code>.
     *
     * @param ex             the validation exception
     * @param messageContext the message context
     * @return <code>true</code> to continue processing the message, <code>false</code> (the default) otherwise
     */
    protected boolean handleValidationException(WsSecurityValidationException ex, MessageContext messageContext) {
        if (logger.isWarnEnabled()) {
            logger.warn("Could not validate request: " + ex.getMessage());
        }
        if (exceptionResolver != null) {
            exceptionResolver.resolveException(messageContext, null, ex);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("No exception resolver present, creating basic soap fault");
            }
            SoapBody response = ((SoapMessage) messageContext.getResponse()).getSoapBody();
            response.addClientOrSenderFault(ex.getMessage(), Locale.ENGLISH);
        }
        return false;
    }

    /**
     * Handles a fault exception.Default implementation logs the given exception, and creates a SOAP Fault with the
     * properties of the given exception, and returns <code>false</code>.
     *
     * @param ex             the validation exception
     * @param messageContext the message context
     * @return <code>true</code> to continue processing the message, <code>false</code> (the default) otherwise
     */
    protected boolean handleFaultException(WsSecurityFaultException ex, MessageContext messageContext) {
        if (logger.isWarnEnabled()) {
            logger.warn("Could not handle request: " + ex.getMessage());
        }
        SoapBody response = ((SoapMessage) messageContext.getResponse()).getSoapBody();
        SoapFault fault;
        if (response instanceof Soap11Body) {
            fault = ((Soap11Body) response).addFault(ex.getFaultCode(), ex.getFaultString(), Locale.ENGLISH);
        }
        else {
            fault = response.addClientOrSenderFault(ex.getFaultString(), Locale.ENGLISH);
        }
        fault.setFaultActorOrRole(ex.getFaultActor());
        return false;
    }

    /**
     * Abstract template method. Subclasses are required to validate the request contained in the given {@link
     * SoapMessage}, and replace the original request with the validated version.
     *
     * @param soapMessage the soap message to validate
     * @throws WsSecurityValidationException in case of validation errors
     */
    protected abstract void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
            throws WsSecurityValidationException;

    /**
     * Abstract template method. Subclasses are required to secure the response contained in the given {@link
     * SoapMessage}, and replace the original response with the secured version.
     *
     * @param soapMessage the soap message to secure
     * @throws WsSecuritySecurementException in case of securement errors
     */
    protected abstract void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
            throws WsSecuritySecurementException;

    protected abstract void cleanUp();
}
