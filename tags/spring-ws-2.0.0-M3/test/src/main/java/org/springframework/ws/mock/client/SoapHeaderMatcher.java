/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.mock.client;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import javax.xml.namespace.QName;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;

import static org.springframework.ws.mock.client.Assert.assertTrue;
import static org.springframework.ws.mock.client.Assert.fail;


/**
 * Matches SOAP headers.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class SoapHeaderMatcher implements RequestMatcher {

    private final QName soapHeaderName;

    SoapHeaderMatcher(QName soapHeaderName) {
        this.soapHeaderName = soapHeaderName;
    }

    public void match(URI uri, WebServiceMessage request) throws IOException, AssertionError {
        if (!(request instanceof SoapMessage)) {
            fail("Request message is not a SOAP message");
            return;
        }
        SoapMessage soapMessage = (SoapMessage) request;
        SoapHeader soapHeader = soapMessage.getSoapHeader();
        if (soapHeader == null) {
            fail("SOAP message [" + soapMessage + "] does not contain SOAP header");
            return;
        }
        Iterator<SoapHeaderElement> soapHeaderElementIterator = soapHeader.examineAllHeaderElements();
        boolean found = false;
        while (soapHeaderElementIterator.hasNext()) {
            SoapHeaderElement soapHeaderElement = soapHeaderElementIterator.next();
            if (soapHeaderName.equals(soapHeaderElement.getName())) {
                found = true;
                break;
            }
        }
        assertTrue("SOAP header [" + soapHeaderName + "] not found", found);
    }
}
