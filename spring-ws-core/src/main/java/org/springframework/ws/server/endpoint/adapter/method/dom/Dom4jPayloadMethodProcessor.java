/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.adapter.method.dom;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;

import org.springframework.core.MethodParameter;
import org.springframework.ws.server.endpoint.adapter.method.AbstractPayloadSourceMethodProcessor;

/**
 * Implementation of
 * {@link org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver
 * MethodArgumentResolver} and
 * {@link org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler
 * MethodReturnValueHandler} that supports dom4j {@linkplain Element elements}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class Dom4jPayloadMethodProcessor extends AbstractPayloadSourceMethodProcessor {

	@Override
	protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
		return supports(parameter);
	}

	@Override
	protected Element resolveRequestPayloadArgument(MethodParameter parameter, Source requestPayload)
			throws TransformerException {
		if (requestPayload instanceof DOMSource) {
			org.w3c.dom.Node node = ((DOMSource) requestPayload).getNode();
			if (node.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE) {
				DOMReader domReader = new DOMReader();
				Document document = domReader.read((org.w3c.dom.Document) node);
				return document.getRootElement();
			}
		}
		// we have no other option than to transform
		DocumentResult dom4jResult = new DocumentResult();
		transform(requestPayload, dom4jResult);
		return dom4jResult.getDocument().getRootElement();
	}

	@Override
	protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
		return supports(returnType);
	}

	@Override
	protected Source createResponsePayload(MethodParameter returnType, Object returnValue) {
		Element returnedElement = (Element) returnValue;
		return new DocumentSource(returnedElement);
	}

	private boolean supports(MethodParameter parameter) {
		return Element.class.equals(parameter.getParameterType());
	}

}
