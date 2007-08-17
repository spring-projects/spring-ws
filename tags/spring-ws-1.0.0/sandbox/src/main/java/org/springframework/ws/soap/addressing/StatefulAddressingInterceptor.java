/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.soap.addressing;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.messageid.MessageIdProvider;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;

/** @author Arjen Poutsma */
class StatefulAddressingInterceptor implements SoapEndpointInterceptor {

    private static final Log logger = LogFactory.getLog(StatefulAddressingInterceptor.class);

    private final AddressingHelper helper;

    private final MessageIdProvider messageIdProvider;

    private final MessageAddressingProperties requestMap;

    public StatefulAddressingInterceptor(AddressingHelper helper,
                                         MessageIdProvider messageIdProvider,
                                         MessageAddressingProperties map) {
        this.helper = helper;
        this.messageIdProvider = messageIdProvider;
        this.requestMap = map;
    }

    public boolean understands(SoapHeaderElement header) {
        return helper.understands(header);
    }

    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        if (!requestMap.isValid()) {
            helper.addMessageHeaderRequiredFault((SoapMessage) messageContext.getResponse());
            return false;
        }
        return true;
    }

    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        SoapMessage response = (SoapMessage) messageContext.getResponse();
        return handleReturnMessage(response, requestMap.getReplyTo());
    }

    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        SoapMessage response = (SoapMessage) messageContext.getResponse();
        return handleReturnMessage(response, requestMap.getFaultTo());
    }

    private boolean handleReturnMessage(SoapMessage response, EndpointReference epr) throws TransformerException {
        if (epr == null || helper.hasNoneAddress(epr)) {
            logger.debug("Request has no response address");
            return false;
        }
        String replyMessageId = messageIdProvider.getMessageId(response);
        if (logger.isDebugEnabled()) {
            logger.debug("Generated response MessageID [" + replyMessageId + "]");
        }
        MessageAddressingProperties replyMap = requestMap.getReplyProperties(epr, null, replyMessageId);
        helper.addAddressingHeaders(response, replyMap);
        if (helper.hasAnonymousAddress(epr)) {
            logger.debug("Request has anonymous response address");
            return true;
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending response message to EPR address [" + epr.getAddress() + "]");
            }
            // TODO: send the message            
            throw new UnsupportedOperationException("Sending to out-of-band EPR not supported");
        }
    }


}
