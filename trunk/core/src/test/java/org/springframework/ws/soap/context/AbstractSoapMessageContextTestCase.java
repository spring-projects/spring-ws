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

package org.springframework.ws.soap.context;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.ws.mock.MockTransportRequest;
import org.springframework.ws.mock.MockTransportResponse;
import org.springframework.ws.transport.TransportRequest;

public abstract class AbstractSoapMessageContextTestCase extends XMLTestCase {

    protected SoapMessageContext messageContext;

    protected Transformer transformer;

    protected MockTransportResponse transportResponse;

    protected final void setUp() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        TransportRequest transportRequest = new MockTransportRequest();
        transportResponse = new MockTransportResponse();
        messageContext = createMessageContext(transportRequest);
    }

    protected abstract SoapMessageContext createMessageContext(TransportRequest transportRequest) throws Exception;


}
