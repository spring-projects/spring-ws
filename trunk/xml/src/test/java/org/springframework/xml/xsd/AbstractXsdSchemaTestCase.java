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

package org.springframework.xml.xsd;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.validation.XmlValidator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class AbstractXsdSchemaTestCase extends XMLTestCase {

    private DocumentBuilder documentBuilder;

    private Transformer transformer;

    protected final void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
    }

    protected abstract XsdSchema createSchemaInternal(Resource[] schemaResources) throws Exception;

    protected XsdSchema createSchema(Resource[] schemaResources) throws Exception {
        XsdSchema schema = createSchemaInternal(schemaResources);
        if (schema instanceof InitializingBean) {
            ((InitializingBean) schema).afterPropertiesSet();
        }
        return schema;
    }

    public void testSingleDocument() throws Exception {
        XsdSchema schema =
                createSchema(new Resource[]{new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class)});
        XmlValidator validator = schema.getValidator();
        assertNotNull("No validator returned", validator);
        XsdSchemaDocument[] documents = schema.getSchemaDocuments();
        assertEquals("Invalid amount of schema documents", 1, documents.length);
        XsdSchemaDocument document = documents[0];
        assertNotNull("Document is null", document);
        assertEquals("Invalid target namespace", "http://www.springframework.org/spring-ws/single",
                document.getTargetNamespace());
        verifyDocument("single.xsd", document);
        XsdElementDeclaration[] declarations = document.getElementDeclarations();
        assertEquals("Invalid amount of element declarations", 1, declarations.length);
        XsdElementDeclaration declaration = declarations[0];
        assertEquals("Invalid element declaration name",
                new QName("http://www.springframework.org/spring-ws/single", "root"), declaration.getName());
    }

    public void testInclude() throws Exception {
        XsdSchema schema =
                createSchema(new Resource[]{new ClassPathResource("including.xsd", AbstractXsdSchemaTestCase.class)});
        XmlValidator validator = schema.getValidator();
        assertNotNull("No validator returned", validator);
        XsdSchemaDocument[] documents = schema.getSchemaDocuments();
        assertEquals("Invalid amount of schema documents", 2, documents.length);
        for (int i = 0; i < documents.length; i++) {
            XsdSchemaDocument document = documents[i];
            assertNotNull("Document is null", document);
            XsdElementDeclaration[] elementDeclarations = document.getElementDeclarations();
            if ("including.xsd".equals(document.getFilename())) {
                assertEquals("Invalid target namespace", "http://www.springframework.org/spring-ws/include",
                        document.getTargetNamespace());
                verifyDocument("including.xsd", document);
                assertEquals("Invalid amount of element declarations", 1, elementDeclarations.length);
                assertEquals("Invalid element declaration name",
                        new QName("http://www.springframework.org/spring-ws/include", "customElement"),
                        elementDeclarations[0].getName());
            }
            else if ("included.xsd".equals(document.getFilename())) {
                assertEquals("Invalid target namespace", "http://www.springframework.org/spring-ws/include",
                        document.getTargetNamespace());
                verifyDocument("included.xsd", document);
                assertEquals("Invalid amount of element declarations", 1, elementDeclarations.length);
                assertEquals("Invalid element declaration name",
                        new QName("http://www.springframework.org/spring-ws/include", "stringElement"),
                        elementDeclarations[0].getName());
            }
            else {
                fail("Invalid filename: " + document.getFilename());
            }
        }
    }

    public void testImport() throws Exception {
        XsdSchema schema =
                createSchema(new Resource[]{new ClassPathResource("importing.xsd", AbstractXsdSchemaTestCase.class)});
        XmlValidator validator = schema.getValidator();
        assertNotNull("No validator returned", validator);
        XsdSchemaDocument[] documents = schema.getSchemaDocuments();
        assertEquals("Invalid amount of schema documents", 2, documents.length);
        for (int i = 0; i < documents.length; i++) {
            XsdSchemaDocument document = documents[i];
            assertNotNull("Document is null", document);
            XsdElementDeclaration[] elementDeclarations = document.getElementDeclarations();
            if ("importing.xsd".equals(document.getFilename())) {
                assertEquals("Invalid target namespace", "http://www.springframework.org/spring-ws/importing",
                        document.getTargetNamespace());
                verifyDocument("importing.xsd", document);
                assertEquals("Invalid amount of element declarations", 1, elementDeclarations.length);
                assertEquals("Invalid element declaration name",
                        new QName("http://www.springframework.org/spring-ws/importing", "customElement"),
                        elementDeclarations[0].getName());
            }
            else if ("imported.xsd".equals(document.getFilename())) {
                assertEquals("Invalid target namespace", "http://www.springframework.org/spring-ws/imported",
                        document.getTargetNamespace());
                verifyDocument("imported.xsd", document);
                assertEquals("Invalid amount of element declarations", 1, elementDeclarations.length);
                assertEquals("Invalid element declaration name",
                        new QName("http://www.springframework.org/spring-ws/imported", "stringElement"),
                        elementDeclarations[0].getName());
            }
            else {
                fail("Invalid filename: " + document.getFilename());
            }
        }
    }

    private void verifyDocument(String filename, XsdSchemaDocument document)
            throws IOException, SAXException, TransformerException {
        assertEquals("Invalid document filename", filename, document.getFilename());
        InputStream inputStream = AbstractXsdSchemaTestCase.class.getResourceAsStream(filename);
        try {
            Document expected = documentBuilder.parse(inputStream);
            DOMResult result = new DOMResult();
            transformer.transform(document.getSource(), result);
            assertXMLEqual("Invalid source returned", expected, (Document) result.getNode());
        }
        finally {
            inputStream.close();
        }
    }

}
