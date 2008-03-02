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

package org.springframework.ws.wsdl.wsdl11.provider;

import java.util.Iterator;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.springframework.xml.namespace.QNameUtils;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class DefaultServiceProvider implements ServiceProvider {

    private QName serviceName;

    /**
     * Returns the service name.
     */
    public QName getServiceName() {
        return serviceName;
    }

    /**
     * Sets the service name.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = QNameUtils.parseQNameString(serviceName);
    }

    public void addService(Definition definition) throws WSDLException {
        Service service = definition.createService();
        populateService(service);
        createPorts(definition, service);
        definition.addService(service);
    }

    /**
     * Called after the {@link javax.wsdl.Service} has been created, but before any sub-elements are added. Subclasses
     * can implement this method to define the service name, or add extensions to it.
     * <p/>
     * Default implementation sets the name to the {@link #setServiceName(String) serviceName} property.
     *
     * @param service the WSDL4J <code>Service</code>
     * @throws WSDLException in case of errors
     */
    protected void populateService(Service service) throws WSDLException {
        service.setQName(getServiceName());
    }

    private void createPorts(Definition definition, Service service) throws WSDLException {
        for (Iterator iterator = definition.getBindings().values().iterator(); iterator.hasNext();) {
            Binding binding = (Binding) iterator.next();
            Port port = definition.createPort();
            port.setBinding(binding);
            populatePort(port, binding);
            service.addPort(port);
        }
    }

    /**
     * Called after the {@link Port} has been created, but before any sub-elements are added. Subclasses can implement
     * this method to define the port name, or add extensions to it.
     * <p/>
     * Default implementation sets the port name to the binding name.
     *
     * @param port    the WSDL4J <code>Port</code>
     * @param binding the corresponding WSDL4J <code>Binding</code>
     * @throws WSDLException in case of errors
     */
    protected void populatePort(Port port, Binding binding) throws WSDLException {
        port.setName(binding.getQName().getLocalPart());
    }

}
