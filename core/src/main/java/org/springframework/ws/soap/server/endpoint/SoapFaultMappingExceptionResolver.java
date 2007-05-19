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

import java.util.Enumeration;
import java.util.Properties;

import org.springframework.util.CollectionUtils;

/**
 * Exception resolver that allows for mapping exception class names to SOAP Faults. The mappings are set using the
 * <code>exceptionMappings</code> property, the format of which is documented in {@link SoapFaultDefinitionEditor}.
 *
 * @author Arjen Poutsma
 */
public class SoapFaultMappingExceptionResolver extends AbstractSoapFaultDefinitionExceptionResolver {

    private Properties exceptionMappings;

    /**
     * Set the mappings between exception class names and SOAP Faults. The exception class name can be a substring, with
     * no wildcard support at present.
     * <p/>
     * The values of the given properties object should use the format described in
     * <code>SoapFaultDefinitionEditor</code>.
     * <p/>
     * Follows the same matching algorithm as <code>SimpleMappingExceptionResolver</code>.
     *
     * @param mappings exception patterns (can also be fully qualified class names) as keys, fault definition texts as
     *                 values
     * @see SoapFaultDefinitionEditor
     * @see org.springframework.web.servlet.handler.SimpleMappingExceptionResolver
     */
    public void setExceptionMappings(Properties mappings) {
        exceptionMappings = mappings;
    }

    protected SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex) {
        if (!CollectionUtils.isEmpty(exceptionMappings)) {
            String definitionText = null;
            int deepest = Integer.MAX_VALUE;
            for (Enumeration names = exceptionMappings.propertyNames(); names.hasMoreElements();) {
                String exceptionMapping = (String) names.nextElement();
                int depth = getDepth(exceptionMapping, ex);
                if (depth >= 0 && depth < deepest) {
                    deepest = depth;
                    definitionText = exceptionMappings.getProperty(exceptionMapping);
                }
            }
            if (definitionText != null) {
                SoapFaultDefinitionEditor editor = new SoapFaultDefinitionEditor();
                editor.setAsText(definitionText);
                return (SoapFaultDefinition) editor.getValue();
            }
        }
        return null;
    }

    /**
     * Return the depth to the superclass matching. <code>0</code> means ex matches exactly. Returns <code>-1</code> if
     * there's no match. Otherwise, returns depth. Lowest depth wins.
     * <p/>
     * Follows the same algorithm as RollbackRuleAttribute, and SimpleMappingExceptionResolver
     *
     * @see org.springframework.web.servlet.handler.SimpleMappingExceptionResolver
     */
    protected int getDepth(String exceptionMapping, Exception ex) {
        return getDepth(exceptionMapping, ex.getClass(), 0);
    }

    private int getDepth(String exceptionMapping, Class exceptionClass, int depth) {
        if (exceptionClass.getName().indexOf(exceptionMapping) != -1) {
            return depth;
        }
        if (exceptionClass.equals(Throwable.class)) {
            return -1;
        }
        return getDepth(exceptionMapping, exceptionClass.getSuperclass(), depth + 1);
    }

}
