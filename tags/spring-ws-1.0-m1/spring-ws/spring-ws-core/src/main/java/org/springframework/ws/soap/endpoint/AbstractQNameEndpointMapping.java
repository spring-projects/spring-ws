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

package org.springframework.ws.soap.endpoint;

import javax.xml.namespace.QName;

import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Abstract base class for <code>EndpointMapping</code>s that resolve qualified names as registration keys.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractQNameEndpointMapping extends AbstractMapBasedSoapEndpointMapping {

    protected final String getLookupKeyForMessage(WebServiceMessage message) throws Exception {
        QName qName = resolveQName(message);
        return (qName != null) ? qName.toString() : null;
    }

    /**
     * Template method that resolves the qualified names from the given SOAP message.
     *
     * @param message the SOAP message
     * @return an array of qualified names that serve as registration keys
     */
    protected abstract QName resolveQName(WebServiceMessage message) throws Exception;

    protected boolean validateLookupKey(String key) {
        return QNameUtils.validateQName(key);
    }

}
