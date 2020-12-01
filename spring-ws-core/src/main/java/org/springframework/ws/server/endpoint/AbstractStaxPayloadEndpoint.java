/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.server.endpoint;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.springframework.xml.XMLInputFactoryUtils;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Abstract base class for endpoints use StAX. Provides an {@code XMLInputFactory} and an {@code XMLOutputFactory}.
 *
 * @author Arjen Poutsma
 * @see XMLInputFactory
 * @see XMLOutputFactory
 * @since 1.0.0
 * @deprecated as of Spring Web Services 2.0, in favor of annotated endpoints
 */
@Deprecated
@SuppressWarnings("Since15")
public abstract class AbstractStaxPayloadEndpoint extends TransformerObjectSupport {

	private XMLInputFactory inputFactory;

	private XMLOutputFactory outputFactory;

	/** Returns an {@code XMLInputFactory} to read XML from. */
	protected final XMLInputFactory getInputFactory() {
		if (inputFactory == null) {
			inputFactory = createXmlInputFactory();
		}
		return inputFactory;
	}

	/** Returns an {@code XMLOutputFactory} to write XML to. */
	protected final XMLOutputFactory getOutputFactory() {
		if (outputFactory == null) {
			outputFactory = createXmlOutputFactory();
		}
		return outputFactory;
	}

	/**
	 * Create a {@code XMLInputFactory} that this endpoint will use to create {@code XMLStreamReader}s or
	 * {@code XMLEventReader}. Can be overridden in subclasses, adding further initialization of the factory. The
	 * resulting {@code XMLInputFactory} is cached, so this method will only be called once.
	 *
	 * @return the created {@code XMLInputFactory}
	 */
	protected XMLInputFactory createXmlInputFactory() {
		return XMLInputFactoryUtils.newInstance();
	}

	/**
	 * Create a {@code XMLOutputFactory} that this endpoint will use to create {@code XMLStreamWriters}s or
	 * {@code XMLEventWriters}. Can be overridden in subclasses, adding further initialization of the factory. The
	 * resulting {@code XMLOutputFactory} is cached, so this method will only be called once.
	 *
	 * @return the created {@code XMLOutputFactory}
	 */
	protected XMLOutputFactory createXmlOutputFactory() {
		return XMLOutputFactory.newInstance();
	}
}
