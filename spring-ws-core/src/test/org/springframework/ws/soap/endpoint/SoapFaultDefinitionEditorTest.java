/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.soap.endpoint;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class SoapFaultDefinitionEditorTest extends TestCase {

    private SoapFaultDefinitionEditor editor;

    protected void setUp() throws Exception {
        editor = new SoapFaultDefinitionEditor();
    }

    public void testSetAsTextNoActor() throws Exception {
        editor.setAsText("Server, Server error");
        SoapFaultDefinition definition = (SoapFaultDefinition) editor.getValue();
        assertNotNull("fault not set", definition);
        assertEquals("Invalid fault code", new QName("Server"), definition.getCode());
        assertEquals("Invalid fault string", "Server error", definition.getString());
        assertNull("Actor set", definition.getActor());
    }

    public void testSetAsTextActor() throws Exception {
        editor.setAsText("Server, Server error, http://tempuri.org");
        SoapFaultDefinition definition = (SoapFaultDefinition) editor.getValue();
        assertNotNull("fault not set", definition);
        assertEquals("Invalid fault code", new QName("Server"), definition.getCode());
        assertEquals("Invalid fault string", "Server error", definition.getString());
        assertEquals("Invalid fault actor", "http://tempuri.org", definition.getActor());
    }

    public void testSetAsTextIllegalArgument() throws Exception {
        try {
            editor.setAsText("SOAP-ENV:Server");
        }
        catch (IllegalArgumentException ex) {
        }
    }

    public void testSetAsTextEmpty() throws Exception {
        editor.setAsText("");
        assertNull("definition not set to null", editor.getValue());
    }
}