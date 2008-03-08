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

package org.springframework.ws.soap.addressing.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.transform.TransformerException;

import org.springframework.core.JdkVersion;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.core.EndpointReference;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.messageid.RandomGuidMessageIdStrategy;
import org.springframework.ws.soap.addressing.messageid.UuidMessageIdStrategy;
import org.springframework.ws.soap.addressing.version.Addressing10;
import org.springframework.ws.soap.addressing.version.AddressingVersion;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

/**
 * {@link WebServiceMessageCallback} implementation that sets the WS-Addressing <code>Action</code> header on the
 * message.
 * <p/>
 * A usage example with {@link org.springframework.ws.client.core.WebServiceTemplate}:
 * <pre>
 * WebServiceTemplate template = new WebServiceTemplate(messageFactory);
 * Result result = new DOMResult();
 * template.sendSourceAndReceiveToResult(
 *     new StringSource("&lt;content xmlns=\"http://tempuri.org\"/&gt;"),
 *     new ActionCallback(new URI("http://tempuri.org/Action")),
 *     result);
 * </pre>
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class ActionCallback implements WebServiceMessageCallback {

    private final AddressingVersion version;

    private final URI action;

    private final URI to;

    private MessageIdStrategy messageIdStrategy;

    private EndpointReference from;

    private EndpointReference replyTo;

    private EndpointReference faultTo;

    /**
     * Create a new <code>ActionCallback</code> with the given <code>Action</code>.
     * <p/>
     * The <code>To</code> header of the outgoing message will reflect the {@link org.springframework.ws.transport.WebServiceConnection#getUri()
     * connection URI}.
     * <p/>
     * The {@link AddressingVersion} is set to {@link Addressing10}.
     *
     * @param action the value of the action property to set
     */
    public ActionCallback(String action) throws URISyntaxException {
        this(new URI(action), new Addressing10(), null);
    }

    /**
     * Create a new <code>ActionCallback</code> with the given <code>Action</code>.
     * <p/>
     * The <code>To</code> header of the outgoing message will reflect the {@link org.springframework.ws.transport.WebServiceConnection#getUri()
     * connection URI}.
     * <p/>
     * The {@link AddressingVersion} is set to {@link Addressing10}.
     *
     * @param action the value of the action property to set
     */
    public ActionCallback(URI action) {
        this(action, new Addressing10(), null);
    }

    /**
     * Create a new <code>ActionCallback</code> with the given version and <code>Action</code>.
     * <p/>
     * The <code>To</code> header of the outgoing message will reflect the {@link org.springframework.ws.transport.WebServiceConnection#getUri()
     * connection URI}.
     *
     * @param action  the value of the action property to set
     * @param version the WS-Addressing version to use
     */
    public ActionCallback(URI action, AddressingVersion version) {
        this(action, version, null);
    }

    /**
     * Create a new <code>ActionCallback</code> with the given version, <code>Action</code>, and optional
     * <code>To</code>.
     *
     * @param action  the value of the action property
     * @param version the WS-Addressing version to use
     * @param action  the value of the destination property
     */
    public ActionCallback(URI action, AddressingVersion version, URI to) {
        Assert.notNull(action, "'action' must not be null");
        Assert.notNull(version, "'version' must not be null");
        this.action = action;
        this.version = version;
        this.to = to;
        if (JdkVersion.isAtLeastJava15()) {
            messageIdStrategy = new UuidMessageIdStrategy();
        }
        else {
            messageIdStrategy = new RandomGuidMessageIdStrategy();
        }
    }

    /**
     * Sets the message id strategy used for creating WS-Addressing MessageIds.
     * <p/>
     * By default, the {@link UuidMessageIdStrategy} is used on Java 5 and higher, and the {@link
     * RandomGuidMessageIdStrategy} on Java 1.4.
     */
    public void setMessageIdStrategy(MessageIdStrategy messageIdStrategy) {
        Assert.notNull(messageIdStrategy, "'messageIdStrategy' must not be null");
        this.messageIdStrategy = messageIdStrategy;
    }

    public void setFrom(EndpointReference from) {
        this.from = from;
    }

    public void setReplyTo(EndpointReference replyTo) {
        this.replyTo = replyTo;
    }

    public void setFaultTo(EndpointReference faultTo) {
        this.faultTo = faultTo;
    }

    /**
     * Returns the <code>Destination</code> for outgoing messages.
     * <p/>
     * Defaults to the {@link org.springframework.ws.transport.WebServiceConnection#getUri() connection URI} if no
     * destination was set.
     */
    protected URI getTo() {
        if (to == null) {
            TransportContext transportContext = TransportContextHolder.getTransportContext();
            if (transportContext != null && transportContext.getConnection() != null) {
                try {
                    return transportContext.getConnection().getUri();
                }
                catch (URISyntaxException ex) {
                    // ignore
                }
            }
            throw new IllegalStateException("Could not obtain connection URI from Transport Context");
        }
        else {
            return to;
        }
    }

    public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
        Assert.isInstanceOf(SoapMessage.class, message);
        SoapMessage soapMessage = (SoapMessage) message;
        URI to = getTo();
        URI messageId = messageIdStrategy.newMessageId(soapMessage);
        MessageAddressingProperties map =
                new MessageAddressingProperties(to, from, replyTo, faultTo, action, messageId);
        version.addAddressingHeaders(soapMessage, map);
    }


}
