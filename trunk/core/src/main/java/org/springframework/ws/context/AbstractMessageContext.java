/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

/**
 * Abstract base class for {@link MessageContext} instances.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractMessageContext implements MessageContext {

    /**
     * Keys are <code>Strings</code>, values are <code>Objects</code>. Lazily initialized by
     * <code>getProperties()</code>.
     */
    private Map<String, Object> properties;

    public boolean containsProperty(String name) {
        return getProperties().containsKey(name);
    }

    public Object getProperty(String name) {
        return getProperties().get(name);
    }

    public String[] getPropertyNames() {
        return StringUtils.toStringArray(getProperties().keySet());
    }

    public void removeProperty(String name) {
        getProperties().remove(name);
    }

    public void setProperty(String name, Object value) {
        getProperties().put(name, value);
    }

    private Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        return properties;
    }
}
