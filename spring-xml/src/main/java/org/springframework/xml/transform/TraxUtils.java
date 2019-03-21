/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xml.transform;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.util.StringUtils;
import org.springframework.util.xml.StaxUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * Convenient utility methods for dealing with TrAX.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class TraxUtils {

	/**
	 * Returns the {@link Document} of the given {@link DOMSource}.
	 *
	 * @param source the DOM source
	 * @return the document
	 */
	public static Document getDocument(DOMSource source) {
		Node node = source.getNode();
		if (node instanceof Document) {
			return (Document) node;
		}
		else if (node != null) {
			return node.getOwnerDocument();
		}
		else {
			return null;
		}
	}

	/**
	 * Performs the given {@linkplain SourceCallback callback} operation on a {@link Source}. Supports both the JAXP 1.4
	 * {@link StAXSource} and the Spring 3.0 {@link StaxUtils#createStaxSource StaxSource}.
	 *
	 * @param source   source to look at
	 * @param callback the callback to invoke for each kind of source
	 */
	public static void doWithSource(Source source, SourceCallback callback) throws Exception {
		if (source instanceof DOMSource) {
			callback.domSource(((DOMSource) source).getNode());
			return;
		}
		else if (StaxUtils.isStaxSource(source)) {
			XMLStreamReader streamReader = StaxUtils.getXMLStreamReader(source);
			if (streamReader != null) {
				callback.staxSource(streamReader);
				return;
			}
			else {
				XMLEventReader eventReader = StaxUtils.getXMLEventReader(source);
				if (eventReader != null) {
					callback.staxSource(eventReader);
					return;
				}
			}
		}
		else if (source instanceof SAXSource) {
			SAXSource saxSource = (SAXSource) source;
			callback.saxSource(saxSource.getXMLReader(), saxSource.getInputSource());
			return;
		}
		else if (source instanceof StreamSource) {
			StreamSource streamSource = (StreamSource) source;
			if (streamSource.getInputStream() != null) {
				callback.streamSource(streamSource.getInputStream());
				return;
			}
			else if (streamSource.getReader() != null) {
				callback.streamSource(streamSource.getReader());
				return;
			}
		}
		if (StringUtils.hasLength(source.getSystemId())) {
			String systemId = source.getSystemId();
			callback.source(systemId);
		}
		else {
			throw new IllegalArgumentException("Unknown Source type: " + source.getClass());
		}
	}

	/**
	 * Performs the given {@linkplain org.springframework.xml.transform.TraxUtils.ResultCallback callback} operation on a {@link javax.xml.transform.Result}. Supports both the JAXP 1.4
	 * {@link javax.xml.transform.stax.StAXResult} and the Spring 3.0 {@link org.springframework.util.xml.StaxUtils#createStaxResult StaxSource}.
	 *
	 * @param result   result to look at
	 * @param callback the callback to invoke for each kind of result
	 */
	public static void doWithResult(Result result, ResultCallback callback) throws Exception {
		if (result instanceof DOMResult) {
			callback.domResult(((DOMResult) result).getNode());
			return;
		}
		else if (StaxUtils.isStaxResult(result)) {
			XMLStreamWriter streamWriter = StaxUtils.getXMLStreamWriter(result);
			if (streamWriter != null) {
				callback.staxResult(streamWriter);
				return;
			}
			else {
				XMLEventWriter eventWriter = StaxUtils.getXMLEventWriter(result);
				if (eventWriter != null) {
					callback.staxResult(eventWriter);
					return;
				}
			}
		}
		else if (result instanceof SAXResult) {
			SAXResult saxSource = (SAXResult) result;
			callback.saxResult(saxSource.getHandler(), saxSource.getLexicalHandler());
			return;
		}
		else if (result instanceof StreamResult) {
			StreamResult streamSource = (StreamResult) result;
			if (streamSource.getOutputStream() != null) {
				callback.streamResult(streamSource.getOutputStream());
				return;
			}
			else if (streamSource.getWriter() != null) {
				callback.streamResult(streamSource.getWriter());
				return;
			}
		}
		if (StringUtils.hasLength(result.getSystemId())) {
			String systemId = result.getSystemId();
			callback.result(systemId);
		}
		else {
			throw new IllegalArgumentException("Unknown Result type: " + result.getClass());
		}
	}

	/**
	 * Callback interface invoked on each sort of {@link Source}.
	 *
	 * @see TraxUtils#doWithSource(Source, SourceCallback)
	 */
	public interface SourceCallback {

		/**
		 * Perform an operation on the node contained in a {@link DOMSource}.
		 *
		 * @param node the node
		 */
		void domSource(Node node) throws Exception;

		/**
		 * Perform an operation on the {@code XMLReader} and {@code InputSource} contained in a {@link SAXSource}.
		 *
		 * @param reader	  the reader, can be {@code null}
		 * @param inputSource the input source, can be {@code null}
		 */
		void saxSource(XMLReader reader, InputSource inputSource) throws Exception;

		/**
		 * Perform an operation on the {@code XMLEventReader} contained in a JAXP 1.4 {@link StAXSource} or Spring
		 * {@link StaxUtils#createStaxSource StaxSource}.
		 *
		 * @param eventReader the reader
		 */
		void staxSource(XMLEventReader eventReader) throws Exception;

		/**
		 * Perform an operation on the {@code XMLStreamReader} contained in a JAXP 1.4 {@link StAXSource} or Spring
		 * {@link StaxUtils#createStaxSource StaxSource}.
		 *
		 * @param streamReader the reader
		 */
		void staxSource(XMLStreamReader streamReader) throws Exception;

		/**
		 * Perform an operation on the {@code InputStream} contained in a {@link StreamSource}.
		 *
		 * @param inputStream the input stream
		 */
		void streamSource(InputStream inputStream) throws Exception;

		/**
		 * Perform an operation on the {@code Reader} contained in a {@link StreamSource}.
		 *
		 * @param reader the reader
		 */
		void streamSource(Reader reader) throws Exception;

		/**
		 * Perform an operation on the system identifier contained in any {@link Source}.
		 *
		 * @param systemId the system identifier
		 */
		void source(String systemId) throws Exception;


	}

	/**
	 * Callback interface invoked on each sort of {@link Result}.
	 *
	 * @see TraxUtils#doWithResult(Result, ResultCallback)
	 */
	public interface ResultCallback {

		/**
		 * Perform an operation on the node contained in a {@link DOMResult}.
		 *
		 * @param node the node
		 */
		void domResult(Node node) throws Exception;

		/**
		 * Perform an operation on the {@code ContentHandler} and {@code LexicalHandler} contained in a {@link
		 * SAXResult}.
		 *
		 * @param contentHandler the content handler
		 * @param lexicalHandler the lexicalHandler, can be {@code null}
		 */
		void saxResult(ContentHandler contentHandler, LexicalHandler lexicalHandler) throws Exception;

		/**
		 * Perform an operation on the {@code XMLEventWriter} contained in a JAXP 1.4 {@link StAXResult} or Spring
		 * {@link StaxUtils#createStaxResult StaxResult}.
		 *
		 * @param eventWriter the writer
		 */
		void staxResult(XMLEventWriter eventWriter) throws Exception;

		/**
		 * Perform an operation on the {@code XMLStreamWriter} contained in a JAXP 1.4 {@link StAXResult} or Spring
		 * {@link StaxUtils#createStaxResult StaxResult}.
		 *
		 * @param streamWriter the writer
		 */
		void staxResult(XMLStreamWriter streamWriter) throws Exception;

		/**
		 * Perform an operation on the {@code OutputStream} contained in a {@link StreamResult}.
		 *
		 * @param outputStream the output stream
		 */
		void streamResult(OutputStream outputStream) throws Exception;

		/**
		 * Perform an operation on the {@code Writer} contained in a {@link StreamResult}.
		 *
		 * @param writer the writer
		 */
		void streamResult(Writer writer) throws Exception;

		/**
		 * Perform an operation on the system identifier contained in any {@link Result}.
		 *
		 * @param systemId the system identifier
		 */
		void result(String systemId) throws Exception;

	}


}
