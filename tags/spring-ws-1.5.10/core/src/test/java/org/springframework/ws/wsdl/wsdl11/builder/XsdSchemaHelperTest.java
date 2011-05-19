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

package org.springframework.ws.wsdl.wsdl11.builder;

import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class XsdSchemaHelperTest extends TestCase {

    public void testSingle() throws Exception {
        Resource resource = new ClassPathResource("single.xsd", getClass());
        XsdSchemaHelper helper = new XsdSchemaHelper(resource);
        assertEquals("Invalid target namespace", "http://www.springframework.org/spring-ws/single/schema",
                helper.getTargetNamespace());
        List elementDeclarations = helper.getElementDeclarations(true);
        assertEquals("Invalid amount of elements", 3, elementDeclarations.size());
        assertEquals("Invalid element declaration name",
                new QName("http://www.springframework.org/spring-ws/single/schema", "GetOrderRequest"),
                elementDeclarations.get(0));
        assertEquals("Invalid element declaration name",
                new QName("http://www.springframework.org/spring-ws/single/schema", "GetOrderResponse"),
                elementDeclarations.get(1));
        assertEquals("Invalid element declaration name",
                new QName("http://www.springframework.org/spring-ws/single/schema", "GetOrderFault"),
                elementDeclarations.get(2));
    }

    public void testFollowInclude() throws Exception {
        Resource resource = new ClassPathResource("including.xsd", getClass());
        XsdSchemaHelper helper = new XsdSchemaHelper(resource);
        assertEquals("Invalid target namespace", "http://www.springframework.org/spring-ws/include/schema",
                helper.getTargetNamespace());
        List elementDeclarations = helper.getElementDeclarations(true);
        assertEquals("Invalid amount of elements", 2, elementDeclarations.size());
        assertEquals("Invalid element declaration name",
                new QName("http://www.springframework.org/spring-ws/include/schema", "GetOrderResponse"),
                elementDeclarations.get(0));
        assertEquals("Invalid element declaration name",
                new QName("http://www.springframework.org/spring-ws/include/schema", "GetOrderRequest"),
                elementDeclarations.get(1));
    }

    public void testNotFollowInclude() throws Exception {
        Resource resource = new ClassPathResource("including.xsd", getClass());
        XsdSchemaHelper helper = new XsdSchemaHelper(resource);
        assertEquals("Invalid target namespace", "http://www.springframework.org/spring-ws/include/schema",
                helper.getTargetNamespace());
        List elementDeclarations = helper.getElementDeclarations(false);
        assertEquals("Invalid amount of elements", 1, elementDeclarations.size());
        assertEquals("Invalid element declaration name",
                new QName("http://www.springframework.org/spring-ws/include/schema", "GetOrderRequest"),
                elementDeclarations.get(0));
    }

    public void testFollowImport() throws Exception {
        Resource resource = new ClassPathResource("importing.xsd", getClass());
        XsdSchemaHelper helper = new XsdSchemaHelper(resource);
        assertEquals("Invalid target namespace", "http://www.springframework.org/spring-ws/importing/schema",
                helper.getTargetNamespace());
        List elementDeclarations = helper.getElementDeclarations(true);
        assertEquals("Invalid amount of elements", 2, elementDeclarations.size());
        assertEquals("Invalid element declaration name",
                new QName("http://www.springframework.org/spring-ws/imported/schema", "GetOrderResponse"),
                elementDeclarations.get(0));
        assertEquals("Invalid element declaration name",
                new QName("http://www.springframework.org/spring-ws/importing/schema", "GetOrderRequest"),
                elementDeclarations.get(1));
    }

    public void testNotFollowImport() throws Exception {
        Resource resource = new ClassPathResource("importing.xsd", getClass());
        XsdSchemaHelper helper = new XsdSchemaHelper(resource);
        assertEquals("Invalid target namespace", "http://www.springframework.org/spring-ws/importing/schema",
                helper.getTargetNamespace());
        List elementDeclarations = helper.getElementDeclarations(false);
        assertEquals("Invalid amount of elements", 1, elementDeclarations.size());
        assertEquals("Invalid element declaration name",
                new QName("http://www.springframework.org/spring-ws/importing/schema", "GetOrderRequest"),
                elementDeclarations.get(0));
    }
}