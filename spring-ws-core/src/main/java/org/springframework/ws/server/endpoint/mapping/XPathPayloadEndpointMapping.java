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

package org.springframework.ws.server.endpoint.mapping;

import java.util.Map;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Element;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;

/**
 * Implementation of the {@code EndpointMapping} interface that maps to endpoint using an XPath expression.
 * Supports both mapping to bean instances and mapping to bean names: the latter is required for prototype endpoints.
 *
 * <p>The XPath expression can be set using the {@code expression} property. Setting this property is required. There
 * is also an optional {@code namespaces} property, which defines to set namespace bindings that are used in the
 * expression.
 *
 * <p>The {@code endpointMap} property is suitable for populating the endpoint map with bean references, e.g. via the
 * map element in XML bean definitions.
 *
 * <p>Mappings to bean names can be set via the {@code mappings} property, in a form accepted by the
 * {@code java.util.Properties} class, like as follows:
 * <pre>
 * BookFlight=bookFlightEndpoint
 * GetFlights=getFlightsEndpoint
 * </pre>
 * The syntax is XPATH_EVALUATION=ENDPOINT_BEAN_NAME. The key is the evaluation of the XPath expression for the incoming
 * message, the value is the name of the endpoint.
 *
 * @author Arjen Poutsma
 * @see #setExpression(String)
 * @see #setNamespaces(java.util.Map) 
 * @since 1.0.0
 */
public class XPathPayloadEndpointMapping extends AbstractMapBasedEndpointMapping implements InitializingBean {

	private String expressionString;

	private XPathExpression expression;

	private Map<String, String> namespaces;

	private TransformerFactory transformerFactory;

	/** Sets the XPath expression to be used. */
	public void setExpression(String expression) {
		expressionString = expression;
	}

	/** Sets the namespaces bindings used in the expression. Keys are prefixes, values are namespaces. */
	public void setNamespaces(Map<String, String> namespaces) {
		this.namespaces = namespaces;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(expressionString, "expression is required");
		if (namespaces == null) {
			expression = XPathExpressionFactory.createXPathExpression(expressionString);
		}
		else {
			expression = XPathExpressionFactory.createXPathExpression(expressionString, namespaces);
		}
		transformerFactory = TransformerFactory.newInstance();
	}

	@Override
	protected String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
		Element payloadElement = getMessagePayloadElement(messageContext.getRequest());
		return expression.evaluateAsString(payloadElement);
	}

	private Element getMessagePayloadElement(WebServiceMessage message) throws TransformerException {
		Transformer transformer = transformerFactory.newTransformer();
		DOMResult domResult = new DOMResult();
		transformer.transform(message.getPayloadSource(), domResult);
		return (Element) domResult.getNode().getFirstChild();
	}

	@Override
	protected boolean validateLookupKey(String key) {
		return StringUtils.hasLength(key);
	}
}
