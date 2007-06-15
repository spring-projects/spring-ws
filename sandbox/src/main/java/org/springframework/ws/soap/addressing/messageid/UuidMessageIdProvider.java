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

import java.util.UUID;

import org.springframework.ws.soap.SoapMessage;

/**
 * Implementation of the {@link MessageIdProvider} interface that uses a {@link UUID} to generate a Message Id. The UUID
 * is prefixed by <code>uuid:</code>.
 * <p/>
 * Note that the {@link UUID} class is only available on Java 5 and above.
 *
 * @author Arjen Poutsma
 * @see java.util.UUID
 */
public class UuidMessageIdProvider implements MessageIdProvider {

    public static final String PREFIX = "uuid:";

    public String getMessageId(SoapMessage message) {
        return PREFIX + UUID.randomUUID().toString();
    }
}