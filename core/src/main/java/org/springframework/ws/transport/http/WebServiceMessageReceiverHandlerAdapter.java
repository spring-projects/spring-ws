/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.transport.support.WebServiceMessageReceiverObjectSupport;

/**
 * Adapter to use the {@link WebServiceMessageReceiver} interface with the generic {@link
 * org.springframework.web.servlet.DispatcherServlet}. Requires a {@link org.springframework.ws.WebServiceMessageFactory}
 * which is used to convert the incoming <code>HttpServletRequest</code> into a <code>WebServiceMessage</code>, and
 * passes that context to the mapped <code>WebServiceMessageReceiver</code>. If a response is created, that is sent via
 * the <code>HttpServletResponse</code>.
 * <p/>
 * Note that the <code>MessageDispatcher</code> implements the <code>WebServiceMessageReceiver</code> interface,
 * enabling this adapter to function as a gateway to further message handling logic.
 *
 * @author Arjen Poutsma
 * @see #setMessageFactory(org.springframework.ws.WebServiceMessageFactory)
 * @see org.springframework.ws.transport.WebServiceMessageReceiver
 * @see org.springframework.ws.WebServiceMessageFactory
 * @see org.springframework.ws.server.MessageDispatcher
 * @since 1.0.0
 */
public class WebServiceMessageReceiverHandlerAdapter extends WebServiceMessageReceiverObjectSupport
        implements HandlerAdapter {

    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1L;
    }

    public ModelAndView handle(HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse,
                               Object handler) throws Exception {
        if (HttpTransportConstants.METHOD_POST.equals(httpServletRequest.getMethod())) {
            WebServiceConnection connection = new HttpServletConnection(httpServletRequest, httpServletResponse);
            try {
                handleConnection(connection, (WebServiceMessageReceiver) handler);
            }
            catch (InvalidXmlException ex) {
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else {
            httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        return null;
    }

    public boolean supports(Object handler) {
        return handler instanceof WebServiceMessageReceiver;
    }

}
