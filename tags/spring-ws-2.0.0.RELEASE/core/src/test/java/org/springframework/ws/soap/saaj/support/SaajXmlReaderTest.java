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

package org.springframework.ws.soap.saaj.support;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

public class SaajXmlReaderTest {

    private SaajXmlReader saajReader;

    private SOAPMessage message;

    private Transformer transformer;

    @Before
    public void setUp() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        message = messageFactory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        saajReader = new SaajXmlReader(envelope);
        transformer = TransformerFactory.newInstance().newTransformer();
    }

    @Test
    public void testNamespacesPrefixes() throws Exception {
        saajReader.setFeature("http://xml.org/sax/features/namespaces", true);
        saajReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        DOMResult result = new DOMResult();
        Source source = new SAXSource(saajReader, new InputSource());
        transformer.transform(source, result);
        DOMResult expected = new DOMResult();
        transformer.transform(new DOMSource(message.getSOAPPart().getEnvelope()), expected);
        assertXMLEqual((Document) expected.getNode(), (Document) result.getNode());
    }

    @Test
    public void testNamespacesNoPrefixes() throws Exception {
        saajReader.setFeature("http://xml.org/sax/features/namespaces", true);
        saajReader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        DOMResult result = new DOMResult();
        Source source = new SAXSource(saajReader, new InputSource());
        transformer.transform(source, result);
        DOMResult expected = new DOMResult();
        transformer.transform(new DOMSource(message.getSOAPPart().getEnvelope()), expected);
        assertXMLEqual((Document) expected.getNode(), (Document) result.getNode());
    }
}