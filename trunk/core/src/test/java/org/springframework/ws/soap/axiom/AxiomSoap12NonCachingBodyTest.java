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

package org.springframework.ws.soap.axiom;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap12.AbstractSoap12BodyTestCase;

public class AxiomSoap12NonCachingBodyTest extends AbstractSoap12BodyTestCase {

    protected SoapBody createSoapBody() throws Exception {
        AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
        messageFactory.setPayloadCaching(false);
        messageFactory.setSoapVersion(SoapVersion.SOAP_12);

        AxiomSoapMessage axiomSoapMessage = (AxiomSoapMessage) messageFactory.createWebServiceMessage();
        return axiomSoapMessage.getSoapBody();
    }

    // overload the parent class version since it assumes the body has a payload ele after calling
    // getPayloadResult, which is not true without payload caching. The paylaod ele doesn't exist until
    // the axiomSoapMessage.writeTo() method is called in the normal call flow
/*
    public void testGetPayloadResultTwice() throws Exception {
        SoapBody soapBody = createSoapBody();
        String payload = "<payload xmlns='http://www.springframework.org' />";
        transformer.transform(new StringSource(payload), soapBody.getPayloadResult());
        transformer.transform(new StringSource(payload), soapBody.getPayloadResult());
        DOMResult domResult = new DOMResult();
        transformer.transform(soapBody.getPayloadSource(), domResult);
        Element payloadElement = ((Document) domResult.getNode()).getDocumentElement();
        assertTrue("No payload node was found", payloadElement != null);
        assertTrue("Invalid payload local name", "payload".equals(payloadElement.getLocalName()));
    }
*/
}
