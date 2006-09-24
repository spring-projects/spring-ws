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

import java.beans.PropertyEditorSupport;
import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;
import org.springframework.ws.propertyeditors.QNameEditor;

/**
 * PropertyEditor for <code>SoapFaultDefinition</code> objects. Takes Strings of form
 * <pre>
 *  faultCode,faultMessage,faultActor
 * </pre>
 * where faultCode is the string representation of a qualfied name, and faultActor is not required. For example:
 * <pre>
 * SOAP-ENV:Server,Server error
 * </pre>
 *
 * @author Arjen Poutsma
 * @see javax.xml.namespace.QName#toString()
 * @see org.springframework.ws.propertyeditors.QNameEditor
 */
public class SoapFaultDefinitionEditor extends PropertyEditorSupport {

    private static final int CODE_INDEX = 0;

    private static final int STRING_INDEX = 1;

    private static final int ACTOR_INDEX = 2;

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
            QNameEditor editor = new QNameEditor();
            editor.setAsText(tokens[CODE_INDEX].trim());
            definition.setCode((QName) editor.getValue());
            definition.setString(tokens[STRING_INDEX].trim());
            if (tokens.length > 2) {
                definition.setActor(tokens[ACTOR_INDEX].trim());
            }
            setValue(definition);
        }
    }

}
