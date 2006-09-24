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

import java.util.Locale;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class SoapFaultDefinitionEditorTest extends TestCase {

    private SoapFaultDefinitionEditor editor;

    protected void setUp() throws Exception {
        editor = new SoapFaultDefinitionEditor();
    }

    public void testSetAsTextNoLocale() throws Exception {
        editor.setAsText("Server, Server error");
        SoapFaultDefinition definition = (SoapFaultDefinition) editor.getValue();
        assertNotNull("fault not set", definition);
        assertEquals("Invalid fault code", new QName("Server"), definition.getFaultCode());
        assertEquals("Invalid fault string", "Server error", definition.getFaultString());
        assertEquals("Invalid fault string locale", Locale.ENGLISH, definition.getFaultStringLocale());
    }

    public void testSetAsTextLocale() throws Exception {
        editor.setAsText("Server, Server error, nl");
        SoapFaultDefinition definition = (SoapFaultDefinition) editor.getValue();
        assertNotNull("fault not set", definition);
        assertEquals("Invalid fault code", new QName("Server"), definition.getFaultCode());
        assertEquals("Invalid fault string", "Server error", definition.getFaultString());
        assertEquals("Invalid fault string locale", new Locale("nl"), definition.getFaultStringLocale());
    }

    public void testSetAsTextSender() throws Exception {
        editor.setAsText("SENDER, Server error");
        SoapFaultDefinition definition = (SoapFaultDefinition) editor.getValue();
        assertNotNull("fault not set", definition);
        assertEquals("Invalid fault code", SoapFaultDefinition.SENDER, definition.getFaultCode());
        assertEquals("Invalid fault string", "Server error", definition.getFaultString());
    }

    public void testSetAsTextReceiver() throws Exception {
        editor.setAsText("RECEIVER, Server error");
        SoapFaultDefinition definition = (SoapFaultDefinition) editor.getValue();
        assertNotNull("fault not set", definition);
        assertEquals("Invalid fault code", SoapFaultDefinition.RECEIVER, definition.getFaultCode());
        assertEquals("Invalid fault string", "Server error", definition.getFaultString());
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