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

import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageFactoryTestCase;

public class AxiomSoap11MessageFactoryTest extends AbstractSoap11MessageFactoryTestCase {

    protected WebServiceMessageFactory createMessageFactory() throws Exception {
        AxiomSoapMessageFactory factory = new AxiomSoapMessageFactory();
        factory.afterPropertiesSet();
        return factory;
    }

/*
    public void testCreateMtom() throws Exception {
        SOAP11Factory factory = new SOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();

        OMNamespace namespace = factory.createOMNamespace("http://springframework.org/spring-ws/mtom", "sws");
        OMElement element = factory.createOMElement("image", namespace);
        envelope.getBody().addChild(element);
        DataHandler dataHandler = new javax.activation.DataHandler(new FileDataSource("/Users/arjen/spring-ws/src/site/resources/images/spring-ws.png"));

        //create an OMText node with the above DataHandler and set optimized to true
        OMText textData = factory.createOMText(dataHandler, true);

        element.addChild(textData);

        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        format.setSOAP11(true);
        format.setCharSetEncoding("UTF-8");
        assertTrue(format.isOptimized());
        OutputStream os = new BufferedOutputStream(new FileOutputStream(
                "/Users/arjen/Projects/Spring/spring-ws/core/src/test/resources/org/springframework/ws/soap/soap11/soap11-mtom.bin"));
        try {
        envelope.serialize(os, format);
        } finally {
            os.close();
        }




    }
*/


}