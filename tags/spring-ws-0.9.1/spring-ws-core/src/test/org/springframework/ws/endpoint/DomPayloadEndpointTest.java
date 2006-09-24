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

package org.springframework.ws.endpoint;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DomPayloadEndpointTest extends XMLTestCase {

    public void testInvokeInternalNullResponse() throws Exception {
        Source request = new StringSource("<request/>");
        AbstractDomPayloadEndpoint endpoint = new AbstractDomPayloadEndpoint() {

            protected Element invokeInternal(Element requestElement, Document document) throws Exception {
                assertEquals("Invalid request element", "request", requestElement.getNodeName());
                return null;
            }

        };
        Source response = endpoint.invoke(request);
        assertNull("Invalid response", response);
    }

    public void testInvokeInternal() throws Exception {
        AbstractDomPayloadEndpoint endpoint = new AbstractDomPayloadEndpoint() {

            protected Element invokeInternal(Element requestElement, Document document) throws Exception {
                assertEquals("Invalid request element", "request", requestElement.getNodeName());
                return document.createElement("response");
            }
        };

        Source request = new StringSource("<request/>");
        Source response = endpoint.invoke(request);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringResult result = new StringResult();
        transformer.transform(response, result);
        assertXMLEqual("Invalid response", "<response/>", result.toString());
    }

}
