/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.addressing.server;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.SmartEndpointInterceptor;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.messageid.UuidMessageIdStrategy;
import org.springframework.ws.soap.addressing.version.Addressing10;
import org.springframework.ws.soap.addressing.version.Addressing200408;
import org.springframework.ws.soap.addressing.version.AddressingVersion;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;
import org.springframework.ws.soap.server.SoapEndpointMapping;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.xml.transform.TransformerObjectSupport;

import javax.xml.transform.TransformerException;
import java.net.URI;
import java.util.*;

/**
 * Abstract base class for {@link EndpointMapping} implementations that handle WS-Addressing. Besides the normal {@link
 * SoapEndpointMapping} properties, this mapping has a {@link #setVersions(org.springframework.ws.soap.addressing.version.AddressingVersion[])
 * versions} property, which defines the WS-Addressing specifications supported. By default, these are {@link
 * org.springframework.ws.soap.addressing.version.Addressing200408} and {@link org.springframework.ws.soap.addressing.version.Addressing10}.
 *
 * <p>The {@link #setMessageIdStrategy(MessageIdStrategy) messageIdStrategy} property defines the strategy to use for
 * creating reply {@code MessageIDs}. By default, this is the {@link UuidMessageIdStrategy}.
 *
 * <p>The {@link #setMessageSenders(WebServiceMessageSender[]) messageSenders} are used to send out-of-band reply messages.
 * If a request messages defines a non-anonymous reply address, these senders will be used to send the message.
 *
 * <p>This mapping (and all subclasses) uses an implicit WS-Addressing {@link EndpointInterceptor}, which is added in every
 * {@link EndpointInvocationChain} produced. As such, this mapping does not have the standard {@code interceptors}
 * property, but rather a {@link #setPreInterceptors(EndpointInterceptor[]) preInterceptors} and {@link
 * #setPostInterceptors(EndpointInterceptor[]) postInterceptors} property, which are added before and after the implicit
 * WS-Addressing interceptor, respectively.
 *
 * @author Arjen Poutsma
 * @author Nate Stoddard
 * @since 1.5.0
 */
public abstract class AbstractAddressingEndpointMapping extends TransformerObjectSupport
        implements SoapEndpointMapping, ApplicationContextAware, InitializingBean, Ordered {

    private String[] actorsOrRoles;

    private boolean isUltimateReceiver = true;

    private MessageIdStrategy messageIdStrategy;

    private WebServiceMessageSender[] messageSenders = new WebServiceMessageSender[0];

    private AddressingVersion[] versions;

    private EndpointInterceptor[] preInterceptors = new EndpointInterceptor[0];

    private EndpointInterceptor[] postInterceptors = new EndpointInterceptor[0];

	private SmartEndpointInterceptor[] smartInterceptors =
			new SmartEndpointInterceptor[0];

	private ApplicationContext applicationContext;

    private int order = Integer.MAX_VALUE;  // default: same as non-Ordered


    /** Protected constructor. Initializes the default settings. */
    protected AbstractAddressingEndpointMapping() {
        initDefaultStrategies();
    }

    /**
     * Initializes the default implementation for this mapping's strategies: the {@link
     * org.springframework.ws.soap.addressing.version.Addressing200408} and {@link org.springframework.ws.soap.addressing.version.Addressing10}
     * versions of the specification, and the {@link UuidMessageIdStrategy}.
     */
    protected void initDefaultStrategies() {
        this.versions = new AddressingVersion[]{new Addressing200408(), new Addressing10()};
        messageIdStrategy = new UuidMessageIdStrategy();
    }

    @Override
    public final void setActorOrRole(String actorOrRole) {
        Assert.notNull(actorOrRole, "actorOrRole must not be null");
        actorsOrRoles = new String[]{actorOrRole};
    }

    @Override
    public final void setActorsOrRoles(String[] actorsOrRoles) {
        Assert.notEmpty(actorsOrRoles, "actorsOrRoles must not be empty");
        this.actorsOrRoles = actorsOrRoles;
    }

    @Override
    public final void setUltimateReceiver(boolean ultimateReceiver) {
        this.isUltimateReceiver = ultimateReceiver;
    }

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
    public final int getOrder() {
        return order;
    }

    /**
     * Specify the order value for this mapping.
     *
     * <p>Default value is {@link Integer#MAX_VALUE}, meaning that it's non-ordered.
     *
     * @see org.springframework.core.Ordered#getOrder()
     */
    public final void setOrder(int order) {
        this.order = order;
    }

	/**
	 * Set additional interceptors to be applied before the implicit WS-Addressing interceptor, e.g.
	 * {@code XwsSecurityInterceptor}.
     */
    public final void setPreInterceptors(EndpointInterceptor[] preInterceptors) {
        Assert.notNull(preInterceptors, "'preInterceptors' must not be null");
        this.preInterceptors = preInterceptors;
    }

    /**
     * Set additional interceptors to be applied after the implicit WS-Addressing interceptor, e.g.
     * {@code PayloadLoggingInterceptor}.
     */
    public final void setPostInterceptors(EndpointInterceptor[] postInterceptors) {
        Assert.notNull(postInterceptors, "'postInterceptors' must not be null");
        this.postInterceptors = postInterceptors;
    }

    /**
     * Sets the message id strategy used for creating WS-Addressing MessageIds.
     *
     * <p>By default, the {@link UuidMessageIdStrategy} is used.
     */
    public final void setMessageIdStrategy(MessageIdStrategy messageIdStrategy) {
        Assert.notNull(messageIdStrategy, "'messageIdStrategy' must not be null");
        this.messageIdStrategy = messageIdStrategy;
    }

	/**
	 * Returns the message id strategy used for creating WS-Addressing MessageIds.
	 */
	public MessageIdStrategy getMessageIdStrategy() {
		return messageIdStrategy;
	}

	/**
	 * Sets a single message senders, which is used to send out-of-band reply messages. If a
	 * request messages defines a non-anonymous reply address, this senders will be used to
	 * send the message.
	 *
	 * @param messageSender the message sender
	 */
	public final void setMessageSender(WebServiceMessageSender messageSender) {
		Assert.notNull(messageSender, "'messageSender' must not be null");
		setMessageSenders(new WebServiceMessageSender[]{messageSender});
	}

	/**
	 * Sets the message senders, which are used to send out-of-band reply messages.
	 * If a request messages defines a non-anonymous reply address, these senders will be
	 * used to send the message.
	 *
	 * @param messageSenders the message senders
	 */
	public final void setMessageSenders(WebServiceMessageSender[] messageSenders) {
		Assert.notNull(messageSenders, "'messageSenders' must not be null");
		this.messageSenders = messageSenders;
    }

	/**
	 * Returns the message senders, which are used to send out-of-band reply messages.
	 *
	 * @return the message sender
	 */
	public final WebServiceMessageSender[] getMessageSenders() {
		return this.messageSenders;
	}

	/**
	 * Sets the WS-Addressing versions to be supported by this mapping.
	 *
	 * <p>By default, this array is set to support {@link org.springframework.ws.soap.addressing.version.Addressing200408
     * the August 2004} and the {@link org.springframework.ws.soap.addressing.version.Addressing10 May 2006} versions of
     * the specification.
     */
    public final void setVersions(AddressingVersion[] versions) {
        this.versions = versions;
    }

	@Override
	public void afterPropertiesSet() throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info("Supporting " + Arrays.asList(versions));
		}
		if (getApplicationContext() != null) {
			Map<String, SmartEndpointInterceptor> smartInterceptors = BeanFactoryUtils
					.beansOfTypeIncludingAncestors(getApplicationContext(),
							SmartEndpointInterceptor.class, true, false);
			if (!smartInterceptors.isEmpty()) {
				this.smartInterceptors = smartInterceptors.values()
						.toArray(new SmartEndpointInterceptor[smartInterceptors.size()]);
			}
		}
	}

    @Override
    public final EndpointInvocationChain getEndpoint(MessageContext messageContext) throws TransformerException {
        Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest());
        SoapMessage request = (SoapMessage) messageContext.getRequest();
        for (AddressingVersion version : versions) {
            if (supports(version, request)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Request [" + request + "] uses [" + version + "]");
                }
                MessageAddressingProperties requestMap = version.getMessageAddressingProperties(request);
                if (requestMap == null) {
                    return null;
                }
                Object endpoint = getEndpointInternal(requestMap);
                if (endpoint == null) {
                    return null;
                }
                return getEndpointInvocationChain(endpoint, version, requestMap, messageContext);
            }
        }
        return null;
    }

    /**
     * Creates a {@link SoapEndpointInvocationChain} based on the given endpoint and {@link
     * org.springframework.ws.soap.addressing.version.AddressingVersion}.
     */
    private EndpointInvocationChain getEndpointInvocationChain(Object endpoint,
                                                               AddressingVersion version,
                                                               MessageAddressingProperties requestMap,
                                                               MessageContext messageContext) {
        URI responseAction = getResponseAction(endpoint, requestMap);
        URI faultAction = getFaultAction(endpoint, requestMap);

        WebServiceMessageSender[] messageSenders = getMessageSenders(endpoint);
        MessageIdStrategy messageIdStrategy = getMessageIdStrategy(endpoint);

        List<EndpointInterceptor> interceptors = new ArrayList<EndpointInterceptor>();
        interceptors.addAll(Arrays.asList(preInterceptors));

        AddressingEndpointInterceptor addressingInterceptor = new AddressingEndpointInterceptor(version, messageIdStrategy,
	             messageSenders, responseAction, faultAction);
        interceptors.add(addressingInterceptor);
        interceptors.addAll(Arrays.asList(postInterceptors));

        if (this.smartInterceptors != null) {
            for (SmartEndpointInterceptor smartInterceptor : smartInterceptors) {
                if (smartInterceptor.shouldIntercept(messageContext, endpoint)) {
                    interceptors.add(smartInterceptor);
                }
            }
        }

	    return new SoapEndpointInvocationChain(endpoint,
			    interceptors.toArray(new EndpointInterceptor[interceptors.size()]), actorsOrRoles, isUltimateReceiver);
    }

    private boolean supports(AddressingVersion version, SoapMessage request) {
        SoapHeader header = request.getSoapHeader();
        if (header != null) {
            for (Iterator<SoapHeaderElement> iterator = header.examineAllHeaderElements(); iterator.hasNext();) {
                SoapHeaderElement headerElement = iterator.next();
                if (version.understands(headerElement)) {
                    return true;
                }
            }
        }
        return false;
    }

	/**
	 * Returns the message senders for the given endpoint. Default implementation returns
	 * {@link #getMessageSenders()}
	 *
	 * @param endpoint the endpoint
	 * @return the message senders for the given endpoint
	 */
	protected WebServiceMessageSender[] getMessageSenders(Object endpoint) {
		return getMessageSenders();
	}

	/**
	 * Returns the message ID strategy for the given endpoint. Default implementation
	 * returns {@link #getMessageIdStrategy()}
	 *
	 * @param endpoint the endpoint
	 * @return the message ID strategy for the given endpoint
	 */
	protected MessageIdStrategy getMessageIdStrategy(Object endpoint) {
		return getMessageIdStrategy();
	}

	/**
	 * Lookup an endpoint for the given  {@link MessageAddressingProperties}, returning {@code null} if no specific
	 * one is found. This template method is called by {@link #getEndpoint(MessageContext)}.
	 *
     * @param map the message addressing properties
     * @return the endpoint, or {@code null}
     */
    protected abstract Object getEndpointInternal(MessageAddressingProperties map);

    /**
     * Provides the WS-Addressing Action for response messages, given the endpoint, and request Message Addressing
     * Properties.
     *
     * @param endpoint   the mapped endpoint
     * @param requestMap the MAP for the request
     * @return the response Action
     */
    protected abstract URI getResponseAction(Object endpoint, MessageAddressingProperties requestMap);

    /**
     * Provides the WS-Addressing Action for response fault messages, given the endpoint, and request Message Addressing
     * Properties.
     *
     * @param endpoint   the mapped endpoint
     * @param requestMap the MAP for the request
     * @return the response Action
     */
    protected abstract URI getFaultAction(Object endpoint, MessageAddressingProperties requestMap);

}
