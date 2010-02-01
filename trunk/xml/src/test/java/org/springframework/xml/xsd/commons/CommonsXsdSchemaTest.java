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

import javax.xml.transform.dom.DOMSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.xsd.AbstractXsdSchemaTestCase;
import org.springframework.xml.xsd.XsdSchema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CommonsXsdSchemaTest extends AbstractXsdSchemaTestCase {

    @Override
    protected XsdSchema createSchema(Resource resource) throws Exception {
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        XmlSchema schema = schemaCollection.read(SaxUtils.createInputSource(resource), null);
        return new CommonsXsdSchema(schema);
    }

    @Test
    public void testXmime() throws Exception {
        Resource resource = new ClassPathResource("xmime.xsd", AbstractXsdSchemaTestCase.class);
        XsdSchema schema = createSchema(resource);
        String namespace = "urn:test";
        assertEquals("Invalid target namespace", namespace, schema.getTargetNamespace());
        Document result = (Document) ((DOMSource) schema.getSource()).getNode();
        Element schemaElement = result.getDocumentElement();
        Element elementElement = (Element) schemaElement.getFirstChild();
        assertNotNull("No expectedContentTypes found",
                elementElement.getAttributeNS("http://www.w3.org/2005/05/xmlmime", "expectedContentTypes"));
    }


}