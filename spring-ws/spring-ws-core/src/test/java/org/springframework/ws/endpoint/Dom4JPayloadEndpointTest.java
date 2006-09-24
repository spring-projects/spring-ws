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

package org.springframework.ws.endpoint;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;

import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class Dom4JPayloadEndpointTest extends XMLTestCase {

    public void testInvokeInternalNullResponse() throws Exception {
        Source request = new StringSource("<request xmlns='namespace'/>");
        AbstractDom4JPayloadEndpoint endpoint = new AbstractDom4JPayloadEndpoint() {

            protected Element invokeInternal(Element requestElement, Document responseDocument) throws Exception {
                assertEquals("Invalid request element", "request", requestElement.getName());
                assertEquals("Invalid request element", "namespace", requestElement.getNamespaceURI());
                return null;
            }

        };
        Source response = endpoint.invoke(request);
        assertNull("Invalid response", response);
    }

    public void testInvokeInternal() throws Exception {
        AbstractDom4JPayloadEndpoint endpoint = new AbstractDom4JPayloadEndpoint() {

            protected Element invokeInternal(Element requestElement, Document responseDocument) throws Exception {
                assertEquals("Invalid request element", "request", requestElement.getName());
                assertEquals("Invalid request element", "namespace", requestElement.getNamespaceURI());
                return responseDocument.addElement("prefix:response", "namespace");
            }
        };

        Source request = new StringSource("<request xmlns='namespace'/>");
        Source response = endpoint.invoke(request);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringResult result = new StringResult();
        transformer.transform(response, result);
        assertXMLEqual("Invalid response", "<prefix:response xmlns:prefix='namespace'/>", result.toString());
    }

}
