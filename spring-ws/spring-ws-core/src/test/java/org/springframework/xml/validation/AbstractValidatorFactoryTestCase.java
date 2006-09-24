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

package org.springframework.xml.validation;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public abstract class AbstractValidatorFactoryTestCase extends TestCase {

    private static final String VALID_MESSAGE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><product xmlns=\"http://www.springframework.org/spring-ws/test/validation\" effDate=\"2006-01-01\"><number>42</number><size>10</size></product>";

    private static final String INVALID_MESSAGE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><product xmlns=\"http://www.springframework.org/spring-ws/test/validation\" effDate=\"2006-01-01\"><size>20</size></product>";

    private XmlValidator validator;

    protected void setUp() throws Exception {
        Resource schemaResource = new ClassPathResource("schema.xsd", Jaxp13ValidatorFactoryTest.class);
        validator = createValidator(schemaResource, XmlValidatorFactory.SCHEMA_W3C_XML);
    }

    protected abstract XmlValidator createValidator(Resource schemaResource, String schemaLanguage) throws Exception;

    public void testHandleValidMessageStream() throws Exception {
        SAXParseException[] errors = validator.validate(new StreamSource(new StringReader(VALID_MESSAGE)));
        assertNotNull("Null returned for errors", errors);
        assertEquals("ValidationErrors returned", 0, errors.length);
    }

    public void testHandleInvalidMessageStream() throws Exception {
        SAXParseException[] errors = validator.validate(new StreamSource(new StringReader(INVALID_MESSAGE)));
        assertNotNull("Null returned for errors", errors);
        assertEquals("ValidationErrors returned", 3, errors.length);
    }

    public void testHandleValidMessageSax() throws Exception {
        SAXParseException[] errors =
                validator.validate(new SAXSource(new InputSource(new StringReader(VALID_MESSAGE))));
        assertNotNull("Null returned for errors", errors);
        assertEquals("ValidationErrors returned", 0, errors.length);
    }

    public void testHandleInvalidMessageSax() throws Exception {
        SAXParseException[] errors =
                validator.validate(new SAXSource(new InputSource(new StringReader(INVALID_MESSAGE))));
        assertNotNull("Null returned for errors", errors);
        assertEquals("ValidationErrors returned", 3, errors.length);
    }

    public void testHandleValidMessageDom() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document document = documentBuilderFactory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(VALID_MESSAGE)));
        SAXParseException[] errors = validator.validate(new DOMSource(document));
        assertNotNull("Null returned for errors", errors);
        assertEquals("ValidationErrors returned", 0, errors.length);
    }

    public void testHandleInvalidMessageDom() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document document = documentBuilderFactory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(INVALID_MESSAGE)));
        SAXParseException[] errors = validator.validate(new DOMSource(document));
        assertNotNull("Null returned for errors", errors);
        assertEquals("ValidationErrors returned", 3, errors.length);
    }

}
