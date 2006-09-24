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

package org.springframework.ws.soap.security.xwss;

import java.io.InputStream;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSProcessor;
import com.sun.xml.wss.XWSSProcessorFactory;
import com.sun.xml.wss.XWSSecurityException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.soap.context.SoapMessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessageContext;
import org.springframework.ws.soap.security.AbstractWsSecurityInterceptor;
import org.springframework.ws.soap.security.xwss.callback.CallbackHandlerChain;

/**
 * WS-Security endpoint interceptor  that is based on Sun's XML and Web Services Security package (XWSS). This
 * WS-Security implementation is part of the Java Web Services Developer Pack (Java WSDP).
 * <p/>
 * This interceptor needs a <code>CallbackHandler</code> to operate. This handler is used to retrieve certificates,
 * private keys, validate user credentials, etc. Refer to the XWSS Javadoc to learn more about the specific
 * <code>Callback</code>s fired by XWSS. You can also set multiple handlers, each of which will be used in turn.
 * <p/>
 * Additionally, you must define a XWSS policy file by setting <code>policyConfiguration</code> property. The format of
 * the policy file is documented in the <a href="http://java.sun.com/webservices/docs/1.6/tutorial/doc/XWS-SecurityIntro4.html#wp529900">Java
 * Web Services Tutorial</a>.
 * <p/>
 * <b>Note</b> that this interceptor depends on SAAJ, and thus requires <code>SaajSoapMessage</code>s to operate. This
 * means that you must use a <code>SaajSoapMessageContextFactory</code> to create the SOAP messages.
 *
 * @author Arjen Poutsma
 * @see #setCallbackHandler(javax.security.auth.callback.CallbackHandler)
 * @see #setPolicyConfiguration(org.springframework.core.io.Resource)
 * @see com.sun.xml.wss.impl.callback.XWSSCallback
 * @see org.springframework.ws.soap.saaj.SaajSoapMessageContextFactory
 * @see <a href="https://xwss.dev.java.net/">XWSS</a>
 */
public class XwsSecurityInterceptor extends AbstractWsSecurityInterceptor implements InitializingBean {

    private static final Log logger = LogFactory.getLog(XwsSecurityInterceptor.class);

    private XWSSProcessor processor;

    private CallbackHandler callbackHandler;

    private Resource policyConfiguration;

    /**
     * Sets the handler to resolve XWSS callbacks. Setting either this propery, or <code>callbackHandlers</code>, is
     * required.
     *
     * @see com.sun.xml.wss.impl.callback.XWSSCallback
     * @see #setCallbackHandlers(javax.security.auth.callback.CallbackHandler[])
     */
    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    /**
     * Sets the handlers to resolve XWSS callbacks. Setting either this propery, or <code>callbackHandlers</code>, is
     * required.
     *
     * @see com.sun.xml.wss.impl.callback.XWSSCallback
     * @see #setCallbackHandler(javax.security.auth.callback.CallbackHandler)
     */
    public void setCallbackHandlers(CallbackHandler[] callbackHandler) {
        this.callbackHandler = new CallbackHandlerChain(callbackHandler);
    }

    /**
     * Sets the policy configuration to use for XWSS. Required.
     */
    public void setPolicyConfiguration(Resource policyConfiguration) {
        this.policyConfiguration = policyConfiguration;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(policyConfiguration, "policyConfiguration is required");
        Assert.notNull(callbackHandler, "callbackHandler is required");
        XWSSProcessorFactory processorFactory = XWSSProcessorFactory.newInstance();
        InputStream is = null;
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Loading policy configuration from from '" + policyConfiguration.getFilename() + "'");
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

    protected void secureResponse(SoapMessageContext soapMessageContext) throws XwsSecuritySecurementException {
        Assert.isTrue(soapMessageContext instanceof SaajSoapMessageContext,
                "XwsSecurityInterceptor requires a SaajSoapMessageContext. " +
                        "Use a SaajSoapMessageContextFactory to create the SOAP messages.");
        SaajSoapMessageContext saajMessageContext = (SaajSoapMessageContext) soapMessageContext;
        SOAPMessage securedMessage = secureMessage(saajMessageContext.getSaajResponse());
        saajMessageContext.setSaajResponse(securedMessage);
    }

    protected void validateRequest(SoapMessageContext soapMessageContext) throws XwsSecurityValidationException {
        Assert.isTrue(soapMessageContext instanceof SaajSoapMessageContext,
                "XwsSecurityInterceptor requires a SaajSoapMessageContext" +
                        "Use a SaajSoapMessageContextFactory to create the SOAP messages.");
        SaajSoapMessageContext saajMessageContext = (SaajSoapMessageContext) soapMessageContext;
        SOAPMessage validatedMessage = validateMessage(saajMessageContext.getSaajRequest());
        saajMessageContext.setSaajRequest(validatedMessage);
    }

    /**
     * Secures the given SAAJ message in accordance with the defined security policy and returns the secured result.
     *
     * @param message the message to be secured
     * @return the secured message
     * @throws XwsSecuritySecurementException in case of errors
     */
    protected SOAPMessage secureMessage(SOAPMessage message) throws XwsSecuritySecurementException {
        try {
            ProcessingContext context = processor.createProcessingContext(message);
            return processor.secureOutboundMessage(context);
        }
        catch (XWSSecurityException ex) {
            throw new XwsSecuritySecurementException(ex.getMessage(), ex);
        }
    }

    /**
     * Validates the given SAAJ message in accordance with the defined security policy and returns the validated
     * result.
     *
     * @param message the message to be validated
     * @return the validated message
     * @throws XwsSecurityValidationException in case of errors
     */
    protected SOAPMessage validateMessage(SOAPMessage message) throws XwsSecurityValidationException {
        try {
            ProcessingContext context = processor.createProcessingContext(message);
            return processor.verifyInboundMessage(context);
        }
        catch (XWSSecurityException ex) {
            throw new XwsSecurityValidationException(ex.getMessage(), ex);
        }
    }
}
