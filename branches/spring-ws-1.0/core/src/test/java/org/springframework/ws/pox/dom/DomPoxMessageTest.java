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

import java.io.ByteArrayOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;

public class DomPoxMessageTest extends XMLTestCase {

    public static final String NAMESPACE = "http://www.springframework.org/spring-ws";

    private DomPoxMessage message;

    private Transformer transformer;

    protected void setUp() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        message = new DomPoxMessage(document, transformer, DomPoxMessageFactory.DEFAULT_CONTENT_TYPE);
    }

    public void testGetPayload() throws Exception {
        String content = "<root xmlns='http://www.springframework.org/spring-ws'>" + "<child/></root>";
        StringSource source = new StringSource(content);
        transformer.transform(source, message.getPayloadResult());
        StringResult stringResult = new StringResult();
        transformer.transform(message.getPayloadSource(), stringResult);
        assertXMLEqual(content, stringResult.toString());
    }

    public void testWriteTo() throws Exception {
        String content = "<root xmlns='http://www.springframework.org/spring-ws'>" + "<child/></root>";
        StringSource source = new StringSource(content);
        transformer.transform(source, message.getPayloadResult());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        message.writeTo(os);
        assertXMLEqual(content, os.toString("UTF-8"));
    }
}