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

package org.springframework.ws.transport.mail;

import javax.jms.JMSException;
import javax.mail.MessagingException;

import org.springframework.ws.transport.TransportException;

/** @author Arjen Poutsma */
public class MailTransportException extends TransportException {

    private MessagingException messagingException;

    public MailTransportException(String msg) {
        super(msg);
    }

    public MailTransportException(String msg, MessagingException ex) {
        super(msg + ": " + ex.getMessage());
        initCause(ex);
    }

    public MailTransportException(MessagingException ex) {
        super(ex.getMessage());
        initCause(ex);
    }

    public MessagingException getMessagingException() {
        return messagingException;
    }
}
