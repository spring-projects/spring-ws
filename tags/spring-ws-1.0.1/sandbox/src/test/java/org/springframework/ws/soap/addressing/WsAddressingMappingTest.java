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

package org.springframework.ws.soap.addressing;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.w3c.dom.Element;

/** Test case for AbstractWsAddressingMapping */
public class WsAddressingMappingTest extends AbstractWsAddressingTestCase {

    private AbstractWsAddressingMapping mapping;

    protected void onSetUp() throws Exception {
        mapping = new MyWsAddressingMapping();
//        mapping.afterPropertiesSet();
    }

    public void testGetSoapHeaderElement() throws Exception {
        SoapMessage message = loadSaajMessage("request-200408.xml");
        Element element = mapping.getSoapHeaderElement(message);
        assertNotNull("No element returned", element);
        assertEquals("Invalid header element returned", "Header", element.getLocalName());
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.bindNamespaceUri("wsa", "http://schemas.xmlsoap.org/ws/2004/08/addressing");
        xpath.setNamespaceContext(namespaceContext);
        XPathExpression expression = xpath.compile("wsa:To");
        String result = expression.evaluate(element);
        System.out.println("result = " + result);
    }

    private static class MyWsAddressingMapping extends AbstractWsAddressingMapping {

    }

}