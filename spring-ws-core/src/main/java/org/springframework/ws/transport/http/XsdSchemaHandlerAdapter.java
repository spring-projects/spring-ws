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
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Adapter to use the {@link XsdSchema} interface with the generic {@code DispatcherServlet}.
 *
 * <p>Reads the source from the mapped {@link XsdSchema} implementation, and writes that as the result to the
 * {@code HttpServletResponse}. Allows for post-processing the schema in subclasses.
 *
 * @author Arjen Poutsma
 * @see XsdSchema
 * @see #getSchemaSource(XsdSchema)
 * @since 1.5.3
 */
public class XsdSchemaHandlerAdapter extends LocationTransformerObjectSupport
		implements HandlerAdapter, InitializingBean {

	/**
	 * Default XPath expression used for extracting all {@code schemaLocation} attributes from the WSDL definition.
	 */
	public static final String DEFAULT_SCHEMA_LOCATION_EXPRESSION = "//@schemaLocation";

	private static final String CONTENT_TYPE = "text/xml";

	private Map<String, String> expressionNamespaces = new HashMap<String, String>();

	private String schemaLocationExpression = DEFAULT_SCHEMA_LOCATION_EXPRESSION;

	private XPathExpression schemaLocationXPathExpression;

	private boolean transformSchemaLocations = false;

	/**
	 * Sets the XPath expression used for extracting the {@code schemaLocation} attributes from the WSDL 1.1 definition.
	 *
	 * <p>Defaults to {@code DEFAULT_SCHEMA_LOCATION_EXPRESSION}.
	 */
	public void setSchemaLocationExpression(String schemaLocationExpression) {
		this.schemaLocationExpression = schemaLocationExpression;
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
		Source schemaSource = ((XsdSchema) handler).getSource();
		return LastModifiedHelper.getLastModified(schemaSource);
	}

	@Override
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (HttpTransportConstants.METHOD_GET.equals(request.getMethod())) {
			Transformer transformer = createTransformer();
			Source schemaSource = getSchemaSource((XsdSchema) handler);

			if (transformSchemaLocations) {
				DOMResult domResult = new DOMResult();
				transformer.transform(schemaSource, domResult);
				Document schemaDocument = (Document) domResult.getNode();
				transformSchemaLocations(schemaDocument, request);
				schemaSource = new DOMSource(schemaDocument);
			}

			response.setContentType(CONTENT_TYPE);
			StreamResult responseResult = new StreamResult(response.getOutputStream());
			transformer.transform(schemaSource, responseResult);
		}
		else {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
		return null;
	}

	@Override
	public boolean supports(Object handler) {
		return handler instanceof XsdSchema;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		schemaLocationXPathExpression =
				XPathExpressionFactory.createXPathExpression(schemaLocationExpression, expressionNamespaces);
	}

	/**
	 * Returns the {@link Source} of the given schema. Allows for post-processing and transformation of the schema in
	 * sub-classes.
	 *
	 * <p>Default implementation simply returns {@link XsdSchema#getSource()}.
	 *
	 * @param schema the schema
	 * @return the source of the given schema
	 * @throws Exception in case of errors
	 */
	protected Source getSchemaSource(XsdSchema schema) throws Exception {
		return schema.getSource();
	}

	/**
	 * Transforms all {@code schemaLocation} attributes to reflect the server name given {@code HttpServletRequest}.
	 * Determines the suitable attributes by evaluating the defined XPath expression, and delegates to {@code
	 * transformLocation} to do the transformation for all attributes that match.
	 *
	 * <p>This method is only called when the {@code transformSchemaLocations} property is true.
	 *
	 * @see #setSchemaLocationExpression(String)
	 * @see #transformLocation(String, javax.servlet.http.HttpServletRequest)
	 */
	protected void transformSchemaLocations(Document definitionDocument, HttpServletRequest request) throws Exception {
		transformLocations(schemaLocationXPathExpression, definitionDocument, request);
	}


}