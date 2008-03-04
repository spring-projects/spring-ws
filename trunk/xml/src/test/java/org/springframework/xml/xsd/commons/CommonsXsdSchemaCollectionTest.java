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

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.xsd.AbstractXsdSchemaTestCase;

public class CommonsXsdSchemaCollectionTest extends TestCase {

    private CommonsXsdSchemaCollection collection;

    protected void setUp() throws Exception {
        collection = new CommonsXsdSchemaCollection();
    }

    public void testSingle() throws Exception {
        Resource resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class);
        collection.setXsds(new Resource[]{resource});
        collection.afterPropertiesSet();
        assertEquals("Invalid amount of XSDs loaded", 1, collection.getXsdSchemas().length);
    }

    public void testIncludes() throws Exception {
        Resource resource = new ClassPathResource("including.xsd", AbstractXsdSchemaTestCase.class);
        collection.setXsds(new Resource[]{resource});
        collection.afterPropertiesSet();
        assertEquals("Invalid amount of XSDs loaded", 2, collection.getXsdSchemas().length);
    }

    public void testImports() throws Exception {
        Resource resource = new ClassPathResource("importing.xsd", AbstractXsdSchemaTestCase.class);
        collection.setXsds(new Resource[]{resource});
        collection.afterPropertiesSet();
        assertEquals("Invalid amount of XSDs loaded", 2, collection.getXsdSchemas().length);
    }

    public void testDuplicates() throws Exception {
        Resource resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class);
        collection.setXsds(new Resource[]{resource, resource});
        collection.afterPropertiesSet();
        assertEquals("Invalid amount of XSDs loaded", 1, collection.getXsdSchemas().length);
    }

    public void testComplex() throws Exception {
        Resource resource = new ClassPathResource("A.xsd", AbstractXsdSchemaTestCase.class);
        collection.setXsds(new Resource[]{resource});
        collection.afterPropertiesSet();
        assertEquals("Invalid amount of XSDs loaded", 5, collection.getXsdSchemas().length);
    }

}