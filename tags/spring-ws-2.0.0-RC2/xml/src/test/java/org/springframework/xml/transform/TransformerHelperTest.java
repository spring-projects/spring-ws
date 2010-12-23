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

package org.springframework.xml.transform;

import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

public class TransformerHelperTest {

    private TransformerHelper helper;

    @Before
    public void setUp() throws Exception {
        helper = new TransformerHelper();
    }

    @Test
    public void defaultTransformerFactory() throws TransformerException, IOException, SAXException {
        doTest();
    }

    @Test
    public void customTransformerFactory() throws TransformerException, IOException, SAXException {
        helper.setTransformerFactoryClass(TransformerFactoryImpl.class);
        doTest();
    }

    private void doTest() throws TransformerException, SAXException, IOException {
        String xml = "<root xmlns='http://springframework.org/spring-ws'><child>text</child></root>";
        Source source = new StringSource(xml);
        Result result = new StringResult();

        helper.transform(source, result);

        assertXMLEqual(xml, result.toString());
    }
}
