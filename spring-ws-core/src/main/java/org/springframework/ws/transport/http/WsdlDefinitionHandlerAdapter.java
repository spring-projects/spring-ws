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

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ws.wsdl.WsdlDefinition;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;

/**
 * Adapter to use the {@code WsdlDefinition} interface with the generic {@code DispatcherServlet}.
 *
 * <p>Reads the source from the mapped {@code WsdlDefinition} implementation, and writes that as the result to the
 * {@code HttpServletResponse}.
 *
 * <p>If the property {@code transformLocations} is set to {@code true}, this adapter will change
 * {@code location} attributes in the WSDL definition to reflect the URL of the incoming request. If the location
 * field in the original WSDL is an absolute path, the scheme, hostname, and port will be changed. If the location is a
 * relative path, the scheme, hostname, port, and context path will be prepended. This behavior can be customized by
 * overriding the {@code transformLocation()} method.
 *
 * <p>For instance, if the location attribute defined in the WSDL is {@code http://localhost:8080/context/services/myService},
 * and the request URI for the WSDL is {@code http://example.com/context/myService.wsdl}, the location will be
 * changed to {@code http://example.com/context/services/myService}.
 *
 * <p>If the location attribute defined in the WSDL is {@code /services/myService}, and the request URI for the WSDL
 * is {@code http://example.com:8080/context/myService.wsdl}, the location will be changed to
 * {@code http://example.com:8080/context/services/myService}.
 *
 * <p>When {@code transformLocations} is enabled, all {@code location} attributes found in the WSDL definition
 * are changed by default. This behavior can be customized by changing the {@code locationExpression} property,
 * which is an XPath expression that matches the attributes to change.
 *
 * @author Arjen Poutsma
 * @see WsdlDefinition
 * @see #setTransformLocations(boolean)
 * @see #setLocationExpression(String)
 * @see #transformLocation(String,javax.servlet.http.HttpServletRequest)
 * @since 1.0.0
 */
public class WsdlDefinitionHandlerAdapter extends LocationTransformerObjectSupport implements HandlerAdapter, InitializingBean {

    /** Default XPath expression used for extracting all {@code location} attributes from the WSDL definition. */
    public static final String DEFAULT_LOCATION_EXPRESSION = "//@location";

    /** Default XPath expression used for extracting all {@code schemaLocation} attributes from the WSDL definition. */
    public static final String DEFAULT_SCHEMA_LOCATION_EXPRESSION = "//@schemaLocation";

    private static final String CONTENT_TYPE = "text/xml";

    private Map<String, String> expressionNamespaces = new HashMap<String, String>();

    private String locationExpression = DEFAULT_LOCATION_EXPRESSION;

    private String schemaLocationExpression = DEFAULT_SCHEMA_LOCATION_EXPRESSION;

    private XPathExpression locationXPathExpression;

    private XPathExpression schemaLocationXPathExpression;

    private boolean transformLocations = false;

    private boolean transformSchemaLocations = false;

    /**
     * Sets the XPath expression used for extracting the {@code location} attributes from the WSDL 1.1 definition.
     *
     * <p>Defaults to {@code DEFAULT_LOCATION_EXPRESSION}.
     */
    public void setLocationExpression(String locationExpression) {
        this.locationExpression = locationExpression;
    }

    /**
     * Sets the XPath expression used for extracting the {@code schemaLocation} attributes from the WSDL 1.1 definition.
     *
     * <p>Defaults to {@code DEFAULT_SCHEMA_LOCATION_EXPRESSION}.
     */
    public void setSchemaLocationExpression(String schemaLocationExpression) {
        this.schemaLocationExpression = schemaLocationExpression;
    }

    /**
     * Sets whether relative address locations in the WSDL are to be transformed using the request URI of the incoming
     * {@code HttpServletRequest}. Defaults to {@code false}.
     */
    public void setTransformLocations(boolean transformLocations) {
        this.transformLocations = transformLocations;
    }

    /**
     * Sets whether relative address schema locations in the WSDL are to be transformed using the request URI of the
     * incoming {@code HttpServletRequest}. Defaults to {@code false}.
     */
    public void setTransformSchemaLocations(boolean transformSchemaLocations) {
        this.transformSchemaLocations = transformSchemaLocations;
    }

    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        Source definitionSource = ((WsdlDefinition) handler).getSource();
        return LastModifiedHelper.getLastModified(definitionSource);
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (HttpTransportConstants.METHOD_GET.equals(request.getMethod())) {
            WsdlDefinition definition = (WsdlDefinition) handler;

            Transformer transformer = createTransformer();
            Source definitionSource = definition.getSource();

            if (transformLocations || transformSchemaLocations) {
                DOMResult domResult = new DOMResult();
                transformer.transform(definitionSource, domResult);
                Document definitionDocument = (Document) domResult.getNode();
                if (transformLocations) {
                    transformLocations(definitionDocument, request);
                }
                if (transformSchemaLocations) {
                    transformSchemaLocations(definitionDocument, request);
                }
                definitionSource = new DOMSource(definitionDocument);
            }

            response.setContentType(CONTENT_TYPE);
            StreamResult responseResult = new StreamResult(response.getOutputStream());
            transformer.transform(definitionSource, responseResult);
        }
        else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        return null;
    }

    @Override
    public boolean supports(Object handler) {
        return handler instanceof WsdlDefinition;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        locationXPathExpression =
                XPathExpressionFactory.createXPathExpression(locationExpression, expressionNamespaces);
        schemaLocationXPathExpression =
                XPathExpressionFactory.createXPathExpression(schemaLocationExpression, expressionNamespaces);
    }

    /**
     * Transforms all {@code location} attributes to reflect the server name given {@code HttpServletRequest}.
     * Determines the suitable attributes by evaluating the defined XPath expression, and delegates to
     * {@code transformLocation} to do the transformation for all attributes that match.
     *
     * <p>This method is only called when the {@code transformLocations} property is true.
     *
     * @see #setLocationExpression(String)
     * @see #setTransformLocations(boolean)
     * @see #transformLocation(String,javax.servlet.http.HttpServletRequest)
     */
    protected void transformLocations(Document definitionDocument, HttpServletRequest request) throws Exception {
        transformLocations(locationXPathExpression, definitionDocument, request);
    }

    /**
     * Transforms all {@code schemaLocation} attributes to reflect the server name given {@code HttpServletRequest}.
     * Determines the suitable attributes by evaluating the defined XPath expression, and delegates to
     * {@code transformLocation} to do the transformation for all attributes that match.
     *
     * <p>This method is only called when the {@code transformSchemaLocations} property is true.
     *
     * @see #setSchemaLocationExpression(String)
     * @see #setTransformSchemaLocations(boolean)
     * @see #transformLocation(String,javax.servlet.http.HttpServletRequest)
     */
    protected void transformSchemaLocations(Document definitionDocument, HttpServletRequest request) throws Exception {
        transformLocations(schemaLocationXPathExpression, definitionDocument, request);
    }

}
