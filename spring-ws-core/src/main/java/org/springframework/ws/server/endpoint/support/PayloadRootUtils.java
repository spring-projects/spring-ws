/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.server.endpoint.support;

import java.io.InputStream;
import java.io.Reader;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.TransformerHelper;
import org.springframework.xml.transform.TraxUtils;

/**
 * Helper class for determining the root qualified name of a Web Service payload.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class PayloadRootUtils {

	private PayloadRootUtils() {
	}

	/**
	 * Returns the root qualified name of the given source, transforming it if necessary.
	 *
	 * @param source			 the source to get the root element from
	 * @param transformerFactory a transformer factory, necessary if the given source is not a {@code DOMSource}
	 * @return the root element, or {@code null} if {@code source} is {@code null}
	 */
	public static QName getPayloadRootQName(Source source, TransformerFactory transformerFactory)
			throws TransformerException {
		return getPayloadRootQName(source, new TransformerHelper(transformerFactory));
	}

	public static QName getPayloadRootQName(Source source, TransformerHelper transformerHelper)
			throws TransformerException {
		if (source == null) {
			return null;
		}
		try {
			PayloadRootSourceCallback callback = new PayloadRootSourceCallback();
			TraxUtils.doWithSource(source, callback);
			if (callback.result != null) {
				return callback.result;
			}
			else {
				// we have no other option than to transform
				DOMResult domResult = new DOMResult();
				transformerHelper.transform(source, domResult);
				Document document = (Document) domResult.getNode();
				return QNameUtils.getQNameForNode(document.getDocumentElement());
			}
		}
		catch (TransformerException ex) {
			throw ex;
		}
		catch (Exception ex) {
			return null;
		}
	}

	private static class PayloadRootSourceCallback implements TraxUtils.SourceCallback {

		private QName result;

		@Override
		public void domSource(Node node) throws Exception {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				result = QNameUtils.getQNameForNode(node);
			}
			else if (node.getNodeType() == Node.DOCUMENT_NODE) {
				Document document = (Document) node;
				result = QNameUtils.getQNameForNode(document.getDocumentElement());
			}
		}

		@Override
		public void staxSource(XMLEventReader eventReader) throws Exception {
			XMLEvent event = eventReader.peek();
			if (event != null && event.isStartDocument()) {
				event = eventReader.nextTag();
			}
			if (event != null) {
				if (event.isStartElement()) {
					result = event.asStartElement().getName();
				}
				else if (event.isEndElement()) {
					result = event.asEndElement().getName();
				}
			}
		}

		@Override
		public void staxSource(XMLStreamReader streamReader) throws Exception {
			if (streamReader.getEventType() == XMLStreamConstants.START_DOCUMENT) {
				try {
					streamReader.nextTag();
				}
				catch (XMLStreamException ex) {
					throw new IllegalStateException("Could not read next tag: " + ex.getMessage(), ex);
				}
			}
			if (streamReader.getEventType() == XMLStreamConstants.START_ELEMENT ||
					streamReader.getEventType() == XMLStreamConstants.END_ELEMENT) {
				result = streamReader.getName();
			}
		}

		@Override
		public void saxSource(XMLReader reader, InputSource inputSource) throws Exception {
			// Do nothing
		}

		@Override
		public void streamSource(InputStream inputStream) throws Exception {
			// Do nothing
		}

		@Override
		public void streamSource(Reader reader) throws Exception {
			// Do nothing
		}

		@Override
		public void source(String systemId) throws Exception {
			// Do nothing
		}
	}


}
