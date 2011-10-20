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

package org.springframework.ws.transport.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;

import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Adapter to use the {@link XsdSchema} interface with the generic <code>DispatcherServlet</code>.
 * <p/>
 * Reads the source from the mapped {@link XsdSchema} implementation, and writes that as the result to the
 * <code>HttpServletResponse</code>. Allows for post-processing the schema in subclasses.
 *
 * @author Arjen Poutsma
 * @see XsdSchema
 * @see #getSchemaSource(XsdSchema)
 * @since 1.5.3
 */
public class XsdSchemaHandlerAdapter extends TransformerObjectSupport implements HandlerAdapter {

    private static final String CONTENT_TYPE = "text/xml";

    public boolean supports(Object handler) {
        return handler instanceof XsdSchema;
    }

    public long getLastModified(HttpServletRequest request, Object handler) {
        Source schemaSource = ((XsdSchema) handler).getSource();
        return LastModifiedHelper.getLastModified(schemaSource);
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (HttpTransportConstants.METHOD_GET.equals(request.getMethod())) {
            response.setContentType(CONTENT_TYPE);
            Transformer transformer = createTransformer();
            Source schemaSource = getSchemaSource((XsdSchema) handler);
            StreamResult responseResult = new StreamResult(response.getOutputStream());
            transformer.transform(schemaSource, responseResult);
        }
        else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        return null;
    }

    /**
     * Returns the {@link Source} of the given schema. Allows for post-processing and transformation of the schema in
     * sub-classes.
     * <p/>
     * Default implementation simply returns {@link XsdSchema#getSource()}.
     *
     * @param schema the schema
     * @return the source of the given schema
     * @throws Exception in case of errors
     */
    protected Source getSchemaSource(XsdSchema schema) throws Exception {
        return schema.getSource();
    }

}