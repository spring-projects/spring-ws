/*
 * Copyright ${YEAR} the original author or authors.
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

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Source;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.xml.xsd.AbstractXsdSchemaTestCase;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.transform.ResourceSource;

public class CommonsXsdSchemaTest extends AbstractXsdSchemaTestCase {

    protected XsdSchema createSchema(Resource resource) throws Exception {
        CommonsXsdSchema schema = new CommonsXsdSchema(resource);
        schema.afterPropertiesSet();
        return schema;
    }

    public void testInline() throws Exception {
        Resource resource = new ClassPathResource("A.xsd", AbstractXsdSchemaTestCase.class);
        CommonsXsdSchema schema = new CommonsXsdSchema(resource);
        schema.afterPropertiesSet();
        XsdSchema[] inlined = schema.inline();
        for (int i = 0; i < inlined.length; i++) {
            transformer.transform(inlined[i].getSource(), new StreamResult(System.out));
            System.out.println();
        }
    }
    
    public void testCircular() throws Exception {
        Resource resource = new ClassPathResource("circular-1.xsd", AbstractXsdSchemaTestCase.class);
        CommonsXsdSchema schema = new CommonsXsdSchema(resource);
        schema.afterPropertiesSet();
        XsdSchema[] inlined = schema.inline();
        for (int i = 0; i < inlined.length; i++) {
            transformer.transform(inlined[i].getSource(), new StreamResult(System.out));
            System.out.println();
        }
    }
}