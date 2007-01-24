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

package org.springframework.ws.client.core.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * Convenient super class for application classes that need Web service access.
 * <p/>
 * Requires a {@link org.springframework.ws.WebServiceMessageFactory} and {@link org.springframework.ws.transport.WebServiceMessageSender},
 * or a {@link org.springframework.ws.client.core.WebServiceTemplate} instance to be set. It will create its own
 * <code>WebServiceTemplate</code> if <code>WebServiceMessageFactory</code> and <code>WebServiceMessageSender</code> are
 * passed in.
 * <p/>
 * In addition to the message factory and sender properties, this gateway offers {@link
 * org.springframework.oxm.Marshaller} and {@link org.springframework.oxm.Unmarshaller} properties. Setting these is
 * required when the {@link org.springframework.ws.client.core.WebServiceTemplate#marshalSendAndReceive(Object)
 * marshalling methods} of the template are to be used.
 *
 * @author Arjen Poutsma
 * @see #setMessageFactory(org.springframework.ws.WebServiceMessageFactory)
 * @see #setMessageSender(org.springframework.ws.transport.WebServiceMessageSender)
 * @see org.springframework.ws.client.core.WebServiceTemplate
 * @see #setMarshaller(org.springframework.oxm.Marshaller)
 */
public abstract class WebServiceGatewaySupport implements InitializingBean {

    protected final Log logger = LogFactory.getLog(getClass());

    private WebServiceMessageFactory messageFactory;

    private WebServiceMessageSender messageSender;

    private WebServiceTemplate webServiceTemplate;

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    /**
     * Returns the <code>WebServiceMessageFactory</code> used by the gateway.
     */
    public final WebServiceMessageFactory getMessageFactory() {
        return messageFactory;
    }

    /**
     * Set the <code>WebServiceMessageFactory</code> to be used by the gateway.
     */
    public final void setMessageFactory(WebServiceMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * Returns the <code>WebServiceMessageSender</code> used by the gateway.
     */
    public final WebServiceMessageSender getMessageSender() {
        return messageSender;
    }

    /**
     * Sets the <code>WebServiceMessageSender</code> to be used by the gateway.
     */
    public final void setMessageSender(WebServiceMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    /**
     * Returns the <code>WebServiceTemplate</code> for the gateway.
     */
    public final WebServiceTemplate getWebServiceTemplate() {
        return webServiceTemplate;
    }

    /**
     * Sets the <code>WebServiceTemplate</code> to be used by the gateway.
     */
    public final void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    /**
     * Returns the <code>Marshaller</code> used by the gateway.
     */
    public final Marshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Sets the <code>Marshaller</code> used by the gateway. Setting this property is only required if the marshalling
     * functionality of <code>WebServiceTemplate</code> is to be used.
     *
     * @see org.springframework.ws.client.core.WebServiceTemplate#marshalSendAndReceive
     */
    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Returns the <code>Unmarshaller</code> used by the gateway.
     */
    public final Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    /**
     * Sets the <code>Unmarshaller</code> used by the gateway. Setting this property is only required if the marshalling
     * functionality of <code>WebServiceTemplate</code> is to be used.
     *
     * @see org.springframework.ws.client.core.WebServiceTemplate#marshalSendAndReceive
     */
    public final void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public final void afterPropertiesSet() throws IllegalArgumentException, BeanInitializationException {
        if (webServiceTemplate == null && messageFactory != null && messageSender != null) {
            webServiceTemplate = new WebServiceTemplate(messageFactory, messageSender);
            if (marshaller != null) {
                webServiceTemplate.setMarshaller(marshaller);
            }
            if (unmarshaller != null) {
                webServiceTemplate.setUnmarshaller(unmarshaller);
            }
        }
        if (webServiceTemplate == null) {
            throw new IllegalArgumentException("messageFactory and messageSender or webServiceTemplate is required");
        }
        try {
            initGateway();
        }
        catch (Exception ex) {
            throw new BeanInitializationException("Initialization of Web service gateway failed: " + ex.getMessage(),
                    ex);
        }
    }

    /**
     * Subclasses can override this for custom initialization behavior. Gets called after population of this instance's
     * bean properties.
     *
     * @throws java.lang.Exception if initialization fails
     */
    protected void initGateway() throws Exception {
    }

}
