/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.wsdl.wsdl11.builder;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.wsdl.WsdlDefinitionException;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl4jDefinition;
import org.springframework.ws.wsdl.wsdl11.Wsdl4jDefinitionException;

/**
 * Abstract base class for <code>Wsdl11DefinitionBuilder</code> implementations that use WSDL4J. Creates a base {@link
 * Definition}, and passes that to subclass template methods.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractWsdl4jDefinitionBuilder implements Wsdl11DefinitionBuilder {

    /** Logger available to subclasses. */
    protected final Log logger = LogFactory.getLog(getClass());

    /** WSDL4J extension registry. Lazily created in <code>createExtension()</code>. */
    private ExtensionRegistry extensionRegistry;

    /** The WSDL4J <code>Definition</code> created by <code>buildDefinition()</code>. */
    private Definition definition;

    public final void buildDefinition() throws WsdlDefinitionException {
        try {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            definition = wsdlFactory.newDefinition();
            populateDefinition(definition);
        }
        catch (WSDLException ex) {
            throw new Wsdl4jDefinitionException(ex);
        }
    }

    /**
     * Called after the <code>Definition</code> has been created, but before any sub-elements are added. Default
     * implementation is empty.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     * @see #buildDefinition()
     */
    protected void populateDefinition(Definition definition) throws WSDLException {
    }

    public final void buildImports() throws WsdlDefinitionException {
        try {
            buildImports(definition);
        }
        catch (WSDLException ex) {
            throw new Wsdl4jDefinitionException(ex);
        }
    }

    /**
     * Adds imports to the definition.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     */
    protected abstract void buildImports(Definition definition) throws WSDLException;

    public final void buildTypes() throws WsdlDefinitionException {
        try {
            buildTypes(definition);
        }
        catch (WSDLException ex) {
            throw new Wsdl4jDefinitionException(ex);
        }
    }

    /**
     * Adds types to the definition.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     */
    protected abstract void buildTypes(Definition definition) throws WSDLException;

    public final void buildMessages() throws WsdlDefinitionException {
        try {
            buildMessages(definition);
        }
        catch (WSDLException ex) {
            throw new Wsdl4jDefinitionException(ex);
        }
    }

    /**
     * Adds messages to the definition.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     */
    protected abstract void buildMessages(Definition definition) throws WSDLException;

    public final void buildPortTypes() throws WsdlDefinitionException {
        try {
            buildPortTypes(definition);
        }
        catch (WSDLException ex) {
            throw new Wsdl4jDefinitionException(ex);
        }
    }

    /**
     * Adds port types to the definition.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     */
    protected abstract void buildPortTypes(Definition definition) throws WSDLException;

    public final void buildBindings() throws WsdlDefinitionException {
        try {
            buildBindings(definition);
        }
        catch (WSDLException ex) {
            throw new Wsdl4jDefinitionException(ex);
        }
    }

    /**
     * Adds bindings to the definition.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     */
    protected abstract void buildBindings(Definition definition) throws WSDLException;

    public final void buildServices() throws WsdlDefinitionException {
        try {
            buildServices(definition);
        }
        catch (WSDLException ex) {
            throw new Wsdl4jDefinitionException(ex);
        }
    }

    /**
     * Adds services to the definition.
     *
     * @param definition the WSDL4J <code>Definition</code>
     * @throws WSDLException in case of errors
     */
    protected abstract void buildServices(Definition definition) throws WSDLException;

    public final Wsdl11Definition getDefinition() throws WsdlDefinitionException {
        return new Wsdl4jDefinition(definition);
    }

    /**
     * Creates a WSDL4J extensibility element.
     *
     * @param parentType  a class object indicating where in the WSDL definition this extension will exist
     * @param elementType the qname of the extensibility element
     * @return the extensibility element
     * @throws WSDLException in case of errors
     * @see javax.wsdl.extensions.ExtensionRegistry#createExtension(Class,javax.xml.namespace.QName)
     */
    protected ExtensibilityElement createExtension(Class parentType, QName elementType) throws WSDLException {
        if (extensionRegistry == null) {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            extensionRegistry = wsdlFactory.newPopulatedExtensionRegistry();
        }
        return extensionRegistry.createExtension(parentType, elementType);
    }

}
