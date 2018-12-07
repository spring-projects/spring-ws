/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.server.endpoint.adapter.method.dom;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.converters.DOMConverter;
import org.w3c.dom.DOMImplementation;

import org.springframework.core.MethodParameter;
import org.springframework.ws.server.endpoint.adapter.method.AbstractPayloadSourceMethodProcessor;
import org.springframework.xml.DocumentBuilderFactoryUtils;

/**
 * Implementation of {@link org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver
 * MethodArgumentResolver} and {@link org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler
 * MethodReturnValueHandler} that supports XOM {@linkplain Element elements}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class XomPayloadMethodProcessor extends AbstractPayloadSourceMethodProcessor {

	private DocumentBuilderFactory documentBuilderFactory = createDocumentBuilderFactory();

	@Override
	protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
		return supports(parameter);
	}

	@Override
	protected Element resolveRequestPayloadArgument(MethodParameter parameter, Source requestPayload)
			throws TransformerException, IOException, ParsingException {
		if (requestPayload instanceof DOMSource) {
			org.w3c.dom.Node node = ((DOMSource) requestPayload).getNode();
			if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
				return DOMConverter.convert((org.w3c.dom.Element) node);
			}
			else if (node.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE) {
				Document document = DOMConverter.convert((org.w3c.dom.Document) node);
				return document.getRootElement();
			}
		}
		// we have no other option than to transform
		ByteArrayInputStream bis = convertToByteArrayInputStream(requestPayload);
		Builder builder = new Builder();
		Document document = builder.build(bis);
		return document.getRootElement();
	}

	@Override
	protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
		return supports(returnType);
	}

	@Override
	protected Source createResponsePayload(MethodParameter returnType, Object returnValue)
			throws ParserConfigurationException {
		Element returnedElement = (Element) returnValue;
		Document document = returnedElement.getDocument();
		if (document == null) {
			document = new Document(returnedElement);
		}
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		DOMImplementation domImplementation = documentBuilder.getDOMImplementation();
		org.w3c.dom.Document w3cDocument = DOMConverter.convert(document, domImplementation);
		return new DOMSource(w3cDocument);
	}

	private boolean supports(MethodParameter parameter) {
		return Element.class.equals(parameter.getParameterType());
	}

	/**
	 * Create a {@code DocumentBuilderFactory} that this resolver will use to create response payloads.
	 *
	 * <p>Can be overridden in subclasses, adding further initialization of the factory. The resulting factory is cached,
	 * so this method will only be called once.
	 *
	 * @return the created factory
	 */
	protected DocumentBuilderFactory createDocumentBuilderFactory() {
		return DocumentBuilderFactoryUtils.newInstance();
	}

}
