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

import junit.framework.TestCase;
import org.easymock.MockControl;

public class DynamicWsdl11DefinitionTest extends TestCase {

    private DynamicWsdl11Definition definition;

    private MockControl builderControl;

    private Wsdl11DefinitionBuilder builderMock;

    protected void setUp() throws Exception {
        definition = new DynamicWsdl11Definition();
        builderControl = MockControl.createControl(Wsdl11DefinitionBuilder.class);
        builderMock = (Wsdl11DefinitionBuilder) builderControl.getMock();
        definition.setBuilder(builderMock);
    }

    public void testComplete() throws Exception {
        builderMock.buildDefinition();
        builderMock.buildImports();
        builderMock.buildTypes();
        builderMock.buildMessages();
        builderMock.buildPortTypes();
        builderMock.buildBindings();
        builderMock.buildServices();
        builderControl.expectAndReturn(builderMock.getDefinition(), null);
        builderControl.replay();
        definition.afterPropertiesSet();
        builderControl.verify();
    }

    public void testAbstract() throws Exception {
        definition.setBuildConcretePart(false);
        builderMock.buildDefinition();
        builderMock.buildImports();
        builderMock.buildTypes();
        builderMock.buildMessages();
        builderMock.buildPortTypes();
        builderControl.expectAndReturn(builderMock.getDefinition(), null);
        builderControl.replay();
        definition.afterPropertiesSet();
        builderControl.verify();
    }

    public void testConcrete() throws Exception {
        definition.setBuildAbstractPart(false);
        builderMock.buildDefinition();
        builderMock.buildImports();
        builderMock.buildBindings();
        builderMock.buildServices();
        builderControl.expectAndReturn(builderMock.getDefinition(), null);
        builderControl.replay();
        definition.afterPropertiesSet();
        builderControl.verify();
    }
}