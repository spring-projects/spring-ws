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

package org.springframework.xml.validation;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.transform.ResourceSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

public abstract class AbstractValidatorFactoryTestCase {

    private XmlValidator validator;

    private InputStream validInputStream;

    private InputStream invalidInputStream;

    @Before
    public void setUp() throws Exception {
        Resource[] schemaResource =
                new Resource[]{new ClassPathResource("schema.xsd", AbstractValidatorFactoryTestCase.class)};
        validator = createValidator(schemaResource, XmlValidatorFactory.SCHEMA_W3C_XML);
        validInputStream = AbstractValidatorFactoryTestCase.class.getResourceAsStream("validDocument.xml");
        invalidInputStream = AbstractValidatorFactoryTestCase.class.getResourceAsStream("invalidDocument.xml");
    }

    @After
    public void tearDown() throws Exception {
        validInputStream.close();
        invalidInputStream.close();
    }

    protected abstract XmlValidator createValidator(Resource[] schemaResources, String schemaLanguage) throws Exception;

    @Test
    public void testHandleValidMessageStream() throws Exception {
        SAXParseException[] errors = validator.validate(new StreamSource(validInputStream));
        Assert.assertNotNull("Null returned for errors", errors);
        Assert.assertEquals("ValidationErrors returned", 0, errors.length);
    }

    @Test
    public void testValidateTwice() throws Exception {
        validator.validate(new StreamSource(validInputStream));
        validInputStream = AbstractValidatorFactoryTestCase.class.getResourceAsStream("validDocument.xml");
        validator.validate(new StreamSource(validInputStream));
    }

    @Test
    public void testHandleInvalidMessageStream() throws Exception {
        SAXParseException[] errors = validator.validate(new StreamSource(invalidInputStream));
        Assert.assertNotNull("Null returned for errors", errors);
        Assert.assertEquals("ValidationErrors returned", 3, errors.length);
    }

    @Test
    public void testHandleValidMessageSax() throws Exception {
        SAXParseException[] errors = validator.validate(new SAXSource(new InputSource(validInputStream)));
        Assert.assertNotNull("Null returned for errors", errors);
        Assert.assertEquals("ValidationErrors returned", 0, errors.length);
    }

    @Test
    public void testHandleInvalidMessageSax() throws Exception {
        SAXParseException[] errors = validator.validate(new SAXSource(new InputSource(invalidInputStream)));
        Assert.assertNotNull("Null returned for errors", errors);
        Assert.assertEquals("ValidationErrors returned", 3, errors.length);
    }

    @Test
    public void testHandleValidMessageDom() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document document = documentBuilderFactory.newDocumentBuilder()
                .parse(new InputSource(validInputStream));
        SAXParseException[] errors = validator.validate(new DOMSource(document));
        Assert.assertNotNull("Null returned for errors", errors);
        Assert.assertEquals("ValidationErrors returned", 0, errors.length);
    }

    @Test
    public void testHandleInvalidMessageDom() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document document = documentBuilderFactory.newDocumentBuilder()
                .parse(new InputSource(invalidInputStream));
        SAXParseException[] errors = validator.validate(new DOMSource(document));
        Assert.assertNotNull("Null returned for errors", errors);
        Assert.assertEquals("ValidationErrors returned", 3, errors.length);
    }

    @Test
    public void testMultipleSchemasValidMessage() throws Exception {
        Resource[] schemaResources = new Resource[]{
                new ClassPathResource("multipleSchemas1.xsd", AbstractValidatorFactoryTestCase.class),
                new ClassPathResource("multipleSchemas2.xsd", AbstractValidatorFactoryTestCase.class)};
        validator = createValidator(schemaResources, XmlValidatorFactory.SCHEMA_W3C_XML);

        Source document = new ResourceSource(
                new ClassPathResource("multipleSchemas1.xml", AbstractValidatorFactoryTestCase.class));
        SAXParseException[] errors = validator.validate(document);
        Assert.assertEquals("ValidationErrors returned", 0, errors.length);
        validator = createValidator(schemaResources, XmlValidatorFactory.SCHEMA_W3C_XML);
        document = new ResourceSource(
                new ClassPathResource("multipleSchemas2.xml", AbstractValidatorFactoryTestCase.class));
        errors = validator.validate(document);
        Assert.assertEquals("ValidationErrors returned", 0, errors.length);
    }

}
