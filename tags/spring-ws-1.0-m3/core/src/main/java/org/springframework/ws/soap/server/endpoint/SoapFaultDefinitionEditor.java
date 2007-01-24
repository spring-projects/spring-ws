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

package org.springframework.ws.soap.server.endpoint;

import java.beans.PropertyEditorSupport;
import java.util.Locale;
import javax.xml.namespace.QName;

import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.QNameEditor;

/**
 * PropertyEditor for <code>SoapFaultDefinition</code> objects. Takes strings of form
 * <pre>
 * faultCode,faultString,locale
 * </pre>
 * where <code>faultCode</code> is the string representation of a <code>QName</code>, <code>faultStringOrReason</code>
 * is the fault string, and <code>locale</code> is the optional string representations for the
 * <code>faultStringOrReason</code>language. By default, the language is set to English.
 * <p/>
 * Instead of supplying a custom fault code, you can use the constants <code>SERVER</code> or <code>RECEIVER</code>
 * indicate a <code>Server</code>/<code>Receiver</code> fault, or or <code>CLIENT</code> or <code>SENDER</code>
 * to<code>Client</code>/<code>Sender</code> fault respectivaly.
 * <p/>
 * For example:
 * <pre>
 * RECEIVER,Server error
 * </pre>
 * or
 * <pre>
 * CLIENT,Client error
 * </pre>
 * or
 * <pre>
 * {http://springframework.org/spring-ws}spring-ws:FatalError},A fatal error has occurred
 * </pre>
 *
 * @author Arjen Poutsma
 * @see javax.xml.namespace.QName#toString()
 * @see org.springframework.xml.namespace.QNameEditor
 * @see SoapFaultDefinition#RECEIVER
 * @see SoapFaultDefinition#SENDER
 * @see org.springframework.ws.soap.SoapFault#getFaultCode()
 */
public class SoapFaultDefinitionEditor extends PropertyEditorSupport {

    private static final int FAULT_CODE_INDEX = 0;

    private static final int FAULT_STRING_INDEX = 1;

    private static final int FAULT_STRING_LOCALE_INDEX = 2;

    public void setAsText(String text) throws IllegalArgumentException {
        if (!StringUtils.hasLength(text)) {
            setValue(null);
        }
        else {
            String[] tokens = StringUtils.commaDelimitedListToStringArray(text);
            if (tokens.length < 2) {
                throw new IllegalArgumentException("Invalid amount of comma delimited values in [" + text +
                        "]: SoapFaultDefinitionEditor requires at least 2");
            }
            SoapFaultDefinition definition = new SoapFaultDefinition();
            QNameEditor qNameEditor = new QNameEditor();
            qNameEditor.setAsText(tokens[FAULT_CODE_INDEX].trim());
            definition.setFaultCode((QName) qNameEditor.getValue());
            definition.setFaultStringOrReason(tokens[FAULT_STRING_INDEX].trim());
            if (tokens.length > 2) {
                LocaleEditor localeEditor = new LocaleEditor();
                localeEditor.setAsText(tokens[FAULT_STRING_LOCALE_INDEX].trim());
                definition.setLocale((Locale) localeEditor.getValue());
            }
            setValue(definition);
        }
    }

}
