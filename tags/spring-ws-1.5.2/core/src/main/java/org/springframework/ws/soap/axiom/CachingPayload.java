/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.soap.axiom;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;

/**
 * @author Arjen Poutsma
 * @since 1.5.2
 */
class CachingPayload extends Payload {

    CachingPayload(SOAPBody axiomBody, SOAPFactory axiomFactory) {
        super(axiomBody, axiomFactory);
    }

    protected XMLStreamReader getStreamReader(OMElement payloadElement) {
        return payloadElement.getXMLStreamReader();
    }

    public Result getResultInternal() {
        return new AxiomResult(getAxiomBody(), getAxiomFactory());
    }

}
