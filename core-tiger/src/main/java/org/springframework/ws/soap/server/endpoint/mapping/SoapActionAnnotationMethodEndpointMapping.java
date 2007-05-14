/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.soap.server.endpoint.mapping;

import java.lang.reflect.Method;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.mapping.AbstractAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;
import org.springframework.ws.soap.server.SoapEndpointMapping;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;

/**
 * Implementation of the {@link org.springframework.ws.server.EndpointMapping} interface that uses the {@link
 * SoapAction} annotation to map methods to the request SOAPAction header.
 * <p/>
 * Endpoints typically have the following form:
 * <pre>
 * &#64;Endpoint
 * public class MyEndpoint{
 *    &#64;SoapAction("http://springframework.org/spring-ws/SoapAction")
 *    public Source doSomethingWithRequest() {
 *       ...
 *    }
 * }
 * </pre>
 *
 * @author Arjen Poutsma
 */
public class SoapActionAnnotationMethodEndpointMapping extends AbstractAnnotationMethodEndpointMapping
        implements SoapEndpointMapping {

    private String[] actorsOrRoles;

    public final void setActorOrRole(String actorOrRole) {
        Assert.notNull(actorOrRole, "actorOrRole must not be null");
        actorsOrRoles = new String[]{actorOrRole};
    }

    public final void setActorsOrRoles(String[] actorsOrRoles) {
        Assert.notEmpty(actorsOrRoles, "actorsOrRoles must not be empty");
        this.actorsOrRoles = actorsOrRoles;
    }

    /**
     * Creates a new <code>SoapEndpointInvocationChain</code> based on the given endpoint, and the set interceptors, and
     * actors/roles.
     *
     * @param endpoint     the endpoint
     * @param interceptors the endpoint interceptors
     * @return the created invocation chain
     * @see #setInterceptors(org.springframework.ws.server.EndpointInterceptor[])
     * @see #setActorsOrRoles(String[])
     */
    protected final EndpointInvocationChain createEndpointInvocationChain(MessageContext messageContext,
                                                                          Object endpoint,
                                                                          EndpointInterceptor[] interceptors) {
        return new SoapEndpointInvocationChain(endpoint, interceptors, actorsOrRoles);
    }

    protected String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
        if (messageContext.getRequest() instanceof SoapMessage) {
            SoapMessage request = (SoapMessage) messageContext.getRequest();
            String soapAction = request.getSoapAction();
            if (StringUtils.hasLength(soapAction) && soapAction.charAt(0) == '"' &&
                    soapAction.charAt(soapAction.length() - 1) == '"') {
                return soapAction.substring(1, soapAction.length() - 1);
            }
            else {
                return soapAction;
            }
        }
        else {
            return null;
        }
    }

    protected String getLookupKeyForMethod(Method method) {
        SoapAction soapAction = method.getAnnotation(SoapAction.class);
        return soapAction != null ? soapAction.value() : null;
    }
}
