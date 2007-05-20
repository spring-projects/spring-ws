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

package org.springframework.ws.server.endpoint.interceptor;

import javax.xml.transform.Source;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.server.endpoint.AbstractLoggingInterceptor;

/**
 * Simple <code>EndpointInterceptor</code> that logs the payload of request and response messages.  By default, both
 * request and response messages are logged, but this behaviour can be changed using the <code>logRequest</code> and
 * <code>logResponse</code> properties.
 *
 * @author Arjen Poutsma
 * @see #setLogRequest(boolean)
 * @see #setLogResponse(boolean)
 */
public class PayloadLoggingInterceptor extends AbstractLoggingInterceptor {

    protected Source getSource(WebServiceMessage message) {
        return message.getPayloadSource();
    }
}
