/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.server.endpoint;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.w3c.dom.Node;

/**
 * Abstract base class for endpoints that handle the message payload as dom4j elements. Offers the message payload as a
 * dom4j {@code Element}, and allows subclasses to create a response by returning an {@code Element}.
 * <p>
 * An {@code AbstractDom4JPayloadEndpoint} only accept one payload element. Multiple payload elements are not in
 * accordance with WS-I.
 *
 * @author Arjen Poutsma
 * @see org.dom4j.Element
 * @since 1.0.0
 * @deprecated as of Spring Web Services 2.0, in favor of annotated endpoints
 */
@Deprecated
public abstract class AbstractDom4jPayloadEndpoint extends TransformerObjectSupport implements PayloadEndpoint {

	private boolean alwaysTransform = false;

	/**
	 * Set if the request {@link Source} should always be transformed into a new {@link DocumentResult}.
	 * <p>
	 * Default is {@code false}, which is faster.
	 */
	public void setAlwaysTransform(boolean alwaysTransform) {
		this.alwaysTransform = alwaysTransform;
	}

	@Override
	public final Source invoke(Source request) throws Exception {
		Element requestElement = null;
		if (request != null) {
			DocumentResult dom4jResult = new DocumentResult();
			transform(request, dom4jResult);
			requestElement = dom4jResult.getDocument().getRootElement();
		}
		Document responseDocument = DocumentHelper.createDocument();
		Element responseElement = invokeInternal(requestElement, responseDocument);
		return responseElement != null ? new DocumentSource(responseElement) : null;
	}

	/**
	 * Returns the payload element of the given source.
	 * <p>
	 * Default implementation checks whether the source is a {@link javax.xml.transform.dom.DOMSource}, and uses a
	 * {@link org.dom4j.io.DOMReader} to create a JDOM {@link org.dom4j.Element}. In all other cases, or when
	 * {@linkplain #setAlwaysTransform(boolean) alwaysTransform} is {@code true}, the source is transformed into a
	 * {@link org.dom4j.io.DocumentResult}, which is more expensive. If the passed source is {@code null}, {@code
	 * null} is returned.
	 *
	 * @param source the source to return the root element of; can be {@code null}
	 * @return the document element
	 * @throws javax.xml.transform.TransformerException in case of errors
	 */
	protected Element getDocumentElement(Source source) throws TransformerException {
		if (source == null) {
			return null;
		}
		if (!alwaysTransform && source instanceof DOMSource) {
			Node node = ((DOMSource) source).getNode();
			if (node.getNodeType() == Node.DOCUMENT_NODE) {
				DOMReader domReader = new DOMReader();
				Document document = domReader.read((org.w3c.dom.Document) node);
				return document.getRootElement();
			}
		}
		// we have no other option than to transform
		DocumentResult dom4jResult = new DocumentResult();
		transform(source, dom4jResult);
		return dom4jResult.getDocument().getRootElement();
	}

	/**
	 * Template method. Subclasses must implement this. Offers the request payload as a dom4j {@code Element}, and allows
	 * subclasses to return a response {@code Element}.
	 * <p>
	 * The given dom4j {@code Document} is to be used for constructing a response element, by using {@code addElement}.
	 *
	 * @param requestElement the contents of the SOAP message as dom4j elements
	 * @param responseDocument a dom4j document to be used for constructing a response
	 * @return the response element. Can be {@code null} to specify no response.
	 */
	protected abstract Element invokeInternal(Element requestElement, Document responseDocument) throws Exception;
}
