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

import org.springframework.ws.samples.echo.service.EchoService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import static org.easymock.EasyMock.*;

public class EchoEndpointTest {

    private EchoEndpoint endpoint;

    private Document requestDocument;

    private Document responseDocument;

    private EchoService mock;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        requestDocument = documentBuilder.newDocument();
        responseDocument = documentBuilder.newDocument();
        mock = createMock(EchoService.class);
        endpoint = new EchoEndpoint(mock);
    }

    @Test
    public void testInvokeInternal() throws Exception {
        Element echoRequest =
                requestDocument.createElementNS(EchoEndpoint.NAMESPACE_URI, EchoEndpoint.ECHO_REQUEST_LOCAL_NAME);
        String content = "ABC";
        Text requestText = requestDocument.createTextNode(content);
        echoRequest.appendChild(requestText);
        String result = "DEF";
        expect(mock.echo(content)).andReturn(result);

        replay(mock);

        Element echoResponse = endpoint.handleEchoRequest(echoRequest);
        Assert.assertEquals("Invalid namespace", EchoEndpoint.NAMESPACE_URI, echoResponse.getNamespaceURI());
        Assert.assertEquals("Invalid namespace", EchoEndpoint.ECHO_RESPONSE_LOCAL_NAME, echoResponse.getLocalName());
        Text responseText = (Text) echoResponse.getChildNodes().item(0);
        Assert.assertEquals("Invalid content", result, responseText.getNodeValue());

        verify(mock);
    }

}