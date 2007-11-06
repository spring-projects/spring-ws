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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.springframework.util.Assert;
import org.springframework.ws.transport.support.ParameterizedUri;

/**
 * @author Arjen Poutsma
 */
public class MailtoUri extends ParameterizedUri {

    public MailtoUri(String uri) {
        super(uri);
        Assert.isTrue(uri.startsWith(MailTransportConstants.URI_SCHEME), "Invalid uri: " + uri);
        try {
            InternetAddress.parse(getDestination(), false);
        }
        catch (AddressException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public InternetAddress getTo() throws AddressException {
        return new InternetAddress(getDestination());
    }

    public String getSubject() {
        return getParameter("subject");
    }

    public boolean hasSubject() {
        return hasParameter("subject");
    }

    public boolean hasCc() {
        return hasParameter("cc");
    }

    public InternetAddress getCc() throws AddressException {
        return new InternetAddress(getParameter("cc"));
    }
}
