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
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.support.EnumerationIterator;

/**
 * @author Arjen Poutsma
 */
public class MailTransportInputStream extends TransportInputStream {

    private final Message message;

    public MailTransportInputStream(Message message) {
        this.message = message;
    }

    protected InputStream createInputStream() throws IOException {
        try {
            return message.getDataHandler().getInputStream();
        }
        catch (MessagingException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    public Iterator getHeaders(String name) throws IOException {
        try {
            String[] headers = message.getHeader(name);
            return Arrays.asList(headers).iterator();
        }
        catch (MessagingException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    public Iterator getHeaderNames() throws IOException {
        try {
            return new EnumerationIterator(message.getAllHeaders());
        }
        catch (MessagingException ex) {
            throw new IOException(ex.getMessage());
        }
    }
}
