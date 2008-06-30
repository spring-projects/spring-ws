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

package org.springframework.ws.transport.http;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.springframework.xml.transform.TraxUtils;
import org.springframework.xml.xsd.XsdSchema;

/**
 * @author Arjen Poutsma
 * @since 1.5.3
 */
public class XsdSchemaHandlerAdapter extends TransformerObjectSupport implements HandlerAdapter {

    private static final String CONTENT_TYPE = "text/xml";

    public boolean supports(Object handler) {
        return handler instanceof XsdSchema;
    }

    public long getLastModified(HttpServletRequest request, Object handler) {
        Source schemaSource = ((XsdSchema) handler).getSource();
        if (schemaSource instanceof DOMSource) {
            Document document = TraxUtils.getDocument((DOMSource) schemaSource);
            return document != null ? getLastModified(document.getDocumentURI()) : -1;
        }
        else {
            return getLastModified(schemaSource.getSystemId());
        }
    }

    private long getLastModified(String systemId) {
        if (StringUtils.hasText(systemId)) {
            try {
                URI systemIdUri = new URI(systemId);
                if ("file".equals(systemIdUri.getScheme())) {
                    File documentFile = new File(systemIdUri);
                    if (documentFile.exists()) {
                        return documentFile.lastModified();
                    }
                }
            }
            catch (URISyntaxException e) {
                // ignore
            }
        }
        return -1;
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (HttpTransportConstants.METHOD_GET.equals(request.getMethod())) {
            response.setContentType(CONTENT_TYPE);
            Transformer transformer = createTransformer();
            XsdSchema schema = (XsdSchema) handler;
            Source schemaSource = schema.getSource();
            StreamResult responseResult = new StreamResult(response.getOutputStream());
            transformer.transform(schemaSource, responseResult);
        }
        else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        return null;
    }
}