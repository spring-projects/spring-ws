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

package org.springframework.ws.server.endpoint.adapter.method;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.springframework.core.MethodParameter;
import org.springframework.util.xml.StaxUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.xml.XMLInputFactoryUtils;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Implementation of {@link MethodArgumentResolver} that supports StAX {@link XMLStreamReader} and
 * {@link XMLEventReader} arguments.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class StaxPayloadMethodArgumentResolver extends TransformerObjectSupport implements MethodArgumentResolver {

	private final XMLInputFactory inputFactory = createXmlInputFactory();

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (parameter.getParameterAnnotation(RequestPayload.class) == null) {
			return false;
		} else {
			Class<?> parameterType = parameter.getParameterType();
			return XMLStreamReader.class.equals(parameterType) || XMLEventReader.class.equals(parameterType);
		}
	}

	@Override
	public Object resolveArgument(MessageContext messageContext, MethodParameter parameter)
			throws TransformerException, XMLStreamException {
		Source source = messageContext.getRequest().getPayloadSource();
		if (source == null) {
			return null;
		}
		Class<?> parameterType = parameter.getParameterType();
		if (XMLStreamReader.class.equals(parameterType)) {
			return resolveStreamReader(source);
		} else if (XMLEventReader.class.equals(parameterType)) {
			return resolveEventReader(source);
		}
		throw new UnsupportedOperationException();
	}

	private XMLStreamReader resolveStreamReader(Source requestSource) throws TransformerException, XMLStreamException {
		XMLStreamReader streamReader = null;
		if (StaxUtils.isStaxSource(requestSource)) {
			streamReader = StaxUtils.getXMLStreamReader(requestSource);
			if (streamReader == null) {
				XMLEventReader eventReader = StaxUtils.getXMLEventReader(requestSource);
				if (eventReader != null) {
					try {
						streamReader = StaxUtils.createEventStreamReader(eventReader);
					} catch (XMLStreamException ex) {
						streamReader = null;
					}
				}
			}
		}
		if (streamReader == null) {
			try {
				streamReader = inputFactory.createXMLStreamReader(requestSource);
			} catch (XMLStreamException ex) {
				streamReader = null;
			} catch (UnsupportedOperationException ex) {
				streamReader = null;
			}
		}
		if (streamReader == null) {
			// as a final resort, transform the source to a stream, and read from that
			ByteArrayInputStream bis = convertToByteArrayInputStream(requestSource);
			streamReader = inputFactory.createXMLStreamReader(bis);
		}
		return streamReader;
	}

	private XMLEventReader resolveEventReader(Source requestSource) throws TransformerException, XMLStreamException {
		XMLEventReader eventReader = null;
		if (StaxUtils.isStaxSource(requestSource)) {
			eventReader = StaxUtils.getXMLEventReader(requestSource);
			if (eventReader == null) {
				XMLStreamReader streamReader = StaxUtils.getXMLStreamReader(requestSource);
				if (streamReader != null) {
					try {
						eventReader = inputFactory.createXMLEventReader(streamReader);
					} catch (XMLStreamException ex) {
						eventReader = null;
					}
				}

			}
		}
		if (eventReader == null) {
			try {
				eventReader = inputFactory.createXMLEventReader(requestSource);
			} catch (XMLStreamException ex) {
				eventReader = null;
			} catch (UnsupportedOperationException ex) {
				eventReader = null;
			}
		}
		if (eventReader == null) {
			// as a final resort, transform the source to a stream, and read from that
			ByteArrayInputStream bis = convertToByteArrayInputStream(requestSource);
			eventReader = inputFactory.createXMLEventReader(bis);
		}
		return eventReader;
	}

	/**
	 * Create a {@code XMLInputFactory} that this resolver will use to create {@link XMLStreamReader} and
	 * {@link XMLEventReader} objects.
	 * <p>
	 * Can be overridden in subclasses, adding further initialization of the factory. The resulting factory is cached, so
	 * this method will only be called once.
	 *
	 * @return the created factory
	 */
	protected XMLInputFactory createXmlInputFactory() {
		return XMLInputFactoryUtils.newInstance();
	}

	private ByteArrayInputStream convertToByteArrayInputStream(Source source) throws TransformerException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		transform(source, new StreamResult(bos));
		return new ByteArrayInputStream(bos.toByteArray());
	}

}
