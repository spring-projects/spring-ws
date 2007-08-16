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

package org.springframework.ws.client.core;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.springframework.util.StringUtils;

/**
 * A simple Servlet that uses SAAJ to echo request.
 *
 * @author Arjen Poutsma
 * @since 1.0
 */
public class SimpleSaajServlet extends HttpServlet {

    private MessageFactory msgFactory = null;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        try {
            msgFactory = MessageFactory.newInstance();
        }
        catch (SOAPException ex) {
            throw new ServletException("Unable to create message factory" + ex.getMessage());
        }
    }

    private MimeHeaders getHeaders(HttpServletRequest httpServletRequest) {
        Enumeration enumeration = httpServletRequest.getHeaderNames();
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
        Iterator it = headers.getAllHeaders();
        while (it.hasNext()) {
            MimeHeader header = (MimeHeader) it.next();
            String[] values = headers.getHeader(header.getName());
            String value = StringUtils.arrayToCommaDelimitedString(values);
            res.setHeader(header.getName(), value);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            MimeHeaders headers = getHeaders(req);
            SOAPMessage request = msgFactory.createMessage(headers, req.getInputStream());
            SOAPMessage reply = onMessage(request);
            if (reply != null) {
                if (reply.saveRequired()) {
                    reply.saveChanges();
                }
                resp.setStatus(!reply.getSOAPBody().hasFault() ? HttpServletResponse.SC_OK :
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                putHeaders(reply.getMimeHeaders(), resp);
                reply.writeTo(resp.getOutputStream());
            }
            else {
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        }
        catch (Exception ex) {
            throw new ServletException("SAAJ POST failed " + ex.getMessage());
        }
    }

    protected SOAPMessage onMessage(SOAPMessage message) {
        return message;
    }


}
