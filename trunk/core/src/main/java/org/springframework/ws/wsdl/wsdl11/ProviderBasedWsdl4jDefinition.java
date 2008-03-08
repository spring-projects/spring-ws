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

package org.springframework.ws.wsdl.wsdl11;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.wsdl.wsdl11.provider.BindingsProvider;
import org.springframework.ws.wsdl.wsdl11.provider.ImportsProvider;
import org.springframework.ws.wsdl.wsdl11.provider.MessagesProvider;
import org.springframework.ws.wsdl.wsdl11.provider.PortTypesProvider;
import org.springframework.ws.wsdl.wsdl11.provider.ServicesProvider;
import org.springframework.ws.wsdl.wsdl11.provider.TypesProvider;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class ProviderBasedWsdl4jDefinition extends Wsdl4jDefinition implements InitializingBean {

    /** The prefix used to register the target namespace in the WSDL. */
    public static final String TARGET_NAMESPACE_PREFIX = "tns";

    private ImportsProvider importsProvider;

    private TypesProvider typesProvider;

    private MessagesProvider messagesProvider;

    private PortTypesProvider portTypesProvider;

    private BindingsProvider bindingsProvider;

    private ServicesProvider servicesProvider;

    private String targetNamespace;

    public void setImportsProvider(ImportsProvider importsProvider) {
        this.importsProvider = importsProvider;
    }

    public void setTypesProvider(TypesProvider typesProvider) {
        this.typesProvider = typesProvider;
    }

    public void setMessagesProvider(MessagesProvider messagesProvider) {
        this.messagesProvider = messagesProvider;
    }

    public void setPortTypesProvider(PortTypesProvider portTypesProvider) {
        this.portTypesProvider = portTypesProvider;
    }

    public void setBindingsProvider(BindingsProvider bindingsProvider) {
        this.bindingsProvider = bindingsProvider;
    }

    public void setServicesProvider(ServicesProvider servicesProvider) {
        this.servicesProvider = servicesProvider;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    /** Sets the target namespace used for this definition. Required. */
    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public void afterPropertiesSet() throws WSDLException {
        Assert.notNull(getTargetNamespace(), "'targetNamespace' is required");
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        Definition definition = wsdlFactory.newDefinition();
        definition.setTargetNamespace(getTargetNamespace());
        definition.addNamespace(TARGET_NAMESPACE_PREFIX, getTargetNamespace());
        if (importsProvider != null) {
            importsProvider.addImports(definition);
        }
        if (typesProvider != null) {
            typesProvider.addTypes(definition);
        }
        if (messagesProvider != null) {
            messagesProvider.addMessages(definition);
        }
        if (portTypesProvider != null) {
            portTypesProvider.addPortTypes(definition);
        }
        if (bindingsProvider != null) {
            bindingsProvider.addBindings(definition);
        }
        if (servicesProvider != null) {
            servicesProvider.addServices(definition);
        }
        setDefinition(definition);
    }
}
