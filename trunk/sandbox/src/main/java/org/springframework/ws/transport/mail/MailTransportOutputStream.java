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

package org.springframework.ws.transport.mail;

import java.io.IOException;
import java.io.OutputStream;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.springframework.ws.transport.TransportOutputStream;

/** @author Arjen Poutsma */
public class MailTransportOutputStream extends TransportOutputStream {

    private final Message message;

    public MailTransportOutputStream(Message message) {
        this.message = message;
    }

    public void addHeader(String name, String value) throws IOException {
        try {
            message.addHeader(name, value);
        }
        catch (MessagingException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    protected OutputStream createOutputStream() throws IOException {
        try {
            return message.getDataHandler().getOutputStream();
        }
        catch (MessagingException ex) {
            throw new IOException(ex.getMessage());
        }
    }

}
