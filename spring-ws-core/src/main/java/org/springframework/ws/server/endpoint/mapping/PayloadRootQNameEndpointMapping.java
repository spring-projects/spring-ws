/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.mapping;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;
import org.springframework.xml.transform.TransformerFactoryUtils;

/**
 * Implementation of the {@code EndpointMapping} interface to map from the qualified name
 * of the request payload root element. Supports both mapping to bean instances and
 * mapping to bean names: the latter is required for prototype endpoints.
 * <p>
 * The {@code endpointMap} property is suitable for populating the endpoint map with bean
 * references, e.g. via the map element in XML bean definitions.
 * <p>
 * Mappings to bean names can be set via the {@code mappings} property, in a form accepted
 * by the {@code java.util.Properties} class, like as follows:
 *
 * <pre>
 * {http://www.springframework.org/spring-ws/samples/airline/schemas}BookFlight=bookFlightEndpoint
 * {http://www.springframework.org/spring-ws/samples/airline/schemas}GetFlights=getFlightsEndpoint
 * </pre>
 *
 * The syntax is QNAME=ENDPOINT_BEAN_NAME. Qualified names are parsed using the syntax
 * described in {@code QNameEditor}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.xml.namespace.QNameEditor
 */
public class PayloadRootQNameEndpointMapping extends AbstractQNameEndpointMapping {

	private static TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();

	/**
	 * Override the default {@link TransformerFactory}.
	 * @param transformerFactory the transformer factory to use
	 */
	public static void setTransformerFactory(TransformerFactory transformerFactory) {
		PayloadRootQNameEndpointMapping.transformerFactory = transformerFactory;
	}

	@Override
	protected @Nullable QName resolveQName(MessageContext messageContext) throws TransformerException {
		return PayloadRootUtils.getPayloadRootQName(messageContext.getRequest().getPayloadSource(), transformerFactory);
	}

}
