/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.mapping;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;

/**
 * Implementation of the {@code EndpointMapping} interface to map from the qualified name of the request payload
 * root element. Supports both mapping to bean instances and mapping to bean names: the latter is required for prototype
 * endpoints.
 *
 * <p>The {@code endpointMap} property is suitable for populating the endpoint map with bean references, e.g. via the
 * map element in XML bean definitions.
 *
 * <p>Mappings to bean names can be set via the {@code mappings} property, in a form accepted by the
 * {@code java.util.Properties} class, like as follows:
 * <pre>
 * {http://www.springframework.org/spring-ws/samples/airline/schemas}BookFlight=bookFlightEndpoint
 * {http://www.springframework.org/spring-ws/samples/airline/schemas}GetFlights=getFlightsEndpoint
 * </pre>
 * The syntax is QNAME=ENDPOINT_BEAN_NAME. Qualified names are parsed using the syntax described in
 * {@code QNameEditor}.
 *
 * @author Arjen Poutsma
 * @see org.springframework.xml.namespace.QNameEditor
 * @since 1.0.0
 */
public class PayloadRootQNameEndpointMapping extends AbstractQNameEndpointMapping {

	private static TransformerFactory transformerFactory;

	static {
		transformerFactory = TransformerFactory.newInstance();
	}

	@Override
	protected QName resolveQName(MessageContext messageContext) throws TransformerException {
		return PayloadRootUtils.getPayloadRootQName(messageContext.getRequest().getPayloadSource(), transformerFactory);
	}

}
