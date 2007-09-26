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

package org.springframework.ws.wsdl.wsdl11;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.util.Assert;
import org.springframework.ws.wsdl.WsdlDefinitionException;
import org.w3c.dom.Document;

/**
 * Implementation of the <code>Wsdl11Definition</code> based on WSDL4J. A {@link javax.wsdl.Definition} can be given as
 * as constructor argument, or set using a property.
 *
 * @author Arjen Poutsma
 * @see #Wsdl4jDefinition(javax.wsdl.Definition)
 * @see #setDefinition(javax.wsdl.Definition)
 * @since 1.0.0
 */
public class Wsdl4jDefinition implements Wsdl11Definition {

    private Definition definition;

    /** Cached DOM version of the definition */
    private Document document;

    /** WSDL4J is not thread safe, hence the need for a monitor. */
    private final Object monitor = new Object();

    /**
     * Constructs a new, empty <code>Wsdl4jDefinition</code>.
     *
     * @see #setDefinition(javax.wsdl.Definition)
     */
    public Wsdl4jDefinition() {
    }

    /**
     * Constructs a new <code>Wsdl4jDefinition</code> based on the given <code>Definition</code>.
     *
     * @param definition the WSDL4J definition
     */
    public Wsdl4jDefinition(Definition definition) {
        setDefinition(definition);
    }

    /** Returns the WSDL4J <code>Definition</code>. */
    public Definition getDefinition() {
        return definition;
    }

    /** Set the WSDL4J <code>Definition</code>. */
    public void setDefinition(Definition definition) {
        synchronized (monitor) {
            this.definition = definition;
            this.document = null;
        }
    }

    public Source getSource() {
        Assert.notNull(definition, "definition must not be null");
        synchronized (monitor) {
            if (document == null) {
                try {
                    WSDLFactory wsdlFactory = WSDLFactory.newInstance();
                    WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
                    document = wsdlWriter.getDocument(definition);
                }
                catch (WSDLException ex) {
                    throw new WsdlDefinitionException(ex.getMessage(), ex);
                }
            }
        }
        return new DOMSource(document);
    }
}
