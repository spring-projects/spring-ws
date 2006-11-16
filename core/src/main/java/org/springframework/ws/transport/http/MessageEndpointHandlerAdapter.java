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

package org.springframework.ws.transport.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.endpoint.MessageEndpoint;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.DefaultTransportContext;
import org.springframework.ws.transport.TransportContext;
import org.springframework.ws.transport.TransportContextHolder;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * Adapter to use the <code>MessageEndpoint</code> interface with the generic <code>DispatcherServlet</code>. Requires a
 * {@link WebServiceMessageFactory}, which is used to convert the incoming <code>HttpServletRequest</code> into a {@link
 * WebServiceMessage}, and passes that context to the mapped <code>MessageEndpoint</code>. If a response is created,
 * that is sent via the <code>HttpServletResponse</code>.
 * <p/>
 * Note that the <code>MessageDispatcher</code> implements the <code>MessageEndpoint</code> interface, enabling this
 * adapter to function as a gateway to further message handling logic.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.endpoint.MessageEndpoint
 * @see org.springframework.ws.MessageDispatcher
 */
public class MessageEndpointHandlerAdapter implements HandlerAdapter, InitializingBean {

    private static final Log logger = LogFactory.getLog(MessageEndpointHandlerAdapter.class);

    private WebServiceMessageFactory messageFactory;

    public void setMessageFactory(WebServiceMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1L;
    }

    public ModelAndView handle(HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse,
                               Object handler) throws Exception {
        if ("POST".equals(httpServletRequest.getMethod())) {
            handlePost(httpServletRequest, (MessageEndpoint) handler, httpServletResponse);
            return null;
        }
        else {
            throw new ServletException("Request method '" + httpServletRequest.getMethod() + "' not supported");
        }
    }

    public boolean supports(Object handler) {
        return handler instanceof MessageEndpoint;
    }

    public final void afterPropertiesSet() throws Exception {
        Assert.notNull(messageFactory, "messageFactory is required");
        logger.info("Using message factory [" + messageFactory + "]");
    }

    private void handlePost(HttpServletRequest httpServletRequest,
                            MessageEndpoint endpoint,
                            HttpServletResponse httpServletResponse) throws Exception {
        TransportInputStream tis = new HttpServletTransportInputStream(httpServletRequest);
        TransportOutputStream tos = new HttpServletTransportOutputStream(httpServletResponse);

        TransportContext previousTransportContext = TransportContextHolder.getTransportContext();
        TransportContextHolder.setTransportContext(new DefaultTransportContext(tis, tos));

        try {
            WebServiceMessage messageRequest = messageFactory.createWebServiceMessage(tis);
            MessageContext messageContext = new DefaultMessageContext(messageRequest, messageFactory);
            endpoint.invoke(messageContext);
            if (!messageContext.hasResponse()) {
                httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            else {
                WebServiceMessage messageResponse = messageContext.getResponse();
                if (messageResponse instanceof SoapMessage &&
                        ((SoapMessage) messageResponse).getSoapBody().hasFault()) {
                    httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                else {
                    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                }
                messageResponse.writeTo(tos);
            }
        }
        catch (NoEndpointFoundException ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        finally {
            TransportContextHolder.setTransportContext(previousTransportContext);
        }
    }
}
