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

package org.springframework.ws.pox.dom;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.mock.MockTransportRequest;
import org.springframework.ws.mock.MockTransportResponse;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DomPoxMessageContextTest extends XMLTestCase {

    private DomPoxMessageContext context;

    private Transformer transformer;

    protected void setUp() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element element = document.createElementNS("http://springframework.org/spring-ws", "element");
        document.appendChild(element);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        MockTransportRequest transportRequest = new MockTransportRequest();
        context = new DomPoxMessageContext(document, transportRequest, documentBuilder, transformer);
    }

    public void testGetRequest() throws Exception {
        WebServiceMessage message = context.getRequest();
        assertNotNull("No request returned", message);
        StringResult result = new StringResult();
        transformer.transform(message.getPayloadSource(), result);
        assertXMLEqual("<element xmlns='http://springframework.org/spring-ws'/>", result.toString());
    }

    public void testGetResponse() throws Exception {
        WebServiceMessage message = context.getResponse();
        assertNotNull("No request returned", message);
    }

    public void testSendResponse() throws Exception {
        WebServiceMessage message = context.getResponse();
        String content = "<element xmlns='http://springframework.org/spring-ws'/>";
        StringSource source = new StringSource(content);
        transformer.transform(source, message.getPayloadResult());
        MockTransportResponse transportResponse = new MockTransportResponse();
        context.sendResponse(transportResponse);
        assertXMLEqual(transportResponse.getContents(), content);
    }
}