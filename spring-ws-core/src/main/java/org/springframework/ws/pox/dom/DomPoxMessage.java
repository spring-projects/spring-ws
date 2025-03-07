/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ws.pox.dom;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.springframework.util.Assert;
import org.springframework.ws.pox.PoxMessage;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Implementation of the {@code PoxMessage} interface that is based on a DOM Document.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see Document
 */
public class DomPoxMessage implements PoxMessage {

	private final String contentType;

	private final Document document;

	private final Transformer transformer;

	/**
	 * Constructs a new instance of the {@code DomPoxMessage} with the given document.
	 * @param document the document to base the message on
	 */
	public DomPoxMessage(Document document, Transformer transformer, String contentType) {
		Assert.notNull(document, "'document' must not be null");
		Assert.notNull(transformer, "'transformer' must not be null");
		Assert.hasLength(contentType, "'contentType' must not be empty");
		this.document = document;
		this.transformer = transformer;
		this.contentType = contentType;
	}

	/** Returns the document underlying this message. */
	public Document getDocument() {
		return this.document;
	}

	@Override
	public Result getPayloadResult() {
		NodeList children = this.document.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			this.document.removeChild(children.item(i));
		}
		return new DOMResult(this.document);
	}

	@Override
	public Source getPayloadSource() {
		return new DOMSource(this.document);
	}

	public boolean hasFault() {
		return false;
	}

	public String getFaultReason() {
		return null;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("DomPoxMessage ");
		Element root = this.document.getDocumentElement();
		if (root != null) {
			builder.append(' ');
			builder.append(QNameUtils.getQNameForNode(root));
		}
		return builder.toString();
	}

	@Override
	public void writeTo(OutputStream outputStream) throws IOException {
		try {
			if (outputStream instanceof TransportOutputStream transportOutputStream) {
				transportOutputStream.addHeader(TransportConstants.HEADER_CONTENT_TYPE, this.contentType);
			}
			this.transformer.transform(getPayloadSource(), new StreamResult(outputStream));
		}
		catch (TransformerException ex) {
			throw new DomPoxMessageException("Could write document: " + ex.getMessage(), ex);
		}
	}

}
