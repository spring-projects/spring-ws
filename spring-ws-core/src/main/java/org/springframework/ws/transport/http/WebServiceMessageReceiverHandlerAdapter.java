/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;

import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.transport.support.WebServiceMessageReceiverObjectSupport;

/**
 * Adapter to use the {@link WebServiceMessageReceiver} interface with the generic
 * {@link org.springframework.web.servlet.DispatcherServlet}. Requires a
 * {@link org.springframework.ws.WebServiceMessageFactory} which is used to convert the
 * incoming {@code HttpServletRequest} into a {@code WebServiceMessage}, and passes that
 * context to the mapped {@code WebServiceMessageReceiver}. If a response is created, that
 * is sent via the {@code HttpServletResponse}.
 * <p>
 * Note that the {@code MessageDispatcher} implements the
 * {@code WebServiceMessageReceiver} interface, enabling this adapter to function as a
 * gateway to further message handling logic.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #setMessageFactory(org.springframework.ws.WebServiceMessageFactory)
 * @see org.springframework.ws.transport.WebServiceMessageReceiver
 * @see org.springframework.ws.WebServiceMessageFactory
 * @see org.springframework.ws.server.MessageDispatcher
 */
public class WebServiceMessageReceiverHandlerAdapter extends WebServiceMessageReceiverObjectSupport
		implements HandlerAdapter {

	@Override
	public @Nullable ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			Object handler) throws Exception {
		if (HttpTransportConstants.METHOD_POST.equals(httpServletRequest.getMethod())) {
			WebServiceConnection connection = new HttpServletConnection(httpServletRequest, httpServletResponse);
			try {
				handleConnection(connection, (WebServiceMessageReceiver) handler);
			}
			catch (InvalidXmlException ex) {
				handleInvalidXmlException(httpServletRequest, httpServletResponse, handler, ex);
			}
		}
		else {
			handleNonPostMethod(httpServletRequest, httpServletResponse, handler);
		}
		return null;
	}

	@Override
	public boolean supports(Object handler) {
		return handler instanceof WebServiceMessageReceiver;
	}

	/**
	 * Template method that is invoked when the request method is not {@code POST}. Called
	 * from {@link #handle(HttpServletRequest, HttpServletResponse, Object)}.
	 * <p>
	 * Default implementation set the response status to 405: Method Not Allowed. Can be
	 * overridden in subclasses.
	 * @param httpServletRequest current HTTP request
	 * @param httpServletResponse current HTTP response
	 * @param handler current handler
	 */
	protected void handleNonPostMethod(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			Object handler) throws Exception {
		httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 * Template method that is invoked when parsing the request results in a
	 * {@link InvalidXmlException}. Called from
	 * {@link #handle(HttpServletRequest, HttpServletResponse, Object)}.
	 * <p>
	 * Default implementation set the response status to 400: Bad Request. Can be
	 * overridden in subclasses.
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
