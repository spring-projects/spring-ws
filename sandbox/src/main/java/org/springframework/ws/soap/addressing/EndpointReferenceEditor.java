/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.soap.addressing;

import java.beans.PropertyEditorSupport;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.util.StringUtils;

/**
 * Editor for <code>EndpointReference</code>, to directly populate a EPR property instead of using a String property as
 * bridge.
 *
 * @author Arjen Poutsma
 * @see EndpointReference
 * @since 1.5.0
 */
public class EndpointReferenceEditor extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            String uri = text.trim();
            try {
                URI address = new URI(uri);
                setValue(new EndpointReference(address));
            }
            catch (URISyntaxException ex) {
                throw new IllegalArgumentException("Invalid URI syntax: " + ex);
            }
        }
        else {
            setValue(null);
        }
    }

    public String getAsText() {
        EndpointReference value = (EndpointReference) getValue();
        return (value != null ? value.toString() : "");
    }
}
