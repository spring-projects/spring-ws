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

package org.springframework.ws.propertyeditors;

import java.beans.PropertyEditorSupport;
import javax.xml.namespace.QName;

/**
 * PropertyEditor for <code>javax.xml.namespace.QName</code>, to populate a property of type QName from a String value.
 * <p/>
 * Expects the same syntax as QName's <code>toString</code>, i.e. "{" + Namespace URI + "}" + local part.
 *
 * @author Arjen Poutsma
 * @see javax.xml.namespace.QName
 * @see javax.xml.namespace.QName#toString()
 * @see javax.xml.namespace.QName#valueOf(String)
 */
public class QNameEditor extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        setValue(QName.valueOf(text));
    }

    public String getAsText() {
        Object value = getValue();
        return (value != null ? value.toString() : "");
    }

}
