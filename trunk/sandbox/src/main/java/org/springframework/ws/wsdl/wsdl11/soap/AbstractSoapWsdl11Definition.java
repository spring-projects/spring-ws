/*
 * Copyright 2008 the original author or authors.
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

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.wsdl.wsdl11.DomWsdl11Definition;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class AbstractSoapWsdl11Definition extends DomWsdl11Definition {

    /** The default transport URI, which indicates an HTTP transport. */
    public static final String DEFAULT_TRANSPORT_URI = "http://schemas.xmlsoap.org/soap/http";

    private String transportUri = DEFAULT_TRANSPORT_URI;

    private Properties soapActions = new Properties();

    private String serviceName;

    private String locationUri;

    /**
     * Sets the value used for the binding transport attribute value. Defaults to {@link #DEFAULT_TRANSPORT_URI}.
     *
     * @param transportUri the binding transport value
     */
    public void setTransportUri(String transportUri) {
        Assert.notNull(transportUri, "'transportUri' must not be null");
        this.transportUri = transportUri;
    }

    /**
     * Sets the SOAP Actions for this binding. Keys are {@link javax.wsdl.BindingOperation#getName() binding operation
     * names}; values are {@link javax.wsdl.extensions.soap.SOAPOperation#getSoapActionURI() SOAP Action URIs}.
     *
     * @param soapActions the soap
     * @return the soap actions
     */
    public void setSoapActions(Properties soapActions) {
        Assert.notNull(soapActions, "'soapActions' must not be null");
        this.soapActions = soapActions;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setLocationUri(String locationUri) {
        this.locationUri = locationUri;
    }

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.notNull(serviceName, "'serviceName' is required");
        Assert.notNull(locationUri, "'locationUri' is required");
    }

    protected void declareNamespaces(Element definitions) {
        super.declareNamespaces(definitions);
        declareNamespace(definitions, getSoapNamespacePrefix(), getSoapNamespaceUri());
    }

    protected void addBindings(Document document, Element definitions) {
        List portTypes = getWsdlChildElements(definitions, "portType");
        for (Iterator iterator = portTypes.iterator(); iterator.hasNext();) {
            Element portType = (Element) iterator.next();
            addSoapBinding(document, definitions, portType);
        }
    }

    private void addSoapBinding(Document document, Element definitions, Element portType) {
        Element binding = createWsdlElement(document, "binding");
        definitions.appendChild(binding);
        String portTypeName = portType.getAttribute("name");
        Assert.hasText(portTypeName, "<portType/> lacks required name attribute");
        binding.setAttribute("name", portTypeName + getBindingSuffix());
        binding.setAttribute("type", TARGET_NAMESPACE_PREFIX + ":" + portTypeName);
        Element soapBinding = createSoapElement(document, "binding");
        binding.appendChild(soapBinding);
        soapBinding.setAttribute("style", "document");
        soapBinding.setAttribute("transport", transportUri);

        List operations = getWsdlChildElements(portType, "operation");
        for (Iterator iterator = operations.iterator(); iterator.hasNext();) {
            Element operation = (Element) iterator.next();
            addSoapOperation(document, binding, operation);
        }
    }

    private void addSoapOperation(Document document, Element binding, Element operation) {
        Element bindingOperation = createWsdlElement(document, "operation");
        binding.appendChild(bindingOperation);
        String operationName = operation.getAttribute("name");
        Assert.hasText(operationName, "<operation/> lacks required name attribute");
        bindingOperation.setAttribute("name", operationName);
        Element soapOperation = createSoapElement(document, "operation");
        bindingOperation.appendChild(soapOperation);
        String soapAction = soapActions.getProperty(operationName, "");
        soapOperation.setAttribute("soapAction", soapAction);

        Element input = getWsdlChildElement(operation, "input");
        if (input != null) {
            createBindingInputOutput(document, input, bindingOperation, "input");
        }
        Element output = getWsdlChildElement(operation, "output");
        if (output != null) {
            createBindingInputOutput(document, output, bindingOperation, "output");
        }
        List faults = getWsdlChildElements(operation, "fault");
        for (Iterator iterator = faults.iterator(); iterator.hasNext();) {
            Element fault = (Element) iterator.next();
            createBindingFault(document, bindingOperation, fault);
        }
    }

    private void createBindingInputOutput(Document document,
                                          Element inputOutput,
                                          Element bindingOperation,
                                          String localName) {
        Element bindingInputOutput = createWsdlElement(document, localName);
        bindingOperation.appendChild(bindingInputOutput);
        String inputOutputName = inputOutput.getAttribute("name");
        if (StringUtils.hasLength(inputOutputName)) {
            bindingInputOutput.setAttribute("name", inputOutputName);
        }
        Element soapBody = createSoapElement(document, "body");
        bindingInputOutput.appendChild(soapBody);
        soapBody.setAttribute("use", "literal");
    }

    private void createBindingFault(Document document, Element bindingOperation, Element fault) {
        Element bindingFault = createWsdlElement(document, "fault");
        bindingOperation.appendChild(bindingFault);
        String faultName = fault.getAttribute("name");
        Assert.hasText(faultName, "<fault/> lacks required name attribute");
        bindingFault.setAttribute("name", faultName);
        Element soapBody = createSoapElement(document, "body");
        bindingFault.appendChild(soapBody);
        soapBody.setAttribute("use", "literal");
    }

    protected void addServices(Document document, Element definitions) {
        List bindings = getWsdlChildElements(definitions, "binding");
        if (!bindings.isEmpty()) {
            Element service = getWsdlChildElement(definitions, "service");
            if (service == null) {
                service = createWsdlElement(document, "service");
            }
            definitions.appendChild(service);
            service.setAttribute("name", serviceName);
            for (Iterator iterator = bindings.iterator(); iterator.hasNext();) {
                Element binding = (Element) iterator.next();
                Element soapBinding = getChildElement(binding, getSoapNamespaceUri(), "binding");
                if (soapBinding != null) {
                    addSoapPort(document, service, binding);
                }
            }
        }
    }

    private void addSoapPort(Document document, Element service, Element binding) {
        Element port = createWsdlElement(document, "port");
        service.appendChild(port);
        String bindingName = binding.getAttribute("name");
        Assert.hasText(serviceName, "<binding/> lacks required name attribute");
        port.setAttribute("name", bindingName);
        port.setAttribute("binding", TARGET_NAMESPACE_PREFIX + ":" + bindingName);
        Element soapAddress = createElement(document, getSoapNamespacePrefix(), getSoapNamespacePrefix(), "address");
        port.appendChild(soapAddress);
        soapAddress.setAttribute("location", locationUri);
    }

    protected Element createSoapElement(Document document, String localName) {
        return createElement(document, getSoapNamespacePrefix(), getSoapNamespaceUri(), localName);
    }

    protected abstract String getSoapNamespaceUri();

    protected abstract String getSoapNamespacePrefix();

    protected abstract String getBindingSuffix();


}