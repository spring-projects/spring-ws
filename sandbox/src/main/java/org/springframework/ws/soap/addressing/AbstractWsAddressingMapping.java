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

import javax.xml.transform.TransformerException;

import org.springframework.core.JdkVersion;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.messageid.MessageIdProvider;
import org.springframework.ws.soap.addressing.messageid.UidMessageIdProvider;
import org.springframework.ws.soap.addressing.messageid.UuidMessageIdProvider;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;
import org.springframework.ws.soap.server.SoapEndpointMapping;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * @author Arjen Poutsma
 */
public abstract class AbstractWsAddressingMapping extends TransformerObjectSupport implements SoapEndpointMapping {

    private String[] actorsOrRoles;

    private boolean isUltimateReceiver = true;

    private MessageIdProvider messageIdProvider;

    private AddressingHelper[] helpers = new AddressingHelper[]{new AddressingHelper(new WsAddressing200408())};

    private EndpointInterceptor[] preInterceptors;

    private EndpointInterceptor[] postInterceptors;

    private static final Object MISSING_HEADER_ENDPOINT = new Object();

    /**
     * Protected constructor
     */
    protected AbstractWsAddressingMapping() {
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
     * Sets the message id provider used for creating WS-Addressing MessageIds. By default, the {@link
     * UuidMessageIdProvider} is used on Java 5 and higher, and the {@link UidMessageIdProvider} on Java 1.4 and lower.
     */
    public final void setMessageIdProvider(MessageIdProvider messageIdProvider) {
        this.messageIdProvider = messageIdProvider;
    }

    public final void setVersions(WsAddressingVersion[] versions) {
        Assert.notEmpty(versions, "specifications must not be empty");
        this.helpers = new AddressingHelper[versions.length];
        for (int i = 0; i < versions.length; i++) {
            this.helpers[i] = new AddressingHelper(versions[i]);
        }
    }

    public final EndpointInvocationChain getEndpoint(MessageContext messageContext) throws TransformerException {
        Assert.isTrue(messageContext.getResponse() instanceof SoapMessage,
                "WsAddressingMapping requires a SoapMessage request");
        SoapMessage request = (SoapMessage) messageContext.getRequest();
        for (int i = 0; i < helpers.length; i++) {
            if (!helpers[i].supports(request)) {
                continue;
            }
            MessageAddressingProperties map = helpers[i].getMessageAddressingProperties(request);
            Object endpoint;
            if (map.isValid()) {
                endpoint = getEndpointInternal(map);
            }
            else {
                // Set a 'fake' endpoint, so that the invocation will continue, but result in a MissingHeader fault
                // returned by the interceptor
                endpoint = MISSING_HEADER_ENDPOINT;
            }
            if (endpoint == null) {
                return null;
            }
            return new SoapEndpointInvocationChain(endpoint, null, actorsOrRoles, isUltimateReceiver);
        }
        return null;
    }

    protected abstract Object getEndpointInternal(MessageAddressingProperties map);


}
