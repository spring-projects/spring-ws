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

package org.springframework.ws.mock.client2;

import org.springframework.core.NamedThreadLocal;

/**
 * @author Arjen Poutsma
 * @since 2.0
 */
class MockWebServiceMessageSenderHolder {

    private static final NamedThreadLocal<MockWebServiceMessageSender> mockWebServiceMessageSenderHolder =
            new NamedThreadLocal<MockWebServiceMessageSender>("Mock Message Sender");

    /** Associate the given {@link MockWebServiceMessageSender} with the current thread. */
    public static void set(MockWebServiceMessageSender messageSender) {
        mockWebServiceMessageSenderHolder.set(messageSender);
    }

    /** Return the {@link MockWebServiceMessageSender} associated with the current thread, if any. */
    public static MockWebServiceMessageSender get() {
        return mockWebServiceMessageSenderHolder.get();
    }

    /**
     * Clears the holder.
     */
    public static void clear() {
        set(null);
    }

}
