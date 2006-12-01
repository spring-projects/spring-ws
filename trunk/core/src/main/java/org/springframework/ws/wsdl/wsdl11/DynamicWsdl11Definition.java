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

import javax.xml.transform.Source;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.wsdl.wsdl11.builder.Wsdl11DefinitionBuilder;

/**
 * <code>Wsdl11Definition</code> that creates a WSDL definition at runtime, using a {@link Wsdl11DefinitionBuilder}. Can
 * be instructed to build abstract or concrete parts of the definition, by setting the <code>buildAbstractPart</code>
 * and <code>buildConcretePart</code> properties.
 *
 * @author Arjen Poutsma
 * @see #setBuilder(org.springframework.ws.wsdl.wsdl11.builder.Wsdl11DefinitionBuilder)
 * @see #setBuildAbstractPart(boolean)
 * @see #setBuildConcretePart(boolean)
 */
public class DynamicWsdl11Definition implements Wsdl11Definition, InitializingBean {

    private Wsdl11DefinitionBuilder builder;

    private Wsdl11Definition definition;

    private boolean buildAbstractPart = true;

    private boolean buildConcretePart = true;

    public void setBuilder(Wsdl11DefinitionBuilder builder) {
        this.builder = builder;
    }

    /**
     * Indicates whether the built definition should contain an abstract part. If set to <code>true</code> (the default)
     * the definition will contain types, messages, and portTypes; if <code>false</code>, it will not.
     */
    public void setBuildAbstractPart(boolean buildAbstractPart) {
        this.buildAbstractPart = buildAbstractPart;
    }

    /**
     * Indicates whether the built definition should contain an concrete part. If set to <code>true</code> (the default)
     * the definition will contain bindings, and services; if <code>false</code>, it will not.
     */
    public void setBuildConcretePart(boolean buildConcretePart) {
        this.buildConcretePart = buildConcretePart;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(builder, "builder is required");
        builder.buildDefinition();
        builder.buildImports();
        if (buildAbstractPart) {
            builder.buildTypes();
            builder.buildMessages();
            builder.buildPortTypes();
        }
        if (buildConcretePart) {
            builder.buildBindings();
            builder.buildServices();
        }
        definition = builder.getDefinition();
    }

    public Source getSource() {
        return definition.getSource();
    }
}
