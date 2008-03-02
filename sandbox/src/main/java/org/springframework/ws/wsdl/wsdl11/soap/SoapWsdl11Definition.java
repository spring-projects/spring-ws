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

import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.springframework.ws.wsdl.wsdl11.DomWsdl11Definition;
import org.springframework.util.Assert;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class SoapWsdl11Definition extends DomWsdl11Definition {

    private Soap11Wsdl11Definition soap11BindingDefinition = new Soap11Wsdl11Definition();

    private Soap12Wsdl11Definition soap12BindingDefinition = new Soap12Wsdl11Definition();

    private boolean createSoap11Binding = true;

    private boolean createSoap12Binding = false;

    public void setCreateSoap11Binding(boolean createSoap11Binding) {
        this.createSoap11Binding = createSoap11Binding;
    }

    public void setCreateSoap12Binding(boolean createSoap12Binding) {
        this.createSoap12Binding = createSoap12Binding;
    }

    public void setTargetNamespace(String targetNamespace) {
        super.setTargetNamespace(targetNamespace);
        soap11BindingDefinition.setTargetNamespace(targetNamespace);
        soap12BindingDefinition.setTargetNamespace(targetNamespace);
    }

    /**
     * Sets the value used for the binding transport attribute value. Defaults to the HTTP transport.
     *
     * @param transportUri the binding transport value
     */
    public void setTransportUri(String transportUri) {
        Assert.notNull(transportUri, "'transportUri' must not be null");
        soap11BindingDefinition.setTransportUri(transportUri);
        soap12BindingDefinition.setTransportUri(transportUri);
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
        soap11BindingDefinition.setSoapActions(soapActions);
        soap12BindingDefinition.setSoapActions(soapActions);
    }

    public void setServiceName(String serviceName) {
        Assert.notNull(serviceName, "'serviceName' must not be null");
        soap11BindingDefinition.setServiceName(serviceName);
        soap12BindingDefinition.setServiceName(serviceName);
    }

    public void setLocationUri(String locationUri) {
        Assert.notNull(locationUri, "'locationUri' must not be null");
        soap11BindingDefinition.setLocationUri(locationUri);
        soap12BindingDefinition.setLocationUri(locationUri);
    }

    public void afterPropertiesSet() throws Exception {
        soap11BindingDefinition.afterPropertiesSet();
        soap12BindingDefinition.afterPropertiesSet();
        super.afterPropertiesSet();
    }

    protected void declareNamespaces(Element definitions) {
        super.declareNamespaces(definitions);
        if (createSoap11Binding) {
            soap11BindingDefinition.declareNamespaces(definitions);
        }
        if (createSoap12Binding) {
            soap12BindingDefinition.declareNamespaces(definitions);
        }
    }

    protected void addBindings(Document document, Element definitions) {
        if (createSoap11Binding) {
            soap11BindingDefinition.addBindings(document, definitions);
        }
        if (createSoap12Binding) {
            soap12BindingDefinition.addBindings(document, definitions);
        }
    }

    protected void addServices(Document document, Element definitions) {
        if (createSoap11Binding) {
            soap11BindingDefinition.addServices(document, definitions);
        }
        if (createSoap12Binding) {
            soap12BindingDefinition.addServices(document, definitions);
        }
    }
}
