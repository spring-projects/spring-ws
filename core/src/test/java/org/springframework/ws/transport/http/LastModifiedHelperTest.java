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

package org.springframework.ws.transport.http;

import java.util.Date;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import junit.framework.*;

import org.springframework.ws.transport.http.LastModifiedHelper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.xml.transform.ResourceSource;

import org.w3c.dom.Document;

public class LastModifiedHelperTest extends TestCase {

    private Resource resource;

    private long expected;

    @Override
    protected void setUp() throws Exception {
        resource = new ClassPathResource("single.xsd", getClass());
        expected = resource.lastModified();
    }

    public void testSaxSource() throws Exception {
        long result = LastModifiedHelper.getLastModified(new ResourceSource(resource));
        assertEquals("Invalid last modified", expected, result);
    }

    public void testDomSource() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(resource.getFile());
        long result = LastModifiedHelper.getLastModified(new DOMSource(document));
        assertEquals("Invalid last modified", expected, result);
    }

    public void testStreamSource() throws Exception {
        long result = LastModifiedHelper.getLastModified(new StreamSource(resource.getFile()));
        assertEquals("Invalid last modified", expected, result);
    }
}