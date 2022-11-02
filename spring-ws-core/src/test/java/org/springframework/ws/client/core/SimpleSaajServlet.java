/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.client.core;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeader;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.springframework.util.StringUtils;

/**
 * A simple Servlet that uses SAAJ to echo request.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SimpleSaajServlet extends HttpServlet {

	private MessageFactory msgFactory = null;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {

		super.init(servletConfig);

		try {
			msgFactory = MessageFactory.newInstance();
		} catch (SOAPException ex) {
			throw new ServletException("Unable to create message factory" + ex.getMessage());
		}
	}

	private MimeHeaders getHeaders(HttpServletRequest httpServletRequest) {

		Enumeration<?> enumeration = httpServletRequest.getHeaderNames();
		MimeHeaders headers = new MimeHeaders();

		while (enumeration.hasMoreElements()) {
			String headerName = (String) enumeration.nextElement();
			String headerValue = httpServletRequest.getHeader(headerName);
			StringTokenizer values = new StringTokenizer(headerValue, ",");
			while (values.hasMoreTokens()) {
				headers.addHeader(headerName, values.nextToken().trim());
			}
		}

		return headers;
	}

	private void putHeaders(MimeHeaders headers, HttpServletResponse res) {

		Iterator<?> it = headers.getAllHeaders();

		while (it.hasNext()) {
			MimeHeader header = (MimeHeader) it.next();
			String[] values = headers.getHeader(header.getName());
			String value = StringUtils.arrayToCommaDelimitedString(values);
			res.setHeader(header.getName(), value);
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

		try {
			MimeHeaders headers = getHeaders(req);
			SOAPMessage request = msgFactory.createMessage(headers, req.getInputStream());
			SOAPMessage reply = onMessage(request);

			if (reply != null) {
				if (reply.saveRequired()) {
					reply.saveChanges();
				}
				resp.setStatus(
						!reply.getSOAPBody().hasFault() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				putHeaders(reply.getMimeHeaders(), resp);
				reply.writeTo(resp.getOutputStream());
			} else {
				resp.setStatus(HttpServletResponse.SC_ACCEPTED);
			}
		} catch (Exception ex) {
			throw new ServletException("SAAJ POST failed " + ex.getMessage());
		}
	}

	protected SOAPMessage onMessage(SOAPMessage message) {
		return message;
	}
}
