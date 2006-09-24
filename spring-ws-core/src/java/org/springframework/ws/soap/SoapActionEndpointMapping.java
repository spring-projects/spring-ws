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

import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.endpoint.AbstractMapBasedSoapEndpointMapping;

/**
 * Endpoint mapping that resolves the message Soap Action as endpoint key of the SOAP message.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.soap.SoapMessage#getSoapAction()
 */
public class SoapActionEndpointMapping extends AbstractMapBasedSoapEndpointMapping {

    protected boolean validateLookupKey(String key) {
        return StringUtils.hasLength(key);
    }

    protected String getLookupKeyForMessage(WebServiceMessage message) throws Exception {
        String soapAction = ((SoapMessage) message).getSoapAction();
        if (soapAction.charAt(0) == '"' && soapAction.charAt(soapAction.length() - 1) == '"') {
            return soapAction.substring(1, soapAction.length() - 1);
        }
        else {
            return soapAction;
        }
    }
}
