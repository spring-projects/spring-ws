/*
 * Copyright 2005-2014 the original author or authors.
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

import java.util.Properties;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

/**
 * Implementation of the {@link BindingsProvider} and {@link ServicesProvider} interfaces that supports SOAP 1.1 and
 * SOAP 1.2. Delegates to {@link Soap11Provider} and {@link Soap12Provider}.
 * <p/>
 * By setting the {@link #setSoapActions(java.util.Properties) soapActions} property, the SOAP Actions defined in the
 * resulting WSDL can be set. Additionally, the transport uri can be changed from the default HTTP transport by using the
 * {@link #setTransportUri(String) transportUri} property.
 * <p/>
 * The {@link #setCreateSoap11Binding(boolean) createSoap11} and {@link #setCreateSoap12Binding(boolean) createSoap12}
 * properties indicate whether a SOAP 1.1 or SOAP 1.2 binding should be created. These properties default to
 * <code>true</code> and <code>false</code> respectively.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class SoapProvider implements BindingsProvider, ServicesProvider {

    private final Soap11Provider soap11BindingProvider = new Soap11Provider();

    private final Soap12Provider soap12BindingProvider = new Soap12Provider();

    private boolean createSoap11Binding = true;

    private boolean createSoap12Binding = false;

    /**
     * Indicates whether a SOAP 1.1 binding should be created.
     * <p/>
     * Defaults to <code>true</code>.
     */
    public void setCreateSoap11Binding(boolean createSoap11Binding) {
        this.createSoap11Binding = createSoap11Binding;
    }

    /**
     * Indicates whether a SOAP 1.2 binding should be created.
     * <p/>
     * Defaults to <code>false</code>.
     */
    public void setCreateSoap12Binding(boolean createSoap12Binding) {
        this.createSoap12Binding = createSoap12Binding;
    }

    /**
     * Sets the SOAP Actions for this binding. Keys are {@link javax.wsdl.BindingOperation#getName() binding operation
     * names}; values are {@link javax.wsdl.extensions.soap.SOAPOperation#getSoapActionURI() SOAP Action URIs}.
     *
     * @param soapActions the soap
     */
    public void setSoapActions(Properties soapActions) {
        soap11BindingProvider.setSoapActions(soapActions);
        soap12BindingProvider.setSoapActions(soapActions);
    }

    /** Sets the value used for the binding transport attribute value. Defaults to HTTP. */
    public void setTransportUri(String transportUri) {
        soap11BindingProvider.setTransportUri(transportUri);
        soap12BindingProvider.setTransportUri(transportUri);
    }

    /** Sets the value used for the SOAP Address location attribute value. */
    public void setLocationUri(String locationUri) {
        soap11BindingProvider.setLocationUri(locationUri);
        soap12BindingProvider.setLocationUri(locationUri);
    }

    /** Sets the service name. */
    public void setServiceName(String serviceName) {
        soap11BindingProvider.setServiceName(serviceName);
        soap12BindingProvider.setServiceName(serviceName);
    }

    @Override
    public void addBindings(Definition definition) throws WSDLException {
        if (createSoap11Binding) {
            soap11BindingProvider.addBindings(definition);
        }
        if (createSoap12Binding) {
            soap12BindingProvider.addBindings(definition);
        }
    }

    @Override
    public void addServices(Definition definition) throws WSDLException {
        if (createSoap11Binding) {
            soap11BindingProvider.addServices(definition);
        }
        if (createSoap12Binding) {
            soap12BindingProvider.addServices(definition);
        }
    }
}
