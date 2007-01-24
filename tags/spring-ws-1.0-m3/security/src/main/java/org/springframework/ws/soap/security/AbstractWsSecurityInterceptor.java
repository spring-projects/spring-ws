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

package org.springframework.ws.soap.security;

import java.util.Locale;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEndpointInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;

/**
 * Interceptor base class for interceptors that handle WS-Security.
 * <p/>
 * Subclasses of this base class can be configured to validate incoming and secure outgoing messages. By default, both
 * are on.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractWsSecurityInterceptor implements SoapEndpointInterceptor {

    private static final QName WS_SECURITY_NAME =
            new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");

    /**
     * Logger available to subclasses.
     */
    protected final Log logger = LogFactory.getLog(getClass());

    private boolean secureResponse = true;

    private boolean validateRequest = true;

    /**
     * Indicates whether outgoing responsed are to be secured. Defaults to <code>true</code>.
     */
    public void setSecureResponse(boolean secureResponse) {
        this.secureResponse = secureResponse;
    }

    /**
     * Indicates whether incoming request are to be validated. Defaults to <code>true</code>.
     */
    public void setValidateRequest(boolean validateRequest) {
        this.validateRequest = validateRequest;
    }

    public final boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        if (validateRequest) {
            Assert.isTrue(messageContext.getRequest() instanceof SoapMessage,
                    "WsSecurityInterceptor requires a SoapMessage request");
            try {
                validateMessage((SoapMessage) messageContext.getRequest());
                return true;
            }
            catch (WsSecurityValidationException ex) {
                return handleValidationException(ex, messageContext);
            }
        }
        else {
            return true;
        }
    }

    public final boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        if (secureResponse) {
            Assert.isTrue(messageContext.getResponse() instanceof SoapMessage,
                    "WsSecurityInterceptor requires a SoapMessage response");
            try {
                secureMessage((SoapMessage) messageContext.getResponse());
                return true;
            }
            catch (WsSecuritySecurementException ex) {
                return handleSecurementException(ex, messageContext);
            }
        }
        else {
            return true;
        }
    }

    /**
     * Returns <code>true</code>, i.e. faults are not secured.
     */
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    public boolean understands(SoapHeaderElement headerElement) {
        return WS_SECURITY_NAME.equals(headerElement.getName());
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
     * Handles an invalid SOAP message. Default implementation logs the given exception, and creates a SOAP 1.1 Client
     * or SOAP 1.2 Sender Fault with the exception message as fault string, and returns <code>false</code>.
     *
     * @param ex             the validation exception
     * @param messageContext the message context
     * @return <code>true</code> to continue processing the message, <code>false</code> (the default) otherwise
     */
    protected boolean handleValidationException(WsSecurityValidationException ex, MessageContext messageContext) {
        if (logger.isWarnEnabled()) {
            logger.warn("Could not validate request: " + ex.getMessage());
        }
        SoapBody response = ((SoapMessage) messageContext.getResponse()).getSoapBody();
        response.addClientOrSenderFault(ex.getMessage(), Locale.ENGLISH);
        return false;
    }

    /**
     * Abstract template method. Subclasses are required to validate the request contained in the given
     * <code>SoapMessageContext</code>, and replace the original request with the validated version.
     *
     * @param soapMessage the soap message to validate
     * @throws WsSecurityValidationException in case of validation errors
     */
    protected abstract void validateMessage(SoapMessage soapMessage) throws WsSecurityValidationException;

    /**
     * Abstract template method. Subclasses are required to secure the response contained in the given
     * <code>SoapMessageContext</code>, and replace the original response with the secured version.
     *
     * @param soapMessage the soap message to secure
     * @throws WsSecuritySecurementException in case of securement errors
     */
    protected abstract void secureMessage(SoapMessage soapMessage) throws WsSecuritySecurementException;
}
