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

package org.springframework.xml.xsd.commons;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.xsd.AbstractXsdSchemaTestCase;
import org.springframework.xml.xsd.XsdSchema;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

public class CommonsXsdSchemaCollectionTest {

    private CommonsXsdSchemaCollection collection;

    private Transformer transformer;

    private DocumentBuilder documentBuilder;

    @Before
    public void setUp() throws Exception {
        collection = new CommonsXsdSchemaCollection();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void testSingle() throws Exception {
        Resource resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class);
        collection.setXsds(new Resource[]{resource});
        collection.afterPropertiesSet();
        Assert.assertEquals("Invalid amount of XSDs loaded", 1, collection.getXsdSchemas().length);
    }

    @Test
    public void testInlineComplex() throws Exception {
        Resource a = new ClassPathResource("A.xsd", AbstractXsdSchemaTestCase.class);
        collection.setXsds(new Resource[]{a});
        collection.setInline(true);
        collection.afterPropertiesSet();
        XsdSchema[] schemas = collection.getXsdSchemas();
        Assert.assertEquals("Invalid amount of XSDs loaded", 2, schemas.length);

        Assert.assertEquals("Invalid target namespace", "urn:1", schemas[0].getTargetNamespace());
        Resource abc = new ClassPathResource("ABC.xsd", AbstractXsdSchemaTestCase.class);
        Document expected = documentBuilder.parse(SaxUtils.createInputSource(abc));
        DOMResult domResult = new DOMResult();
        transformer.transform(schemas[0].getSource(), domResult);
        assertXMLEqual("Invalid XSD generated", expected, (Document) domResult.getNode());

        Assert.assertEquals("Invalid target namespace", "urn:2", schemas[1].getTargetNamespace());
        Resource cd = new ClassPathResource("CD.xsd", AbstractXsdSchemaTestCase.class);
        expected = documentBuilder.parse(SaxUtils.createInputSource(cd));
        domResult = new DOMResult();
        transformer.transform(schemas[1].getSource(), domResult);
        assertXMLEqual("Invalid XSD generated", expected, (Document) domResult.getNode());
    }

    @Test
    public void testCircular() throws Exception {
        Resource resource = new ClassPathResource("circular-1.xsd", AbstractXsdSchemaTestCase.class);
        collection.setXsds(new Resource[]{resource});
        collection.setInline(true);
        collection.afterPropertiesSet();
        XsdSchema[] schemas = collection.getXsdSchemas();
        Assert.assertEquals("Invalid amount of XSDs loaded", 1, schemas.length);
    }

    @Test
    public void testXmlNamespace() throws Exception {
        Resource resource = new ClassPathResource("xmlNamespace.xsd", AbstractXsdSchemaTestCase.class);
        collection.setXsds(new Resource[]{resource});
        collection.setInline(true);
        collection.afterPropertiesSet();
        XsdSchema[] schemas = collection.getXsdSchemas();
        Assert.assertEquals("Invalid amount of XSDs loaded", 1, schemas.length);
    }

    @Test
    public void testCreateValidator() throws Exception {
        Resource a = new ClassPathResource("A.xsd", AbstractXsdSchemaTestCase.class);
        collection.setXsds(new Resource[]{a});
        collection.setInline(true);
        collection.afterPropertiesSet();

        XmlValidator validator = collection.createValidator();
        Assert.assertNotNull("No XmlValidator returned", validator);
    }

    @Test
    public void testInvalidSchema() throws Exception {
        Resource invalid = new ClassPathResource("invalid.xsd", AbstractXsdSchemaTestCase.class);
        collection.setXsds(new Resource[]{invalid});
        try {
            collection.afterPropertiesSet();
            Assert.fail("CommonsXsdSchemaException expected");
        }
        catch (CommonsXsdSchemaException ex) {
            // expected
        }
    }

    @Test
    public void testIncludesAndImports() throws Exception {
        Resource hr = new ClassPathResource("hr.xsd", getClass());
        collection.setXsds(new Resource[]{hr});
        collection.setInline(true);
        collection.afterPropertiesSet();

        XsdSchema[] schemas = collection.getXsdSchemas();
        Assert.assertEquals("Invalid amount of XSDs loaded", 2, schemas.length);

        Assert.assertEquals("Invalid target namespace", "http://mycompany.com/hr/schemas", schemas[0].getTargetNamespace());
        Resource hr_employee = new ClassPathResource("hr_employee.xsd", getClass());
        Document expected = documentBuilder.parse(SaxUtils.createInputSource(hr_employee));
        DOMResult domResult = new DOMResult();
        transformer.transform(schemas[0].getSource(), domResult);
        assertXMLEqual("Invalid XSD generated", expected, (Document) domResult.getNode());

        Assert.assertEquals("Invalid target namespace", "http://mycompany.com/hr/schemas/holiday", schemas[1].getTargetNamespace());
        Resource holiday = new ClassPathResource("holiday.xsd", getClass());
        expected = documentBuilder.parse(SaxUtils.createInputSource(holiday));
        domResult = new DOMResult();
        transformer.transform(schemas[1].getSource(), domResult);
        assertXMLEqual("Invalid XSD generated", expected, (Document) domResult.getNode());

    }
}