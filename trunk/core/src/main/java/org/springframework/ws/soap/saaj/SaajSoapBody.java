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
 *  * limitations under the License.
 */

package org.springframework.ws.soap.saaj;

import java.util.Iterator;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.ws.soap.SoapBody;

abstract class SaajSoapBody extends SaajSoapElement implements SoapBody {

    protected SaajSoapBody(SOAPBody body, SaajImplementationStrategy strategy) {
        super(body, strategy);
    }

    public boolean hasFault() {
        return getSaajBody().hasFault();
    }

    public Source getPayloadSource() {
        SOAPBodyElement payloadElement = getPayloadElement();
        if (payloadElement == null) {
            return null;
        }
        else {
            return getStrategy().getSource(payloadElement);
        }
    }

    public Result getPayloadResult() {
        getStrategy().removeContents(getSaajBody());
        return getStrategy().getResult(getSaajBody());
    }

    /**
     * Retrieves the payload of the wrapped SAAJ message as a single DOM element. The payload of a message is the
     * contents of the SOAP body.
     *
     * @return the message payload, or <code>null</code> if none is set.
     */
    protected SOAPBodyElement getPayloadElement() {
        for (Iterator iterator = getSaajBody().getChildElements(); iterator.hasNext();) {
            Object child = iterator.next();
            if (child instanceof SOAPBodyElement) {
                return (SOAPBodyElement) child;
            }
        }
        return null;
    }

    protected SOAPBody getSaajBody() {
        return (SOAPBody) getSaajElement();
    }
}
