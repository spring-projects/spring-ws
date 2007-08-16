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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ws.wsdl.WsdlDefinition;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

/**
 * Adapter to use the <code>WsdlDefinition</code> interface with the generic <code>DispatcherServlet</code>.
 * <p/>
 * Reads the source from the mapped <code>WsdlDefinition</code> implementation, and writes that as the result to the
 * <code>HttpServletResponse</code>.
 * <p/>
 * If the property <code>transformLocations</code> is set to <code>true</code>, this adapter will change
 * <code>location</code> attributes in the WSDL definition to reflect the URL of the incoming request. If the location
 * field in the original WSDL is an absolute path, the scheme, hostname, and port will be changed. If the location is a
 * relative path, the scheme, hostname, port, and context path will be prepended. This behavior can be customized by
 * overriding the <code>transformLocation()</code> method.
 * <p/>
 * For instance, if the location attribute defined in the WSDL is <code>http://localhost:8080/context/services/myService</code>,
 * and the request URI for the WSDL is <code>http://example.com/context/myService.wsdl</code>, the location will be
 * changed to <code>http://example.com/context/services/myService</code>.
 * <p/>
 * If the location attribute defined in the WSDL is <code>/services/myService</code>, and the request URI for the WSDL
 * is <code>http://example.com:8080/context/myService.wsdl</code>, the location will be changed to
 * <code>http://example.com:8080/context/services/myService</code>.
 * <p/>
 * When <code>transformLocations</code> is enabled, all <code>location</code> attributes found in the WSDL definition
 * are changed by default. This behavior can be customized by changing the <code>locationExpression</code> property,
 * which is an XPath expression that matches the attributes to change.
 *
 * @author Arjen Poutsma
 * @see WsdlDefinition
 * @see #setTransformLocations(boolean)
 * @see #setLocationExpression(String)
 * @see #transformLocation(String,javax.servlet.http.HttpServletRequest)
 * @since 1.0
 */
public class WsdlDefinitionHandlerAdapter extends TransformerObjectSupport implements HandlerAdapter, InitializingBean {

    /** Default XPath expression used for extracting all <code>location</code> attributes from the WSDL definition. */
    public static final String DEFAULT_LOCATION_EXPRESSION = "//@location";

    private static final String CONTENT_TYPE = "text/xml";

    private static final Log logger = LogFactory.getLog(WsdlDefinitionHandlerAdapter.class);

    private Properties expressionNamespaces = new Properties();

    private String locationExpression = DEFAULT_LOCATION_EXPRESSION;

    private XPathExpression locationXPathExpression;

    private boolean transformLocations = false;

    /**
     * Sets the XPath expression used for extracting the <code>location</code> attributes from the WSDL 1.1 definition.
     * <p/>
     * Defaults to <code>DEFAULT_LOCATION_EXPRESSION</code>.
     *
     * @see #DEFAULT_LOCATION_EXPRESSION
     */
    public void setLocationExpression(String locationExpression) {
        this.locationExpression = locationExpression;
    }

    /**
     * Sets whether relative address locations in the WSDL are to be transformed using the request URI of the incoming
     * <code>HttpServletRequest</code>. Defaults to <code>false</code>.
     */
    public void setTransformLocations(boolean transformLocations) {
        this.transformLocations = transformLocations;
    }

    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1;
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!"GET".equals(request.getMethod())) {
            throw new ServletException("Request method '" + request.getMethod() + "' not supported");
        }
        response.setContentType(CONTENT_TYPE);
        Transformer transformer = createTransformer();
        WsdlDefinition definition = (WsdlDefinition) handler;
        Source definitionSource = definition.getSource();
        if (transformLocations) {
            DOMResult domResult = new DOMResult();
            transformer.transform(definitionSource, domResult);
            Document definitionDocument = (Document) domResult.getNode();
            transformLocations(definitionDocument, request);
            definitionSource = new DOMSource(definitionDocument);
        }
        StreamResult responseResult = new StreamResult(response.getOutputStream());
        transformer.transform(definitionSource, responseResult);
        return null;
    }

    public boolean supports(Object handler) {
        return handler instanceof WsdlDefinition;
    }

    public void afterPropertiesSet() throws Exception {
        locationXPathExpression =
                XPathExpressionFactory.createXPathExpression(locationExpression, expressionNamespaces);
    }

    /**
     * Transform the given location string to reflect the given request. If the given location is a full url, the
     * scheme, server name, and port are changed. If it is a relative url, the scheme, server name, and port are
     * prepended. Can be overridden in subclasses to change this behavior.
     * <p/>
     * For instance, if the location attribute defined in the WSDL is <code>http://localhost:8080/context/services/myService</code>,
     * and the request URI for the WSDL is <code>http://example.com:8080/context/myService.wsdl</code>, the location
     * will be changed to <code>http://example.com:8080/context/services/myService</code>.
     * <p/>
     * If the location attribute defined in the WSDL is <code>/services/myService</code>, and the request URI for the
     * WSDL is <code>http://example.com:8080/context/myService.wsdl</code>, the location will be changed to
     * <code>http://example.com:8080/context/services/myService</code>.
     * <p/>
     * This method is only called when the <code>transformLocations</code> property is true.
     */
    protected String transformLocation(String location, HttpServletRequest request) {
        try {
            if (location.startsWith("/")) {
                // a relative path, prepend the context path
                URL newLocation = new URL(request.getScheme(), request.getServerName(), request.getServerPort(),
                        request.getContextPath() + location);
                return newLocation.toString();
            }
            else {
                // a full url
                URL oldLocation = new URL(location);
                URL newLocation = new URL(request.getScheme(), request.getServerName(), request.getServerPort(),
                        oldLocation.getFile());
                return newLocation.toString();
            }
        }
        catch (MalformedURLException e) {
            return location;
            // fall though to the default return value
        }
    }

    /**
     * Transforms all <code>location</code> attributes to reflect the server name given <code>HttpServletRequest</code>.
     * Determines the suitable attributes by evaluating the defined XPath expression, and delegates to
     * <code>transformLocation</code> to do the transformation for all attributes that match.
     * <p/>
     * This method is only called when the <code>transformLocations</code> property is true.
     *
     * @see #setLocationExpression(String)
     * @see #setTransformLocations(boolean)
     * @see #transformLocation(String,javax.servlet.http.HttpServletRequest)
     */
    protected void transformLocations(Document definitionDocument, HttpServletRequest request) throws Exception {
        List locationNodes = locationXPathExpression.evaluateAsNodeList(definitionDocument);
        for (Iterator iterator = locationNodes.iterator(); iterator.hasNext();) {
            Attr location = (Attr) iterator.next();
            if (location != null && StringUtils.hasLength(location.getValue())) {
                String newLocation = transformLocation(location.getValue(), request);
                logger.debug("Transforming [" + location.getValue() + "] to [" + newLocation + "]");
                location.setValue(newLocation);
            }
        }
    }
}
