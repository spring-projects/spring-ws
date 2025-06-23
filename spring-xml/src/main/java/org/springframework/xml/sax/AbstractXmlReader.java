/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.xml.sax;

import org.jspecify.annotations.Nullable;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * Abstract base class for SAX {@code XMLReader} implementations. Contains properties as
 * defined in {@link XMLReader}, and does not recognize any features
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #setContentHandler(org.xml.sax.ContentHandler)
 * @see #setDTDHandler(org.xml.sax.DTDHandler)
 * @see #setEntityResolver(org.xml.sax.EntityResolver)
 * @see #setErrorHandler(org.xml.sax.ErrorHandler)
 */
public abstract class AbstractXmlReader implements XMLReader {

	private @Nullable DTDHandler dtdHandler;

	private @Nullable ContentHandler contentHandler;

	private @Nullable EntityResolver entityResolver;

	private @Nullable ErrorHandler errorHandler;

	private @Nullable LexicalHandler lexicalHandler;

	@Override
	public @Nullable ContentHandler getContentHandler() {
		return this.contentHandler;
	}

	@Override
	public void setContentHandler(ContentHandler contentHandler) {
		this.contentHandler = contentHandler;
	}

	@Override
	public void setDTDHandler(DTDHandler dtdHandler) {
		this.dtdHandler = dtdHandler;
	}

	@Override
	public @Nullable DTDHandler getDTDHandler() {
		return this.dtdHandler;
	}

	@Override
	public @Nullable EntityResolver getEntityResolver() {
		return this.entityResolver;
	}

	@Override
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	@Override
	public @Nullable ErrorHandler getErrorHandler() {
		return this.errorHandler;
	}

	@Override
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	protected @Nullable LexicalHandler getLexicalHandler() {
		return this.lexicalHandler;
	}

	/**
	 * Throws a {@code SAXNotRecognizedException} exception.
	 * @throws org.xml.sax.SAXNotRecognizedException always
	 */
	@Override
	public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotRecognizedException(name);
	}

	/**
	 * Throws a {@code SAXNotRecognizedException} exception.
	 * @throws SAXNotRecognizedException always
	 */
	@Override
	public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotRecognizedException(name);
	}

	/**
	 * Throws a {@code SAXNotRecognizedException} exception when the given property does
	 * not signify a lexical handler. The property name for a lexical handler is
	 * {@code http://xml.org/sax/properties/lexical-handler}.
	 */
	@Override
	public @Nullable Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
			return this.lexicalHandler;
		}
		else {
			throw new SAXNotRecognizedException(name);
		}
	}

	/**
	 * Throws a {@code SAXNotRecognizedException} exception when the given property does
	 * not signify a lexical handler. The property name for a lexical handler is
	 * {@code http://xml.org/sax/properties/lexical-handler}.
	 */
	@Override
	public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
		if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
			this.lexicalHandler = (LexicalHandler) value;
		}
		else {
			throw new SAXNotRecognizedException(name);
		}
	}

}
