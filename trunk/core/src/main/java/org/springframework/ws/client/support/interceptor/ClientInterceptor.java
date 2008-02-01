/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.client.support.interceptor;

import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Workflow interface that allows for customized client-side message interception. Applications can register any number
 * of existing or custom interceptors on a {@link WebServiceTemplate}, to add common pre- and postprocessing behavior
 * without needing to modify payload handling code.
 * <p/>
 * A <code>ClientInterceptor</code> gets called after payload creation (using {@link
 * WebServiceTemplate#marshalSendAndReceive(Object)} or similar methods, and after {@link WebServiceMessageCallback
 * callback} invocation, but before the message is sent over the {@link WebServiceConnection}. This mechanism can be
 * used for a large field of preprocessing aspects, e.g. for authorization checks, or message header checks. Its main
 * purpose is to allow for factoring out meta-data (i.e. {@link SoapHeader}) related code.
 * <p/>
 * Client interceptors are defined on a {@link WebServiceTemplate}, using the {@link
 * WebServiceTemplate#setInterceptors(ClientInterceptor[]) interceptors} property.
 *
 * @author Giovanni Cuccu
 * @author Arjen Poutsma
 * @see WebServiceTemplate#setInterceptors(ClientInterceptor[])
 * @since 1.5.0
 */
public interface ClientInterceptor {

    boolean handleRequest(MessageContext messageContext) throws WebServiceClientException;

    boolean handleResponse(MessageContext messageContext) throws WebServiceClientException;

    boolean handleFault(MessageContext messageContext) throws WebServiceClientException;

}
