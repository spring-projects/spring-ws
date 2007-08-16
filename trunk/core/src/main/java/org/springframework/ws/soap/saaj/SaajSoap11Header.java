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

package org.springframework.ws.soap.saaj;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.soap11.Soap11Header;

/**
 * SAAJ-specific implementation of the <code>Soap11Header</code> interface. Wraps a {@link javax.xml.soap.SOAPHeader}.
 *
 * @author Arjen Poutsma
 * @since 1.0
 */
class SaajSoap11Header extends SaajSoapHeader implements Soap11Header {

    SaajSoap11Header(SOAPHeader header) {
        super(header);
    }

    public Iterator examineHeaderElementsToProcess(String[] actors) {
        List result = new ArrayList();
        Iterator iterator = getImplementation().examineAllHeaderElements(getSaajHeader());
        while (iterator.hasNext()) {
            SOAPHeaderElement saajHeaderElement = (SOAPHeaderElement) iterator.next();
            String headerActor = saajHeaderElement.getActor();
            if (shouldProcess(headerActor, actors)) {
                result.add(saajHeaderElement);
            }
        }
        return new SaajSoapHeaderElementIterator(result.iterator());
    }

    private boolean shouldProcess(String headerActor, String[] actors) {
        if (!StringUtils.hasLength(headerActor)) {
            return true;
        }
        if (SOAPConstants.URI_SOAP_ACTOR_NEXT.equals(headerActor)) {
            return true;
        }
        if (!ObjectUtils.isEmpty(actors)) {
            for (int i = 0; i < actors.length; i++) {
                if (actors[i].equals(headerActor)) {
                    return true;
                }
            }
        }
        return false;
    }
}
