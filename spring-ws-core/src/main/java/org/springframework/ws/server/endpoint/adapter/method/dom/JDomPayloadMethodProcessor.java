/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.adapter.method.dom;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.w3c.dom.Node;

import org.springframework.core.MethodParameter;
import org.springframework.ws.server.endpoint.adapter.method.AbstractPayloadSourceMethodProcessor;

/**
 * Implementation of
 * {@link org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver
 * MethodArgumentResolver} and
 * {@link org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler
 * MethodReturnValueHandler} that supports JDOM {@linkplain Element elements}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class JDomPayloadMethodProcessor extends AbstractPayloadSourceMethodProcessor {

	@Override
	protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
		return supports(parameter);
	}

	@Override
	protected Element resolveRequestPayloadArgument(MethodParameter parameter, Source requestPayload) throws Exception {
		if (requestPayload instanceof DOMSource) {
			Node node = ((DOMSource) requestPayload).getNode();
			DOMBuilder domBuilder = new DOMBuilder();
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				return domBuilder.build((org.w3c.dom.Element) node);
			}
			else if (node.getNodeType() == Node.DOCUMENT_NODE) {
				Document document = domBuilder.build((org.w3c.dom.Document) node);
				return document.getRootElement();
			}
		}
		// we have no other option than to transform
		JDOMResult jdomResult = new JDOMResult();
		transform(requestPayload, jdomResult);
		return jdomResult.getDocument().getRootElement();
	}

	@Override
	protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
		return supports(returnType);
	}

	@Override
	protected Source createResponsePayload(MethodParameter returnType, Object returnValue) {
		Element returnedElement = (Element) returnValue;
		return new JDOMSource(returnedElement);
	}

	private boolean supports(MethodParameter parameter) {
		return Element.class.equals(parameter.getParameterType());
	}

}
