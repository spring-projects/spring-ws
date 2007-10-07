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

package org.springframework.ws.soap.addressing.messageid;

import java.rmi.server.UID;

import org.springframework.ws.soap.SoapMessage;

/**
 * Implementation of the {@link MessageIdStrategy} interface that uses a {@link UID} to generate a Message Id. The UID
 * is prefixed by <code>uid:</code>.
 *
 * @author Arjen Poutsma
 */
public class UidMessageIdStrategy implements MessageIdStrategy {

    public static final String PREFIX = "uid:";

    /** Returns <code>false</code>. */
    public boolean isDuplicate(String messageId) {
        return false;
    }

    public String newMessageId(SoapMessage message) {
        return PREFIX + new UID().toString();
    }
}
