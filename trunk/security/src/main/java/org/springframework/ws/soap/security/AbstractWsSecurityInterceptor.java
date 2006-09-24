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
import org.springframework.ws.soap.SoapEndpointInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.context.SoapMessageContext;

/**
 * Interceptor base class for interceptors that handle WS-Security.
 * <p/>
 * Subclasses of this base class can be configured to validate incoming and secure outgoing messages. By default, both
 * are on.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractWsSecurityInterceptor implements SoapEndpointInterceptor {

    /**
     * Logger available to subclasses.
     */
    private final Log logger = LogFactory.getLog(getClass());

    private boolean validateRequest = true;

    private boolean secureResponse = true;

    private static final QName WS_SECURITY_NAME =
            new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");

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
            Assert.isTrue(messageContext instanceof SoapMessageContext,
                    "WsSecurityInterceptor requires a SoapMessageContext");
            SoapMessageContext soapMessageContext = (SoapMessageContext) messageContext;
            try {
                validateRequest(soapMessageContext);
                return true;
            }
            catch (WsSecurityValidationException ex) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Could not validate request: " + ex.getMessage());
                }
                SoapBody response = soapMessageContext.getSoapResponse().getSoapBody();
                response.addClientOrSenderFault(ex.getMessage(), Locale.ENGLISH);
                return false;
            }
        }
        else {
            return true;
        }
    }

    public final boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        if (secureResponse) {
            Assert.isTrue(messageContext instanceof SoapMessageContext,
                    "WsSecurityInterceptor requires a SoapMessageContext");
            SoapMessageContext soapMessageContext = (SoapMessageContext) messageContext;
            try {
                secureResponse(soapMessageContext);
                return true;
            }
            catch (WsSecuritySecurementException ex) {
                if (logger.isErrorEnabled()) {
                    logger.error("Could not secure response: " + ex.getMessage(), ex);
                }
                return false;
            }
        }
        else {
            return true;
        }
    }

    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    /**
     * Abstract template method. Subclasses are required to validate the request contained in the given
     * <code>SoapMessageContext</code>, and replace the original request with the validated version.
     *
     * @param messageContext the soap message context
     * @throws WsSecurityValidationException in case of validation errors
     */
    protected abstract void validateRequest(SoapMessageContext messageContext) throws WsSecurityValidationException;

    /**
     * Abstract template method. Subclasses are required to secure the response contained in the given
     * <code>SoapMessageContext</code>, and replace the original response with the secured version.
     *
     * @param messageContext the soap message context
     * @throws WsSecuritySecurementException in case of securement errors
     */
    protected abstract void secureResponse(SoapMessageContext messageContext) throws WsSecuritySecurementException;

    public boolean understands(SoapHeaderElement headerElement) {
        return WS_SECURITY_NAME.equals(headerElement.getName());
    }
}
