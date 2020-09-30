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

package org.springframework.ws.server.endpoint.adapter.method;

import java.io.ByteArrayInputStream;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.MethodParameter;
import org.springframework.xml.JaxpVersion;
import org.springframework.xml.XMLInputFactoryUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Implementation of {@link MethodArgumentResolver} and {@link MethodReturnValueHandler} that supports {@link Source}
 * objects.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class SourcePayloadMethodProcessor extends AbstractPayloadSourceMethodProcessor {

	private XMLInputFactory inputFactory = createXmlInputFactory();

	// MethodArgumentResolver

	@Override
	protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
		return supports(parameter);
	}

	@Override
	protected Source resolveRequestPayloadArgument(MethodParameter parameter, Source requestPayload) throws Exception {
		Class<?> parameterType = parameter.getParameterType();
		if (parameterType.isAssignableFrom(requestPayload.getClass())) {
			return requestPayload;
		}
		if (DOMSource.class.isAssignableFrom(parameterType)) {
			DOMResult domResult = new DOMResult();
			transform(requestPayload, domResult);
			Node node = domResult.getNode();
			if (node.getNodeType() == Node.DOCUMENT_NODE) {
				return new DOMSource(((Document) node).getDocumentElement());
			} else {
				return new DOMSource(domResult.getNode());
			}
		} else if (SAXSource.class.isAssignableFrom(parameterType)) {
			ByteArrayInputStream bis = convertToByteArrayInputStream(requestPayload);
			InputSource inputSource = new InputSource(bis);
			return new SAXSource(inputSource);
		} else if (StreamSource.class.isAssignableFrom(parameterType)) {
			ByteArrayInputStream bis = convertToByteArrayInputStream(requestPayload);
			return new StreamSource(bis);
		} else if (JaxpVersion.isAtLeastJaxp14() && Jaxp14StaxHandler.isStaxSource(parameterType)) {
			XMLStreamReader streamReader;
			try {
				streamReader = inputFactory.createXMLStreamReader(requestPayload);
			} catch (UnsupportedOperationException ignored) {
				streamReader = null;
			} catch (XMLStreamException ignored) {
				streamReader = null;
			}
			if (streamReader == null) {
				ByteArrayInputStream bis = convertToByteArrayInputStream(requestPayload);
				streamReader = inputFactory.createXMLStreamReader(bis);
			}
			return Jaxp14StaxHandler.createStaxSource(streamReader, requestPayload.getSystemId());
		}
		throw new IllegalArgumentException("Unknown Source type: " + parameterType);
	}

	// MethodReturnValueHandler

	@Override
	protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
		return supports(returnType);
	}

	@Override
	protected Source createResponsePayload(MethodParameter returnType, Object returnValue) {
		return (Source) returnValue;
	}

	private boolean supports(MethodParameter parameter) {
		return Source.class.isAssignableFrom(parameter.getParameterType());
	}

	/**
	 * Create a {@code XMLInputFactory} that this resolver will use to create {@link javax.xml.stream.XMLStreamReader} and
	 * {@link javax.xml.stream.XMLEventReader} objects.
	 * <p>
	 * Can be overridden in subclasses, adding further initialization of the factory. The resulting factory is cached, so
	 * this method will only be called once.
	 *
	 * @return the created factory
	 */
	protected XMLInputFactory createXmlInputFactory() {
		return XMLInputFactoryUtils.newInstance();
	}

	/** Inner class to avoid a static JAXP 1.4 dependency. */
	private static class Jaxp14StaxHandler {

		private static boolean isStaxSource(Class<?> clazz) {
			return StAXSource.class.isAssignableFrom(clazz);
		}

		private static Source createStaxSource(XMLStreamReader streamReader, String systemId) {
			return new StAXSource(new SystemIdStreamReaderDelegate(streamReader, systemId));
		}

	}

	private static class SystemIdStreamReaderDelegate extends StreamReaderDelegate {

		private final String systemId;

		private SystemIdStreamReaderDelegate(XMLStreamReader reader, String systemId) {
			super(reader);
			this.systemId = systemId;
		}

		@Override
		public Location getLocation() {
			final Location parentLocation = getParent().getLocation();
			return new Location() {
				public int getLineNumber() {
					return parentLocation != null ? parentLocation.getLineNumber() : -1;
				}

				public int getColumnNumber() {
					return parentLocation != null ? parentLocation.getColumnNumber() : -1;
				}

				public int getCharacterOffset() {
					return parentLocation != null ? parentLocation.getLineNumber() : -1;
				}

				public String getPublicId() {
					return parentLocation != null ? parentLocation.getPublicId() : null;
				}

				public String getSystemId() {
					return systemId;
				}
			};
		}
	}

}
