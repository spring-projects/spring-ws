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

package org.springframework.ws.soap;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;

public abstract class AbstractSoapElementTestCase extends XMLTestCase {

    private SoapElement soapElement;

    protected Transformer transformer;

    protected final void setUp() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        soapElement = createSoapElement();
    }

    protected abstract SoapElement createSoapElement() throws Exception;

    public void testAttributes() throws Exception {
        QName name = new QName("http://springframework.org/spring-ws", "attribute");
        String value = "value";
        soapElement.addAttribute(name, value);
        assertEquals("Invalid attribute value", value, soapElement.getAttributeValue(name));
        Iterator allAttributes = soapElement.getAllAttributes();
        assertTrue("Iterator is empty", allAttributes.hasNext());

    }


}
