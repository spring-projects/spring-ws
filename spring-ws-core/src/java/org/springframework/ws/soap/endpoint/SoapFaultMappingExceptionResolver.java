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

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.EndpointExceptionResolver;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.context.SoapMessageContext;

/**
 * Exception resolver that generates a <code>&lt;soap:fault&gt;</code> based on the given exception.
 *
 * @author Arjen Poutsma
 */
public class SoapFaultMappingExceptionResolver implements EndpointExceptionResolver {

    protected final Log logger = LogFactory.getLog(getClass());

    private Set mappedEndpoints;

    private Properties exceptionMappings;

    private SoapFaultDefinition defaultFault;

    /**
     * Set the mappings between exception class names and SOAP Faults. The exception class name can be a substring, with
     * no wildcard support at present. <p/> The values of the given properties object should use the format described in
     * <code>SoapFaultDefinitionEditor</code>. <p/> Follows the same matching algorithm as RuleBasedTransactionAttribute
     * and RollbackRuleAttribute.
     *
     * @param mappings exception patterns (can also be fully qualified class names) as keys, fault definition texts as
     *                 values
     * @see org.springframework.ws.soap.endpoint.SoapFaultDefinitionEditor
     * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
     * @see org.springframework.transaction.interceptor.RollbackRuleAttribute
     * @see org.springframework.web.servlet.handler.SimpleMappingExceptionResolver
     */
    public void setExceptionMappings(Properties mappings) {
        this.exceptionMappings = mappings;
    }

    /**
     * Set the default fault. This fault will be returned if no specific mapping was found.
     */
    public void setDefaultFault(SoapFaultDefinition defaultFault) {
        this.defaultFault = defaultFault;
    }

    /**
     * Specify the set of endpoints that this exception resolver should map. The exception mappings and the default
     * fault will only apply to the specified endpoints. <p/> If no endpoints set, both the exception mappings and the
     * default fault will apply to all handlers. This means that a specified default fault will be used as fallback for
     * all exceptions; any further HandlerExceptionResolvers in the chain will be ignored in this case.
     */
    public void setMappedEndpoints(Set mappedEndpoints) {
        this.mappedEndpoints = mappedEndpoints;
    }

    public boolean resolveException(MessageContext messageContext, Object endpoint, Exception ex) {
        if (!(messageContext instanceof SoapMessageContext)) {
            throw new IllegalArgumentException("SoapFaultMappingExceptionResolver requires a SoapMessageContext");
        }
        if (this.mappedEndpoints != null && !this.mappedEndpoints.contains(endpoint)) {
            return false;
        }
        SoapFaultDefinition definition = getFaultDefinition(ex);
        if (definition == null) {
            return false;
        }
        SoapMessageContext soapContext = (SoapMessageContext) messageContext;
        SoapBody body = soapContext.createSoapResponse().getSoapBody();
        if (SoapFaultDefinition.RECEIVER.equals(definition.getFaultCode())) {
            body.addReceiverFault(definition.getFaultString());
        }
        else if (SoapFaultDefinition.SENDER.equals(definition.getFaultCode())) {
            body.addSenderFault(definition.getFaultString());
        }
        else {
            body.addFault(definition.getFaultCode(), definition.getFaultString());
        }
        return true;
    }

    private SoapFaultDefinition getFaultDefinition(Exception ex) {
        SoapFaultDefinition definition = null;
        if (this.exceptionMappings != null) {
            String definitionText = null;
            int deepest = Integer.MAX_VALUE;
            for (Enumeration names = this.exceptionMappings.propertyNames(); names.hasMoreElements();) {
                String exceptionMapping = (String) names.nextElement();
                int depth = getDepth(exceptionMapping, ex);
                if (depth >= 0 && depth < deepest) {
                    deepest = depth;
                    definitionText = this.exceptionMappings.getProperty(exceptionMapping);
                }
            }
            if (definitionText != null) {
                SoapFaultDefinitionEditor editor = new SoapFaultDefinitionEditor();
                editor.setAsText(definitionText);
                definition = (SoapFaultDefinition) editor.getValue();
            }
        }
        if (definition == null && this.defaultFault != null) {
            definition = this.defaultFault;
        }
        return definition;
    }

    /**
     * Return the depth to the superclass matching. <code>0</code> means ex matches exactly. Returns <code>-1</code> if
     * there's no match. Otherwise, returns depth. Lowest depth wins.
     * <p/>
     * Follows the same algorithm as RollbackRuleAttribute, and SimpleMappingExceptionResolver
     *
     * @see org.springframework.transaction.interceptor.RollbackRuleAttribute
     * @see org.springframework.web.servlet.handler.SimpleMappingExceptionResolver
     */
    public int getDepth(String exceptionMapping, Exception ex) {
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
