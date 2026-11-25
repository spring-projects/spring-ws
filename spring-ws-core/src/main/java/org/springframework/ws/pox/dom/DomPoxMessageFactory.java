/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.pox.dom;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.springframework.util.Assert;
import org.springframework.util.unit.DataSize;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.support.BoundedInputStream;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Implementation of the {@link WebServiceMessageFactory} interface that creates a
 * {@link DomPoxMessage}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.ws.pox.dom.DomPoxMessage
 */
public class DomPoxMessageFactory extends TransformerObjectSupport implements WebServiceMessageFactory {

	/** The default content type for the POX messages. */
	public static final String DEFAULT_CONTENT_TYPE = "application/xml";

	private final DocumentBuilderFactory documentBuilderFactory;

	private String contentType = DEFAULT_CONTENT_TYPE;

	private DataSize maxMessageSize = DEFAULT_MAX_MESSAGE_SIZE;

	/**
	 * Use default {@link DocumentBuilderFactory}.
	 */
	public DomPoxMessageFactory() {
		this(DocumentBuilderFactoryUtils.newInstance());
	}

	/**
	 * Provide your own {@link DocumentBuilderFactory}.
	 * @param documentBuilderFactory the document builder factory to use
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

	/**
	 * Set whether or not the XML parser should be XML namespace aware. Default is
	 * {@code true}.
	 */
	public void setNamespaceAware(boolean namespaceAware) {
		this.documentBuilderFactory.setNamespaceAware(namespaceAware);
	}

	/** Set if the XML parser should validate the document. Default is {@code false}. */
	public void setValidating(boolean validating) {
		this.documentBuilderFactory.setValidating(validating);
	}

	/**
	 * Set if the XML parser should expand entity reference nodes. Default is
	 * {@code false}.
	 */
	public void setExpandEntityReferences(boolean expandEntityRef) {
		this.documentBuilderFactory.setExpandEntityReferences(expandEntityRef);
	}

	/**
	 * Set the maximum message size allowed for incoming message. Defaults to
	 * {@link #DEFAULT_MAX_MESSAGE_SIZE}. Set to {@code -1} for unlimited message size.
	 * @since 3.1.10
	 * @see #createWebServiceMessage(InputStream)
	 */
	public void setMaxMessageSize(DataSize maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

	/**
	 * Set the maximum message size allowed for incoming message, using a textual
	 * representation such as {@code 20MB}, as supported by
	 * {@link DataSize#parse(CharSequence)}. A plain number is interpreted as a number of
	 * bytes. Set to {@code -1} for unlimited message size.
	 * @param maxMessageSize the maximum message size, as a {@link String}
	 * @since 3.1.11
	 * @see #setMaxMessageSize(DataSize)
	 */
	public void setMaxMessageSize(String maxMessageSize) {
		setMaxMessageSize(DataSize.parse(maxMessageSize));
	}

	@Override
	public DomPoxMessage createWebServiceMessage() {
		try {
			DocumentBuilder documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
			Document request = documentBuilder.newDocument();
			return new DomPoxMessage(request, createTransformer(), this.contentType);
		}
		catch (ParserConfigurationException ex) {
			throw new DomPoxMessageException("Could not create message context", ex);
		}
		catch (TransformerConfigurationException ex) {
			throw new DomPoxMessageException("Could not create transformer", ex);
		}
	}

	@Override
	public DomPoxMessage createWebServiceMessage(InputStream inputStream) throws IOException {
		try {
			DocumentBuilder documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
			if (!this.maxMessageSize.isNegative()) {
				inputStream = new BoundedInputStream(inputStream, this.maxMessageSize);
			}
			Document request = documentBuilder.parse(inputStream);
			return new DomPoxMessage(request, createTransformer(), this.contentType);
		}
		catch (ParserConfigurationException ex) {
			throw new DomPoxMessageException("Could not create message context", ex);
		}
		catch (SAXException ex) {
			throw new DomPoxMessageException("Could not parse request message", ex);
		}
		catch (TransformerConfigurationException ex) {
			throw new DomPoxMessageException("Could not create transformer", ex);
		}
	}

}
