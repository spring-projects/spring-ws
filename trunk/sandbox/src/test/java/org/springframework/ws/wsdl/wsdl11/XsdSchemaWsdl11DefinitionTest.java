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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;

import junit.framework.*;

import org.springframework.ws.wsdl.wsdl11.XsdSchemaWsdl11Definition;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

public class XsdSchemaWsdl11DefinitionTest extends TestCase {

    private XsdSchemaWsdl11Definition definition;

    protected void setUp() throws Exception {
        definition = new XsdSchemaWsdl11Definition();
    }

    public void testDefinition() throws Exception {
        ClassPathResource resource = new ClassPathResource("A.xsd", getClass());
        definition.setSchemas(new Resource[] {resource});
        definition.setTargetNamespace("http://springframework.org/spring-ws");        
        definition.afterPropertiesSet();
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.transform(definition.getSource(), new StreamResult(System.out));
        
        
    }
}