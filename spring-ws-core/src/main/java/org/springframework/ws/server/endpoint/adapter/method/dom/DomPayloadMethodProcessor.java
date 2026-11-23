/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.adapter.method.dom;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.core.MethodParameter;
import org.springframework.ws.server.endpoint.adapter.method.AbstractPayloadSourceMethodProcessor;

/**
 * Implementation of
 * {@link org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver
 * MethodArgumentResolver} and
 * {@link org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler
 * MethodReturnValueHandler} that supports W3C DOM {@linkplain Element elements}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class DomPayloadMethodProcessor extends AbstractPayloadSourceMethodProcessor {

	// MethodArgumentResolver

	@Override
	protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
		return supports(parameter);
	}

	@Override
	protected Node resolveRequestPayloadArgument(MethodParameter parameter, Source requestPayload) throws Exception {
		if (requestPayload instanceof DOMSource) {
			return resolveArgumentDomSource(parameter, (DOMSource) requestPayload);
		}
		else {
			DOMResult domResult = new DOMResult();
			transform(requestPayload, domResult);
			DOMSource domSource = new DOMSource(domResult.getNode());
			return resolveArgumentDomSource(parameter, domSource);
		}
	}

	private Node resolveArgumentDomSource(MethodParameter parameter, DOMSource requestSource) {
		Class<?> parameterType = parameter.getParameterType();
		Node requestNode = requestSource.getNode();
		if (parameterType.isAssignableFrom(requestNode.getClass())) {
			return requestNode;
		}
		else if (Element.class.equals(parameterType) && requestNode instanceof Document document) {
			return document.getDocumentElement();
		}
		// should not happen
		throw new UnsupportedOperationException();
	}

	// MethodReturnValueHandler

	@Override
	protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
		return supports(returnType);
	}

	@Override
	protected DOMSource createResponsePayload(MethodParameter returnType, Object returnValue) {
		Element returnedElement = (Element) returnValue;
		return new DOMSource(returnedElement);
	}

	private boolean supports(MethodParameter parameter) {
		return Element.class.equals(parameter.getParameterType());
	}

}
