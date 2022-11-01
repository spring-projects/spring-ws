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

package org.springframework.ws.pox.dom;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Implementation of the {@link WebServiceMessageFactory} interface that creates a {@link DomPoxMessage}.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.pox.dom.DomPoxMessage
 * @since 1.0.0
 */
public class DomPoxMessageFactory extends TransformerObjectSupport implements WebServiceMessageFactory {

	/** The default content type for the POX messages. */
	public static final String DEFAULT_CONTENT_TYPE = "application/xml";

	private DocumentBuilderFactory documentBuilderFactory;

	private String contentType = DEFAULT_CONTENT_TYPE;

	/**
	 * Use default {@link DocumentBuilderFactory}.
	 */
	public DomPoxMessageFactory() {
		this(DocumentBuilderFactoryUtils.newInstance());
	}

	/**
	 * Provide your own {@link DocumentBuilderFactory}.
	 *
	 * @param documentBuilderFactory
	 */
	public DomPoxMessageFactory(DocumentBuilderFactory documentBuilderFactory) {
		this.documentBuilderFactory = documentBuilderFactory;

		documentBuilderFactory.setNamespaceAware(true);
		documentBuilderFactory.setValidating(false);
		documentBuilderFactory.setExpandEntityReferences(false);
	}

	/** Sets the content-type for the {@link DomPoxMessage}. */
	public void setContentType(String contentType) {
		Assert.hasLength(contentType, "'contentType' must not be empty");
		this.contentType = contentType;
	}

	/** Set whether or not the XML parser should be XML namespace aware. Default is {@code true}. */
	public void setNamespaceAware(boolean namespaceAware) {
		documentBuilderFactory.setNamespaceAware(namespaceAware);
	}

	/** Set if the XML parser should validate the document. Default is {@code false}. */
	public void setValidating(boolean validating) {
		documentBuilderFactory.setValidating(validating);
	}

	/**
	 * Set if the XML parser should expand entity reference nodes. Default is {@code false}.
	 */
	public void setExpandEntityReferences(boolean expandEntityRef) {
		documentBuilderFactory.setExpandEntityReferences(expandEntityRef);
	}

	@Override
	public DomPoxMessage createWebServiceMessage() {
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document request = documentBuilder.newDocument();
			return new DomPoxMessage(request, createTransformer(), contentType);
		} catch (ParserConfigurationException ex) {
			throw new DomPoxMessageException("Could not create message context", ex);
		} catch (TransformerConfigurationException ex) {
			throw new DomPoxMessageException("Could not create transformer", ex);
		}
	}

	@Override
	public DomPoxMessage createWebServiceMessage(InputStream inputStream) throws IOException {
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document request = documentBuilder.parse(inputStream);
			return new DomPoxMessage(request, createTransformer(), contentType);
		} catch (ParserConfigurationException ex) {
			throw new DomPoxMessageException("Could not create message context", ex);
		} catch (SAXException ex) {
			throw new DomPoxMessageException("Could not parse request message", ex);
		} catch (TransformerConfigurationException ex) {
			throw new DomPoxMessageException("Could not create transformer", ex);
		}
	}
}
