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

package org.springframework.ws.wsdl.wsdl11.soap;

import java.util.Properties;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;

import junit.framework.*;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SoapWsdl11DefinitionTest extends TestCase {

    private SoapWsdl11Definition definition;

    protected void setUp() throws Exception {
        definition = new MySoapWsdl11Definition();
    }

    public void testIt() throws Exception {
        definition.setBeanName("wsdlDefinition");
        definition.setTargetNamespace("http://springframework.org/spring-ws");
        definition.setServiceName("Service");
        definition.setLocationUri("http://localhost");
        definition.setCreateSoap11Binding(true);
        definition.setCreateSoap12Binding(true);
        Properties soapActions = new Properties();
        soapActions.setProperty("Operation", "http://springframework.org/spring-ws/Action");
        definition.setSoapActions(soapActions);
        definition.afterPropertiesSet();
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.transform(definition.getSource(), new StreamResult(System.out));
    }

    private static class MySoapWsdl11Definition extends SoapWsdl11Definition {

        protected void addPortTypes(Document document, Element definitions) {
            Element portType = createWsdlElement(document, "portType");
            definitions.appendChild(portType);
            portType.setAttribute("name", "PortType");
            Element operation = createWsdlElement(document, "operation");
            portType.appendChild(operation);
            operation.setAttribute("name", "Operation");
            Element input = createWsdlElement(document, "input");
            operation.appendChild(input);
//            input.setAttribute("name", "Input");
            Element output = createWsdlElement(document, "output");
            operation.appendChild(output);
//            output.setAttribute("name", "Output");
            Element fault = createWsdlElement(document, "fault");
            operation.appendChild(fault);
            fault.setAttribute("name", "Fault");
        }
    }
}