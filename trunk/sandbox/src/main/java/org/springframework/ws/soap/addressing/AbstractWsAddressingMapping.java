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

import java.util.Arrays;
import java.util.Iterator;
import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.JdkVersion;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.messageid.RandomGuidMessageIdStrategy;
import org.springframework.ws.soap.addressing.messageid.UuidMessageIdStrategy;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;
import org.springframework.ws.soap.server.SoapEndpointMapping;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Abstract base class for {@link EndpointMapping} implementations that implement WS-Addressing.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class AbstractWsAddressingMapping extends TransformerObjectSupport
        implements SoapEndpointMapping, InitializingBean {

    private String[] actorsOrRoles;

    private boolean isUltimateReceiver = true;

    private MessageIdStrategy messageIdStrategy;

    private WebServiceMessageSender[] messageSenders;

    private WsAddressingVersion[] versions;

    private EndpointInterceptor[] preInterceptors;

    private EndpointInterceptor[] postInterceptors;

    private int wsAddressingInterceptorIdx = -1;

    private EndpointInterceptor[] allInterceptors;

    /** Protected constructor. Initializes the default settings. */
    protected AbstractWsAddressingMapping() {
        initDefaultStrategies();
    }

    /**
     * Initializes the default implementation for this mapping's strategies: the {@link WsAddressing200408} and {@link
     * WsAddressing200605} versions of the specication, and the {@link UuidMessageIdStrategy} on Java 5 and higher; the
     * {@link RandomGuidMessageIdStrategy} on Java 1.4.
     */
    protected void initDefaultStrategies() {
        this.versions = new WsAddressingVersion[]{new WsAddressing200408(), new WsAddressing200605()};
        if (JdkVersion.isAtLeastJava15()) {
            messageIdStrategy = new UuidMessageIdStrategy();
        }
        else {
            messageIdStrategy = new RandomGuidMessageIdStrategy();
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
     * By default, the {@link UuidMessageIdStrategy} is used on Java 5 and higher, and the {@link
     * RandomGuidMessageIdStrategy} on Java 1.4.
     */
    public final void setMessageIdProvider(MessageIdStrategy messageIdStrategy) {
        this.messageIdStrategy = messageIdStrategy;
    }

    public final void setMessageSenders(WebServiceMessageSender[] messageSenders) {
        this.messageSenders = messageSenders;
    }

    /**
     * Sets the WS-Addressing versions to be supported by this mapping.
     * <p/>
     * By default, this array is set to support {@link WsAddressing200408 the August 2004} and the {@link
     * WsAddressing200605 May 2006} versions of the specification.
     */
    public final void setVersions(WsAddressingVersion[] versions) {
        this.versions = versions;
    }

    public void afterPropertiesSet() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Supporting WS-Addressing " + Arrays.asList(versions));
        }
    }

    public final EndpointInvocationChain getEndpoint(MessageContext messageContext) throws TransformerException {
        Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest());
        SoapMessage request = (SoapMessage) messageContext.getRequest();
        for (int i = 0; i < versions.length; i++) {
            if (supports(versions[i], request)) {
                MessageAddressingProperties requestMap = versions[i].getMessageAddressingProperties(request);
                if (requestMap == null) {
                    return null;
                }
                Object endpoint = getEndpointInternal(requestMap);
                if (endpoint == null) {
                    return null;
                }
                return new SoapEndpointInvocationChain(endpoint, getAllEndpointInterceptors(versions[i]), actorsOrRoles,
                        isUltimateReceiver);
            }
        }
        return null;
    }

    private boolean supports(WsAddressingVersion version, SoapMessage request) {
        SoapHeader header = request.getSoapHeader();
        if (header != null) {
            for (Iterator iterator = header.examineAllHeaderElements(); iterator.hasNext();) {
                SoapHeaderElement headerElement = (SoapHeaderElement) iterator.next();
                if (version.understands(headerElement)) {
                    return true;
                }
            }
        }
        return false;
    }

    private EndpointInterceptor[] getAllEndpointInterceptors(WsAddressingVersion version) {
        // lazy init
        if (allInterceptors == null) {
            if (preInterceptors == null) {
                preInterceptors = new EndpointInterceptor[0];
            }
            if (postInterceptors == null) {
                postInterceptors = new EndpointInterceptor[0];
            }
            allInterceptors = new EndpointInterceptor[preInterceptors.length + postInterceptors.length + 1];
            System.arraycopy(preInterceptors, 0, allInterceptors, 0, preInterceptors.length);
            System.arraycopy(postInterceptors, 0, allInterceptors, preInterceptors.length + 1, postInterceptors.length);
        }
        allInterceptors[preInterceptors.length] =
                new WsAddressingEndpointInterceptor(version, messageIdStrategy, messageSenders);
        return allInterceptors;
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
