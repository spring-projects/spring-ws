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

package org.springframework.ws.transport.xmpp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.ThreadFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractSenderConnection;
import org.springframework.ws.transport.xmpp.support.XmppTransportUtils;

/**
 * Implementation of {@link org.springframework.ws.transport.WebServiceConnection} that is used for client-side XMPP
 * access. Exposes a {@link Message} request and response message.
 *
 * @author Gildas Cuisinier
 * @author Arjen Poutsma
 * @since 2.0
 */
public class XmppSenderConnection extends AbstractSenderConnection {

    private final Message requestMessage;

    private final XMPPConnection connection;

    private Message responseMessage;

    private String messageEncoding;

    private long receiveTimeout;

    protected XmppSenderConnection(XMPPConnection connection, String to, String thread) {
        Assert.notNull(connection, "'connection' must not be null");
        Assert.hasLength(to, "'to' must not be empty");
        Assert.hasLength(thread, "'thread' must not be empty");
        this.connection = connection;
        this.requestMessage = new Message(to, Message.Type.chat);
        this.requestMessage.setThread(thread);
    }

    /** Returns the request message for this connection. */
    public Message getRequestMessage() {
        return requestMessage;
    }

    /** Returns the response message, if any, for this connection. */
    public Message getResponseMessage() {
        return responseMessage;
    }

    /*
     * Package-friendly setters
     */

    void setMessageEncoding(String messageEncoding) {
        this.messageEncoding = messageEncoding;
    }

    void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    /*
    * URI
    */

    @Override
    public URI getUri() throws URISyntaxException {
        return XmppTransportUtils.toUri(requestMessage);
    }

    /*
     * Errors
     */

    @Override
    public boolean hasError() {
        return XmppTransportUtils.hasError(responseMessage);
    }

    @Override
    public String getErrorMessage() {
        return XmppTransportUtils.getErrorMessage(responseMessage);
    }

    /*
     * Sending
     */

    @Override
    protected void addRequestHeader(String name, String value) {
        XmppTransportUtils.addHeader(requestMessage, name, value);
    }

    @Override
    protected OutputStream getRequestOutputStream() throws IOException {
        return new MessageOutputStream(requestMessage, messageEncoding);
    }

    @Override
    protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
        requestMessage.setFrom(connection.getUser());
        connection.sendPacket(requestMessage);
    }

    /*
     * Receiving
     */

    @Override
    protected void onReceiveBeforeRead() throws IOException {
        PacketFilter packetFilter = createPacketFilter();

        PacketCollector collector = connection.createPacketCollector(packetFilter);
        Packet packet = receiveTimeout >= 0 ? collector.nextResult(receiveTimeout) : collector.nextResult();
        if (packet instanceof Message) {
            responseMessage = (Message) packet;
        }
        else if (packet != null) {
            throw new IllegalArgumentException(
                    "Wrong packet type: [" + packet.getClass() + "]. Only Messages can be handled.");
        }
    }

    private PacketFilter createPacketFilter() {
        AndFilter andFilter = new AndFilter();
        andFilter.addFilter(new PacketTypeFilter(Message.class));
        andFilter.addFilter(new ThreadFilter(requestMessage.getThread()));
        return andFilter;
    }

    @Override
    protected boolean hasResponse() throws IOException {
        return responseMessage != null;
    }

    @Override
    protected Iterator<String> getResponseHeaderNames() {
        return XmppTransportUtils.getHeaderNames(responseMessage);
    }

    @Override
    protected Iterator<String> getResponseHeaders(String name) throws IOException {
        return XmppTransportUtils.getHeaders(responseMessage, name);
    }

    @Override
    protected InputStream getResponseInputStream() throws IOException {
        return new MessageInputStream(responseMessage, messageEncoding);
    }

}
