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

import org.springframework.ws.wsdl.WsdlDefinitionException;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;

/**
 * Defines the contract for classes that can create a {@link Wsdl11Definition} at runtime.
 * <p/>
 * Used by {@link org.springframework.ws.wsdl.wsdl11.DynamicWsdl11Definition} to generate the WSDL based on a schema, a
 * class, etc.
 *
 * @author Arjen Poutsma
 */
public interface Wsdl11DefinitionBuilder {

    /**
     * Builds a new, empty definition. This method should be called before all others.
     *
     * @throws WsdlDefinitionException in case of errors
     */
    void buildDefinition() throws WsdlDefinitionException;

    /**
     * Adds imports to the definition.
     *
     * @throws WsdlDefinitionException in case of errors
     */
    void buildImports() throws WsdlDefinitionException;

    /**
     * Adds types to the definition.
     *
     * @throws WsdlDefinitionException in case of errors
     */
    void buildTypes() throws WsdlDefinitionException;

    /**
     * Adds messages to the definition.
     *
     * @throws WsdlDefinitionException in case of errors
     */
    void buildMessages() throws WsdlDefinitionException;

    /**
     * Adds portTypes to the definition.
     *
     * @throws WsdlDefinitionException in case of errors
     */
    void buildPortTypes() throws WsdlDefinitionException;

    /**
     * Adds bindings to the definition.
     *
     * @throws WsdlDefinitionException in case of errors
     */
    void buildBindings() throws WsdlDefinitionException;

    /**
     * Adds services to the definition.
     *
     * @throws WsdlDefinitionException in case of errors
     */
    void buildServices() throws WsdlDefinitionException;

    /**
     * Returns the built <code>Wsdl11Definition</code>.
     *
     * @return the WSDL definition, or <code>null</code> if {@link #buildDefinition()} has not been called
     * @throws WsdlDefinitionException in case of errors
     */
    Wsdl11Definition getDefinition() throws WsdlDefinitionException;

}
