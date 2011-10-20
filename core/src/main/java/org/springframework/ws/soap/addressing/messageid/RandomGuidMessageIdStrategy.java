/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.addressing.messageid;

import java.net.URI;

import org.springframework.ws.soap.SoapMessage;

/**
 * Implementation of the {@link MessageIdStrategy} interface that uses a {@link RandomGuid} to generate a Message Id.
 * The GUID is prefixed by <code>urn:guid:</code>.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 * @deprecated as of Spring Web Services 2.0, in favor of {@link UuidMessageIdStrategy}
 */
@Deprecated
public class RandomGuidMessageIdStrategy implements MessageIdStrategy {

    public static final String PREFIX = "urn:guid:";

    private boolean secure;

    /**
     * Sets whether or not the generated random numbers should be <i>secure</i>. If set to <code>true</code>, generated
     * GUIDs are cryptographically strong.
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /** Returns <code>false</code>. */
    public boolean isDuplicate(URI messageId) {
        return false;
    }

    public URI newMessageId(SoapMessage message) {
        return URI.create(PREFIX + new RandomGuid(secure).toString());
    }
}