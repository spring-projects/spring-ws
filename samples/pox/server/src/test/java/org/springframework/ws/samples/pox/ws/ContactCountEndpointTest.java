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

package org.springframework.ws.samples.pox.ws;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;

import org.springframework.ws.samples.pox.ws.ContactCountEndpoint;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.core.io.ClassPathResource;

import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;

public class ContactCountEndpointTest extends XMLTestCase {

    private ContactCountEndpoint endpoint;

    private Transformer transformer;

    private DocumentBuilder documentBuilder;

    @Override
    protected void setUp() throws Exception {
        endpoint = new ContactCountEndpoint();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
    }

    public void testInvoke() throws Exception {
        Document request = documentBuilder
                .parse(SaxUtils.createInputSource(new ClassPathResource("contactsRequest.xml", getClass())));
        Document expected = documentBuilder
                .parse(SaxUtils.createInputSource(new ClassPathResource("contactCountResponse.xml", getClass())));

        Source source = endpoint.invoke(new DOMSource(request));
        DOMResult result = new DOMResult();
        transformer.transform(source, result);
        assertXMLEqual("Invalid resonse", expected, (Document) result.getNode());
    }
}