/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.support.matcher.PayloadDiffMatcher;
import org.springframework.ws.test.support.matcher.SchemaValidatingMatcher;
import org.springframework.ws.test.support.matcher.SoapEnvelopeDiffMatcher;
import org.springframework.ws.test.support.matcher.SoapHeaderMatcher;
import org.springframework.xml.transform.ResourceSource;

/**
 * Factory methods for {@link RequestMatcher} classes. Typically used to provide input for
 * {@link MockWebServiceServer#expect(RequestMatcher)}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class RequestMatchers {

	private RequestMatchers() {}

	/**
	 * Expects any request.
	 *
	 * @return the request matcher
	 */
	public static RequestMatcher anything() {
		return new RequestMatcher() {
			public void match(URI uri, WebServiceMessage request) throws IOException, AssertionError {}
		};
	}

	// Payload

	/**
	 * Expects the given {@link javax.xml.transform.Source} XML payload.
	 *
	 * @param payload the XML payload
	 * @return the request matcher
	 */
	public static RequestMatcher payload(Source payload) {
		Assert.notNull(payload, "'payload' must not be null");
		return new WebServiceMessageMatcherAdapter(new PayloadDiffMatcher(payload));
	}

	/**
	 * Expects the given {@link org.springframework.core.io.Resource} XML payload.
	 *
	 * @param payload the XML payload
	 * @return the request matcher
	 */
	public static RequestMatcher payload(Resource payload) throws IOException {
		Assert.notNull(payload, "'payload' must not be null");
		return payload(new ResourceSource(payload));
	}

	/**
	 * Expects the payload to validate against the given XSD schema(s).
	 *
	 * @param schema the schema
	 * @param furtherSchemas further schemas, if necessary
	 * @return the request matcher
	 */
	public static RequestMatcher validPayload(Resource schema, Resource... furtherSchemas) throws IOException {
		return new WebServiceMessageMatcherAdapter(new SchemaValidatingMatcher(schema, furtherSchemas));
	}

	/**
	 * Expects the given XPath expression to (not) exist or be evaluated to a value.
	 *
	 * @param xpathExpression the XPath expression
	 * @return the XPath expectations, to be further configured
	 */
	public static RequestXPathExpectations xpath(String xpathExpression) {
		return new XPathExpectationsHelperAdapter(xpathExpression, null);
	}

	/**
	 * Expects the given XPath expression to (not) exist or be evaluated to a value.
	 *
	 * @param xpathExpression the XPath expression
	 * @param namespaceMapping the namespaces
	 * @return the XPath expectations, to be further configured
	 */
	public static RequestXPathExpectations xpath(String xpathExpression, Map<String, String> namespaceMapping) {
		return new XPathExpectationsHelperAdapter(xpathExpression, namespaceMapping);
	}

	// SOAP

	/**
	 * Expects the given {@link javax.xml.transform.Source} XML SOAP envelope.
	 *
	 * @param soapEnvelope the XML SOAP envelope
	 * @return the request matcher
	 * @since 2.1.1
	 */
	public static RequestMatcher soapEnvelope(Source soapEnvelope) {
		Assert.notNull(soapEnvelope, "'soapEnvelope' must not be null");
		return new WebServiceMessageMatcherAdapter(new SoapEnvelopeDiffMatcher(soapEnvelope));
	}

	/**
	 * Expects the given {@link org.springframework.core.io.Resource} XML SOAP envelope.
	 *
	 * @param soapEnvelope the XML SOAP envelope
	 * @return the request matcher
	 * @since 2.1.1
	 */
	public static RequestMatcher soapEnvelope(Resource soapEnvelope) throws IOException {
		Assert.notNull(soapEnvelope, "'soapEnvelope' must not be null");
		return soapEnvelope(new ResourceSource(soapEnvelope));
	}

	/**
	 * Expects the given SOAP header in the outgoing message.
	 *
	 * @param soapHeaderName the qualified name of the SOAP header to expect
	 * @return the request matcher
	 */
	public static RequestMatcher soapHeader(QName soapHeaderName) {
		Assert.notNull(soapHeaderName, "'soapHeaderName' must not be null");
		return new WebServiceMessageMatcherAdapter(new SoapHeaderMatcher(soapHeaderName));
	}

	// Other

	/**
	 * Expects a connection to the given URI.
	 *
	 * @param uri the String uri expected to connect to
	 * @return the request matcher
	 */
	public static RequestMatcher connectionTo(String uri) {
		Assert.notNull(uri, "'uri' must not be null");
		return connectionTo(URI.create(uri));
	}

	/**
	 * Expects a connection to the given URI.
	 *
	 * @param uri the String uri expected to connect to
	 * @return the request matcher
	 */
	public static RequestMatcher connectionTo(URI uri) {
		Assert.notNull(uri, "'uri' must not be null");
		return new UriMatcher(uri);
	}

}
