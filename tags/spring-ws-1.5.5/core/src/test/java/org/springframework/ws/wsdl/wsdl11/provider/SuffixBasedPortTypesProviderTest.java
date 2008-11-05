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

package org.springframework.ws.wsdl.wsdl11.provider;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class SuffixBasedPortTypesProviderTest extends TestCase {

    private SuffixBasedPortTypesProvider provider;

    private Definition definition;

    protected void setUp() throws Exception {
        provider = new SuffixBasedPortTypesProvider();
        WSDLFactory factory = WSDLFactory.newInstance();
        definition = factory.newDefinition();
    }

    public void testAddPortTypes() throws Exception {
        String namespace = "http://springframework.org/spring-ws";
        definition.addNamespace("tns", namespace);
        definition.setTargetNamespace(namespace);

        Message message = definition.createMessage();
        message.setQName(new QName(namespace, "OperationRequest"));
        definition.addMessage(message);

        message = definition.createMessage();
        message.setQName(new QName(namespace, "OperationResponse"));
        definition.addMessage(message);

        message = definition.createMessage();
        message.setQName(new QName(namespace, "OperationFault"));
        definition.addMessage(message);

        provider.setPortTypeName("PortType");
        provider.addPortTypes(definition);

        PortType portType = definition.getPortType(new QName(namespace, "PortType"));
        assertNotNull("No port type created", portType);

        Operation operation = portType.getOperation("Operation", "OperationRequest", "OperationResponse");
        assertNotNull("No operation created", operation);
        assertNotNull("No input created", operation.getInput());
        assertNotNull("No output created", operation.getOutput());
        assertFalse("No fault created", operation.getFaults().isEmpty());
    }
}