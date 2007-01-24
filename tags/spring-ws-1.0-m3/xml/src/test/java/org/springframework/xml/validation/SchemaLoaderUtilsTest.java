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

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;

import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

public class SchemaLoaderUtilsTest extends TestCase {

    public void testLoadSchema() throws Exception {
        Resource resource = new ClassPathResource("schema.xsd", getClass());
        Schema schema = SchemaLoaderUtils.loadSchema(resource, XMLConstants.W3C_XML_SCHEMA_NS_URI);
        assertNotNull("No schema returned", schema);
        assertFalse("Resource not closed", resource.isOpen());
    }

    public void testLoadNonExistantSchema() throws Exception {
        try {
            Resource nonExistant = new ClassPathResource("bla");
            SchemaLoaderUtils.loadSchema(nonExistant, XMLConstants.W3C_XML_SCHEMA_NS_URI);
            fail("Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testLoadNullSchema() throws Exception {
        try {
            SchemaLoaderUtils.loadSchema((Resource) null, XMLConstants.W3C_XML_SCHEMA_NS_URI);
            fail("Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testLoadMultipleSchemas() throws Exception {
        Resource envelope = new UrlResource("http://schemas.xmlsoap.org/soap/envelope/");
        Resource encoding = new UrlResource("http://schemas.xmlsoap.org/soap/encoding/");
        Schema schema =
                SchemaLoaderUtils.loadSchema(new Resource[]{envelope, encoding}, XMLConstants.W3C_XML_SCHEMA_NS_URI);
        assertNotNull("No schema returned", schema);
        assertFalse("Resource not closed", envelope.isOpen());
        assertFalse("Resource not closed", encoding.isOpen());
    }
}