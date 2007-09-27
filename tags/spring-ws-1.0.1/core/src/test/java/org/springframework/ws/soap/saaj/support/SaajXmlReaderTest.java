/*
 * Copyright 2007 the original author or authors.
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
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.xml.transform.StringResult;
import org.xml.sax.InputSource;

public class SaajXmlReaderTest extends XMLTestCase {

    private SaajXmlReader reader;

    private SOAPMessage message;

    private Transformer transformer;

    protected void setUp() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        message = messageFactory.createMessage();
        reader = new SaajXmlReader(message.getSOAPPart().getEnvelope());
        transformer = TransformerFactory.newInstance().newTransformer();
    }

    public void testIt() throws Exception {
        Result result = new StringResult();
        Source source = new SAXSource(reader, new InputSource());
        transformer.transform(source, result);
        Result expected = new StringResult();
        transformer.transform(new DOMSource(message.getSOAPPart().getEnvelope()), expected);
        assertXMLEqual(expected.toString(), result.toString());
    }
}