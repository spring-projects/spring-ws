/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.transport.jms;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.transport.support.SimpleWebServiceMessageReceiverObjectSupport;

/**
 * Convenience base class for JMS server-side transport objects. Contains a {@link WebServiceMessageReceiver}, and has
 * methods for handling incoming JMS {@link Message} requests.
 * <p/>
 * Used by {@link WebServiceMessageListener} and {@link WebServiceMessageDrivenBean}.
 *
 * @author Arjen Poutsma
 * @since 1.1.0
 */
public class JmsMessageReceiver extends SimpleWebServiceMessageReceiverObjectSupport {

    /**
     * Handles an incoming messages. Uses the given session to create a response message.
     *
     * @param request the incoming message
     * @param session the JMS session used to create a response
     * @throws IllegalArgumentException when request is not a {@link BytesMessage}
     */
    protected final void handleMessage(Message request, Session session) throws Exception {
        if (request instanceof BytesMessage) {
            WebServiceConnection connection = new JmsReceiverConnection((BytesMessage) request, session);
            handleConnection(connection);
        }
        else {
            throw new IllegalArgumentException(
                    "Wrong message type: [" + request.getClass() + "]. Only BytesMessages can be handled.");
        }

    }
}
