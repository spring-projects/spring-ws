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

package org.springframework.ws.wsdl.wsdl11;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.wsdl.wsdl11.provider.InliningXsdSchemaTypesProviderTest;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;

public class DefaultWsdl11DefinitionTest extends TestCase {

    private DefaultWsdl11Definition definition;

    private Transformer transformer;

    protected void setUp() throws Exception {
        definition = new DefaultWsdl11Definition();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
    }

    public void testSingle() throws Exception {
        Resource resource = new ClassPathResource("single.xsd", getClass());
        SimpleXsdSchema schema = new SimpleXsdSchema(resource);
        schema.afterPropertiesSet();
        definition.setSchema(schema);

        definition.setTargetNamespace("http://www.springframework.org/spring-ws/single/definitions");
        definition.setPortTypeName("Order");
        definition.setLocationUri("http://localhost:8080/");

        definition.afterPropertiesSet();

        transformer.transform(definition.getSource(), new StreamResult(System.out));

    }

    public void testComplex() throws Exception {
        Resource resource = new ClassPathResource("A.xsd", InliningXsdSchemaTypesProviderTest.class);
        CommonsXsdSchemaCollection collection = new CommonsXsdSchemaCollection(new Resource[]{resource});
        collection.setInline(true);
        collection.afterPropertiesSet();
        definition.setSchemaCollection(collection);

        definition.setTargetNamespace("http://www.springframework.org/spring-ws/single/definitions");
        definition.setPortTypeName("Order");
        definition.setLocationUri("http://localhost:8080/");

        definition.afterPropertiesSet();

        transformer.transform(definition.getSource(), new StreamResult(System.out));

    }
}