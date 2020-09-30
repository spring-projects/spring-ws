/*
 * Copyright 2005-2014 the original author or authors.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.transport.support.WebServiceMessageReceiverObjectSupport;

/**
 * Adapter to use the {@link WebServiceMessageReceiver} interface with the generic
 * {@link org.springframework.web.servlet.DispatcherServlet}. Requires a
 * {@link org.springframework.ws.WebServiceMessageFactory} which is used to convert the incoming
 * {@code HttpServletRequest} into a {@code WebServiceMessage}, and passes that context to the mapped
 * {@code WebServiceMessageReceiver}. If a response is created, that is sent via the {@code HttpServletResponse}.
 * <p>
 * Note that the {@code MessageDispatcher} implements the {@code WebServiceMessageReceiver} interface, enabling this
 * adapter to function as a gateway to further message handling logic.
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

	@Override
	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1L;
	}

	@Override
	public ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			Object handler) throws Exception {
		if (HttpTransportConstants.METHOD_POST.equals(httpServletRequest.getMethod())) {
			WebServiceConnection connection = new HttpServletConnection(httpServletRequest, httpServletResponse);
			try {
				handleConnection(connection, (WebServiceMessageReceiver) handler);
			} catch (InvalidXmlException ex) {
				handleInvalidXmlException(httpServletRequest, httpServletResponse, handler, ex);
			}
		} else {
			handleNonPostMethod(httpServletRequest, httpServletResponse, handler);
		}
		return null;
	}

	@Override
	public boolean supports(Object handler) {
		return handler instanceof WebServiceMessageReceiver;
	}

	/**
	 * Template method that is invoked when the request method is not {@code POST}. Called from
	 * {@link #handle(HttpServletRequest, HttpServletResponse, Object)}.
	 * <p>
	 * Default implementation set the response status to 405: Method Not Allowed. Can be overridden in subclasses.
	 *
	 * @param httpServletRequest current HTTP request
	 * @param httpServletResponse current HTTP response
	 * @param handler current handler
	 */
	protected void handleNonPostMethod(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			Object handler) throws Exception {
		httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 * Template method that is invoked when parsing the request results in a {@link InvalidXmlException}. Called from
	 * {@link #handle(HttpServletRequest, HttpServletResponse, Object)}.
	 * <p>
	 * Default implementation set the response status to 400: Bad Request. Can be overridden in subclasses.
	 *
	 * @param httpServletRequest current HTTP request
	 * @param httpServletResponse current HTTP response
	 * @param handler current handler
	 * @param ex the invalid XML exception that resulted in this method being called
	 */
	protected void handleInvalidXmlException(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, Object handler, InvalidXmlException ex) throws Exception {
		httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

}
