/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.soap;

import javax.xml.namespace.QName;

import org.springframework.util.ObjectUtils;
import org.springframework.ws.EndpointInterceptor;
import org.springframework.ws.EndpointInvocationChain;
import org.springframework.ws.MessageDispatcher;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.context.SoapMessageContext;
import org.w3c.dom.Element;

/**
 * SOAP-specific subclass of the <code>MessageDispatcher</code>. Adds functionality for adding actor roles to a endpoint
 * invocation chain, and endpoint interception using <code>SoapEndpointInterceptor</code>s.
 *
 * @author Arjen Poutsma
 * @see SoapMessage
 * @see SoapEndpointInterceptor
 */
public class SoapMessageDispatcher extends MessageDispatcher {

    /**
     * Process the MustUnderstand headers in the incoming SOAP request message. Iterates over all SOAP headers which
     * should be understood, and determines whether these are supported. Generates a SOAP MustUnderstand fault if a
     * header is not understood.
     *
     * @param mappedEndpoint the mapped EndpointInvocationChain
     * @param messageContext the message context
     * @return <code>true</code> if all necessary headers are understood; <code>false</code> otherwise
     * @see SoapEndpointInvocationChain#getRoles()
     * @see SoapMessage#getMustUnderstandHeaderElements(String)
     */
    protected boolean handleRequest(EndpointInvocationChain mappedEndpoint, MessageContext messageContext) {
        if (mappedEndpoint instanceof SoapEndpointInvocationChain && messageContext instanceof SoapMessageContext) {
            SoapMessageContext soapContext = (SoapMessageContext) messageContext;
            SoapMessage soapRequest = soapContext.getSoapRequest();
            SoapEndpointInvocationChain mappedSoapEndpoint = (SoapEndpointInvocationChain) mappedEndpoint;
            for (int i = 0; i < mappedSoapEndpoint.getRoles().length; i++) {
                String role = mappedSoapEndpoint.getRoles()[i];
                Element[] mustUnderstandHeaders = soapRequest.getMustUnderstandHeaderElements(role);
                for (int j = 0; j < mustUnderstandHeaders.length; j++) {
                    Element mustUnderstandHeader = mustUnderstandHeaders[j];
                    boolean understood = false;
                    for (int k = 0; k < mappedSoapEndpoint.getInterceptors().length; k++) {
                        EndpointInterceptor interceptor = mappedSoapEndpoint.getInterceptors()[k];
                        if (interceptor instanceof SoapEndpointInterceptor &&
                                ((SoapEndpointInterceptor) interceptor).understands(mustUnderstandHeader)) {
                            understood = true;
                        }
                    }
                    if (!understood) {
                        SoapMessage response = (SoapMessage) soapContext.createSoapResponse();
                        response.addFault(new QName("MustUnderstand"), "Mandatory Header error.", role);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Trigger handleResponse or handleFault on the mapped EndpointInterceptors. Will just invoke said method on all
     * interceptors whose handleRequest invocation returned <code>true</code>, in addition to the last interceptor who
     * returned <code>false</code>.
     *
     * @param mappedEndpoint   the mapped EndpointInvocationChain
     * @param interceptorIndex index of last interceptor that was called
     * @param messageContext   the message context, whose request and response are filled
     * @see org.springframework.ws.EndpointInterceptor#handleResponse(org.springframework.ws.context.MessageContext,
     *      Object)
     */
    protected void triggerHandleResponse(EndpointInvocationChain mappedEndpoint,
                                         int interceptorIndex,
                                         MessageContext messageContext) throws Exception {
        if (mappedEndpoint != null && messageContext.getResponse() != null &&
                !ObjectUtils.isEmpty(mappedEndpoint.getInterceptors())) {
            boolean hasFault = false;
            if (((SoapMessage) messageContext.getResponse()).getFault() != null) {
                hasFault = true;
            }
            boolean resume = true;
            for (int i = interceptorIndex; resume && i >= 0; i--) {
                EndpointInterceptor interceptor = mappedEndpoint.getInterceptors()[i];
                boolean isSoapEndpointInterceptor = interceptor instanceof SoapEndpointInterceptor;
                if (!hasFault || !isSoapEndpointInterceptor) {
                    resume = interceptor.handleResponse(messageContext, mappedEndpoint.getEndpoint());
                }
                else if (isSoapEndpointInterceptor) {
                    resume = ((SoapEndpointInterceptor) interceptor)
                            .handleFault(messageContext, mappedEndpoint.getEndpoint());
                }
            }
        }
    }
}
