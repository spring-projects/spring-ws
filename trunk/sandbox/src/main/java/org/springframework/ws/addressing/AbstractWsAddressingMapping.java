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

package org.springframework.ws.soap.addressing;

import java.util.Iterator;
import javax.xml.transform.TransformerException;

import org.springframework.core.JdkVersion;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.messageid.MessageIdProvider;
import org.springframework.ws.soap.addressing.messageid.UidMessageIdProvider;
import org.springframework.ws.soap.addressing.messageid.UuidMessageIdProvider;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;
import org.springframework.ws.soap.server.SoapEndpointMapping;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Abstract base class for {@link EndpointMapping} implementations that implement WS-Addressing.
 *
 * @author Arjen Poutsma
 * @since 1.1.0
 */
public abstract class AbstractWsAddressingMapping extends TransformerObjectSupport implements SoapEndpointMapping {

    private String[] actorsOrRoles;

    private boolean isUltimateReceiver = true;

    private MessageIdProvider messageIdProvider;

    private AbstractWsAddressingInterceptor[] addressingInterceptors;

    private EndpointInterceptor[] preInterceptors;

    private EndpointInterceptor[] postInterceptors;

    /** Protected constructor */
    protected AbstractWsAddressingMapping() {
        addressingInterceptors = new AbstractWsAddressingInterceptor[]{new WsAddressing200408Interceptor(),
                new WsAddressing200508Interceptor()};
        if (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_15) {
            messageIdProvider = new UuidMessageIdProvider();
        }
        else {
            messageIdProvider = new UidMessageIdProvider();
        }
    }

    public final void setActorOrRole(String actorOrRole) {
        Assert.notNull(actorOrRole, "actorOrRole must not be null");
        actorsOrRoles = new String[]{actorOrRole};
    }

    public final void setActorsOrRoles(String[] actorsOrRoles) {
        Assert.notEmpty(actorsOrRoles, "actorsOrRoles must not be empty");
        this.actorsOrRoles = actorsOrRoles;
    }

    public final void setUltimateReceiver(boolean ultimateReceiver) {
        this.isUltimateReceiver = ultimateReceiver;
    }

    /**
     * Set additional interceptors to be applied before the implicit WS-Addressing interceptor, e.g.
     * <code>XwsSecurityInterceptor</code>.
     */
    public final void setPreInterceptors(EndpointInterceptor[] preInterceptors) {
        this.preInterceptors = preInterceptors;
    }

    /**
     * Set additional interceptors to be applied after the implicit WS-Addressing interceptor, e.g.
     * <code>PayloadLoggingInterceptor</code>.
     */
    public final void setPostInterceptors(EndpointInterceptor[] postInterceptors) {
        this.postInterceptors = postInterceptors;
    }

    /**
     * Sets the message id provider used for creating WS-Addressing MessageIds.
     * <p/>
     * By default, the {@link UuidMessageIdProvider} is used on Java 5 and higher, and the {@link UidMessageIdProvider}
     * on Java 1.4 and lower.
     */
    public final void setMessageIdProvider(MessageIdProvider messageIdProvider) {
        this.messageIdProvider = messageIdProvider;
    }

    /**
     * Sets the WS-Addressing interceptors to be supported by this mapping.
     * <p/>
     * By default, this includes the {@link WsAddressing200408Interceptor}, and the {@link
     * WsAddressing200508Interceptor}.
     */
    public final void setAddressingInterceptors(AbstractWsAddressingInterceptor[] addressingInterceptors) {
        Assert.notEmpty(addressingInterceptors, "'addressingInterceptors' must not be empty");
        this.addressingInterceptors = addressingInterceptors;
    }

    public final EndpointInvocationChain getEndpoint(MessageContext messageContext) throws TransformerException {
        Assert.isTrue(messageContext.getResponse() instanceof SoapMessage,
                "WsAddressingMapping requires a SoapMessage request");
        SoapMessage request = (SoapMessage) messageContext.getRequest();
        for (int i = 0; i < addressingInterceptors.length; i++) {
            AbstractWsAddressingInterceptor interceptor = addressingInterceptors[i];
            if (!understands(interceptor, request)) {
                continue;
            }
            MessageAddressingProperties requestMap = interceptor.getMessageAddressingProperties(request);
            if (requestMap == null) {
                return null;
            }
            Object endpoint = getEndpointInternal(requestMap);
            if (endpoint == null) {
                return null;
            }
            return new SoapEndpointInvocationChain(endpoint, getAllEndpointInterceptors(interceptor), actorsOrRoles,
                    isUltimateReceiver);
        }
        return null;
    }

    private boolean understands(AbstractWsAddressingInterceptor interceptor, SoapMessage request) {
        SoapHeader header = request.getSoapHeader();
        if (header != null) {
            for (Iterator iterator = header.examineAllHeaderElements(); iterator.hasNext();) {
                SoapHeaderElement headerElement = (SoapHeaderElement) iterator.next();
                if (interceptor.understands(headerElement)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected EndpointInterceptor[] getAllEndpointInterceptors(AbstractWsAddressingInterceptor interceptor) {
        if (preInterceptors == null) {
            preInterceptors = new EndpointInterceptor[0];
        }
        if (postInterceptors == null) {
            postInterceptors = new EndpointInterceptor[0];
        }
        EndpointInterceptor[] interceptors =
                new EndpointInterceptor[preInterceptors.length + postInterceptors.length + 1];
        System.arraycopy(preInterceptors, 0, interceptors, 0, preInterceptors.length);
        interceptors[preInterceptors.length] = interceptor;
        System.arraycopy(postInterceptors, 0, interceptors, preInterceptors.length + 1, postInterceptors.length);
        return interceptors;
    }

    /**
     * Lookup an endpoint for the given  {@link MessageAddressingProperties}, returning <code>null</code> if no specific
     * one is found. This template method is called by {@link #getEndpoint(MessageContext)}.
     *
     * @param map the message addressing properties
     * @return the endpoint, or <code>null</code>
     */
    protected abstract Object getEndpointInternal(MessageAddressingProperties map);


}
