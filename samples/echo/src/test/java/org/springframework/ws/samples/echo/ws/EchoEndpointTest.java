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

package org.springframework.ws.samples.echo.ws;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.easymock.MockControl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.springframework.ws.samples.echo.service.EchoService;

public class EchoEndpointTest extends XMLTestCase {

    private EchoEndpoint endpoint;

    private Document requestDocument;

    private Document responseDocument;

    private MockControl control;

    private EchoService mock;

    @Override
    protected void setUp() throws Exception {
        endpoint = new EchoEndpoint();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        requestDocument = documentBuilder.newDocument();
        responseDocument = documentBuilder.newDocument();
        control = MockControl.createControl(EchoService.class);
        mock = (EchoService) control.getMock();
        endpoint.setEchoService(mock);
    }

    public void testInvokeInternal() throws Exception {
        Element echoRequest =
                requestDocument.createElementNS(EchoEndpoint.NAMESPACE_URI, EchoEndpoint.ECHO_REQUEST_LOCAL_NAME);
        String content = "ABC";
        Text requestText = requestDocument.createTextNode(content);
        echoRequest.appendChild(requestText);
        String result = "DEF";
        control.expectAndReturn(mock.echo(content), result);
        control.replay();
        Element echoResponse = endpoint.invokeInternal(echoRequest, responseDocument);
        assertEquals("Invalid namespace", EchoEndpoint.NAMESPACE_URI, echoResponse.getNamespaceURI());
        assertEquals("Invalid namespace", EchoEndpoint.ECHO_RESPONSE_LOCAL_NAME, echoResponse.getLocalName());
        Text responseText = (Text) echoResponse.getChildNodes().item(0);
        assertEquals("Invalid content", result, responseText.getNodeValue());
        control.verify();
    }

}