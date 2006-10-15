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

package org.springframework.ws.soap.saaj.saaj13;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapBody;

/**
 * * Internal class that uses SAAJ 1.3 to implement the <code>SoapBody</code> interface. Used by
 * <code>Saaj13SoapEnvelope</code>.
 *
 * @author Arjen Poutsma
 */
abstract class Saaj13SoapBody implements SoapBody {

    protected final SOAPBody saajBody;

    Saaj13SoapBody(SOAPBody saajBody) {
        Assert.notNull(saajBody, "No saajBody given");
        this.saajBody = saajBody;
    }

    public final Source getPayloadSource() {
        SOAPBodyElement payloadElement = getPayloadElement();
        return payloadElement != null ? new DOMSource(payloadElement) : null;
    }

    public final Result getPayloadResult() {
        saajBody.removeContents();
        return new DOMResult(saajBody);
    }

    public final boolean hasFault() {
        return saajBody.hasFault();
    }

    public final QName getName() {
        return saajBody.getElementQName();
    }

    public final Source getSource() {
        return new DOMSource(saajBody);
    }

    /**
     * Retrieves the payload of the wrapped SAAJ message as a single DOM element. The payload of a message is the
     * contents of the SOAP body.
     *
     * @return the message payload, or <code>null</code> if none is set.
     */
    private SOAPBodyElement getPayloadElement() {
        for (Iterator iterator = saajBody.getChildElements(); iterator.hasNext();) {
            Object child = iterator.next();
            if (child instanceof SOAPBodyElement) {
                return (SOAPBodyElement) child;
            }
        }
        return null;
    }

}
