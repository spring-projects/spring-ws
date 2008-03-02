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

package org.springframework.ws.wsdl.wsdl11.visitor;

import java.util.Iterator;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.springframework.core.io.ClassPathResource;
import org.springframework.xml.sax.SaxUtils;

import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;

public class DefaultBindingVisitorTest extends XMLTestCase {

    private DefaultBindingVisitor visitor;

    private Definition definition;

    private Definition expected;

    private WSDLFactory factory;

    protected void setUp() throws Exception {
        factory = WSDLFactory.newInstance();
        definition = factory.newDefinition();
        visitor = new DefaultBindingVisitor();
        WSDLReader reader = factory.newWSDLReader();
        definition = reader.readWSDL(null,
                SaxUtils.createInputSource(new ClassPathResource("defaultBindingVisitorTest-input.wsdl", getClass())));
        expected = reader.readWSDL(null,
                SaxUtils.createInputSource(new ClassPathResource("defaultBindingVisitorTest-expected.wsdl", getClass())));
    }

    public void testDefaultBindingVisitor() throws Exception {
        visitor.startDefinition(definition);
        PortType portType = definition.getPortType(new QName("http://springframework.org/spring-ws", "PortType"));
        visitor.startPortType(portType);
        for (Iterator operationIter = portType.getOperations().iterator(); operationIter.hasNext();) {
            Operation operation = (Operation) operationIter.next();
            visitor.startOperation(operation);
            if (operation.getInput() != null) {
                visitor.input(operation.getInput());
            }
            if (operation.getOutput() != null) {
                visitor.output(operation.getOutput());
            }
            for (Iterator faultIter = operation.getFaults().values().iterator(); faultIter.hasNext();) {
                Fault fault = (Fault) faultIter.next();
                visitor.fault(fault);
            }
            visitor.endOperation(operation);
        }
        visitor.endPortType(portType);
        visitor.endDefinition(definition);

        WSDLWriter writer = factory.newWSDLWriter();
        Document resultDocument = writer.getDocument(definition);
        Document expectedDocument = writer.getDocument(expected);
        assertXMLEqual("Invalid WSDL generated", expectedDocument, resultDocument);
    }
}