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

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

public abstract class AbstractValidatorFactoryTestCase extends TestCase {

    private XmlValidator validator;

    private InputStream validInputStream;

    private InputStream invalidInputStream;

    protected void setUp() throws Exception {
        Resource[] schemaResource =
                new Resource[]{new ClassPathResource("schema.xsd", AbstractValidatorFactoryTestCase.class)};
        validator = createValidator(schemaResource, XmlValidatorFactory.SCHEMA_W3C_XML);
        validInputStream = AbstractValidatorFactoryTestCase.class.getResourceAsStream("validDocument.xml");
        invalidInputStream = AbstractValidatorFactoryTestCase.class.getResourceAsStream("invalidDocument.xml");
    }

    protected void tearDown() throws Exception {
        validInputStream.close();
        invalidInputStream.close();
    }

    protected abstract XmlValidator createValidator(Resource[] schemaResources, String schemaLanguage) throws Exception;

    public void testHandleValidMessageStream() throws Exception {
        SAXParseException[] errors = validator.validate(new StreamSource(validInputStream));
        assertNotNull("Null returned for errors", errors);
        assertEquals("ValidationErrors returned", 0, errors.length);
    }

    public void testValidateTwice() throws Exception {
        validator.validate(new StreamSource(validInputStream));
        validInputStream = AbstractValidatorFactoryTestCase.class.getResourceAsStream("validDocument.xml");
        validator.validate(new StreamSource(validInputStream));
    }

    public void testHandleInvalidMessageStream() throws Exception {
        SAXParseException[] errors = validator.validate(new StreamSource(invalidInputStream));
        assertNotNull("Null returned for errors", errors);
        assertEquals("ValidationErrors returned", 3, errors.length);
    }

    public void testHandleValidMessageSax() throws Exception {
        SAXParseException[] errors = validator.validate(new SAXSource(new InputSource(validInputStream)));
        assertNotNull("Null returned for errors", errors);
        assertEquals("ValidationErrors returned", 0, errors.length);
    }

    public void testHandleInvalidMessageSax() throws Exception {
        SAXParseException[] errors = validator.validate(new SAXSource(new InputSource(invalidInputStream)));
        assertNotNull("Null returned for errors", errors);
        assertEquals("ValidationErrors returned", 3, errors.length);
    }

    public void testHandleValidMessageDom() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document document = documentBuilderFactory.newDocumentBuilder()
                .parse(new InputSource(validInputStream));
        SAXParseException[] errors = validator.validate(new DOMSource(document));
        assertNotNull("Null returned for errors", errors);
        assertEquals("ValidationErrors returned", 0, errors.length);
    }

    public void testHandleInvalidMessageDom() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document document = documentBuilderFactory.newDocumentBuilder()
                .parse(new InputSource(invalidInputStream));
        SAXParseException[] errors = validator.validate(new DOMSource(document));
        assertNotNull("Null returned for errors", errors);
        assertEquals("ValidationErrors returned", 3, errors.length);
    }

}
