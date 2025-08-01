/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.client.support.destination;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.WebServiceTransformerException;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;

/**
 * Implementation of the {@link DestinationProvider} that resolves a destination URI from
 * a WSDL file.
 * <p>
 * The extraction relies on an XPath expression to locate the URI. By default, the
 * {@link #DEFAULT_WSDL_LOCATION_EXPRESSION} will be used, but this expression can be
 * overridden by setting the {@link #setLocationExpression(String) locationExpression}
 * property.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @since 1.5.4
 */
public class Wsdl11DestinationProvider extends AbstractCachingDestinationProvider {

	/**
	 * Default XPath expression used for extracting all {@code location} attributes from
	 * the WSDL definition.
	 */
	public static final String DEFAULT_WSDL_LOCATION_EXPRESSION = "/wsdl:definitions/wsdl:service/wsdl:port/soap:address/@location";

	private static final TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();

	private final Map<String, String> expressionNamespaces = new HashMap<>();

	private XPathExpression locationXPathExpression;

	private @Nullable Resource wsdlResource;

	public Wsdl11DestinationProvider() {
		this.expressionNamespaces.put("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		this.expressionNamespaces.put("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
		this.expressionNamespaces.put("soap12", "http://schemas.xmlsoap.org/wsdl/soap12/");

		this.locationXPathExpression = XPathExpressionFactory.createXPathExpression(DEFAULT_WSDL_LOCATION_EXPRESSION,
				this.expressionNamespaces);
	}

	/**
	 * Sets a WSDL location from which the service destination {@code URI} will be
	 * resolved.
	 */
	public void setWsdl(Resource wsdlResource) {
		Assert.notNull(wsdlResource, "'wsdl' must not be null");
		Assert.isTrue(wsdlResource.exists(), wsdlResource + " does not exist");
		this.wsdlResource = wsdlResource;
	}

	/**
	 * Sets the XPath expression to use when extracting the service location {@code URI}
	 * from a WSDL.
	 * <p>
	 * The expression can use the following bound prefixes: <blockquote>
	 * <table>
	 * <caption>Bound Prefixes</caption>
	 * <tr>
	 * <th>Prefix</th>
	 * <th>Namespace</th>
	 * </tr>
	 * <tr>
	 * <td>{@code wsdl}</td>
	 * <td>{@code http://schemas.xmlsoap.org/wsdl/}</td>
	 * </tr>
	 * <tr>
	 * <td>{@code soap}</td>
	 * <td>{@code http://schemas.xmlsoap.org/wsdl/soap/}</td>
	 * </tr>
	 * <tr>
	 * <td>{@code soap12}</td>
	 * <td>{@code http://schemas.xmlsoap.org/wsdl/soap12/}</td>
	 * </tr>
	 * </table>
	 * </blockquote>
	 * <p>
	 * Defaults to {@link #DEFAULT_WSDL_LOCATION_EXPRESSION}.
	 */
	public void setLocationExpression(String expression) {
		Assert.hasText(expression, "'expression' must not be empty");
		this.locationXPathExpression = XPathExpressionFactory.createXPathExpression(expression,
				this.expressionNamespaces);
	}

	@Override
	protected @Nullable URI lookupDestination() {
		Assert.notNull(this.wsdlResource, "'wsdlResource' must not be null");
		try {
			DOMResult result = new DOMResult();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(new ResourceSource(this.wsdlResource), result);
			Document definitionDocument = (Document) result.getNode();
			String location = this.locationXPathExpression.evaluateAsString(definitionDocument);
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Found location [" + location + "] in " + this.wsdlResource);
			}
			return (location != null) ? URI.create(location) : null;
		}
		catch (IOException ex) {
			throw new WebServiceIOException("Error extracting location from WSDL [" + this.wsdlResource + "]", ex);
		}
		catch (TransformerException ex) {
			throw new WebServiceTransformerException("Error extracting location from WSDL [" + this.wsdlResource + "]",
					ex);
		}
	}

}
