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

package org.springframework.ws.wsdl.wsdl11;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.wsdl.WsdlDefinitionException;

/**
 * Implementation of the {@code Wsdl11Definition} based on WSDL4J. A
 * {@link javax.wsdl.Definition} can be given as as constructor argument, or set using a
 * property.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.0.0
 * @see #Wsdl4jDefinition(javax.wsdl.Definition)
 * @see #setDefinition(javax.wsdl.Definition)
 */
public class Wsdl4jDefinition implements Wsdl11Definition {

	protected @Nullable String name;

	private @Nullable Definition definition;

	/** WSDL4J is not thread safe, hence the need for a monitor. */
	private final Object monitor = new Object();

	/**
	 * Constructs a new, empty {@code Wsdl4jDefinition}.
	 * @see #setDefinition(javax.wsdl.Definition)
	 */
	public Wsdl4jDefinition() {
	}

	/**
	 * Constructs a new {@code Wsdl4jDefinition} based on the given {@code Definition}.
	 * @param definition the WSDL4J definition
	 */
	public Wsdl4jDefinition(Definition definition) {
		setDefinition(definition);
	}

	@Override
	public @Nullable String getName() {
		return this.name;
	}

	/**
	 * Set the name of this definition.
	 * @param name the name
	 * @since 5.1.0
	 */
	public void setName(@Nullable String name) {
		this.name = name;
	}

	/** Returns the WSDL4J {@code Definition}. */
	public @Nullable Definition getDefinition() {
		synchronized (this.monitor) {
			return this.definition;
		}
	}

	/** Set the WSDL4J {@code Definition}. */
	public void setDefinition(Definition definition) {
		synchronized (this.monitor) {
			this.definition = definition;
		}
	}

	@Override
	public Source getSource() {
		synchronized (this.monitor) {
			Assert.notNull(this.definition, "definition must not be null");
			try {
				WSDLFactory wsdlFactory = WSDLFactory.newInstance();
				WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
				Document document = wsdlWriter.getDocument(this.definition);
				return new DOMSource(document);
			}
			catch (WSDLException ex) {
				throw new WsdlDefinitionException(ex.getMessage(), ex);
			}
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("Wsdl4jDefinition");
		if (this.definition != null && StringUtils.hasLength(this.definition.getTargetNamespace())) {
			builder.append('{');
			builder.append(this.definition.getTargetNamespace());
			builder.append('}');
		}
		return builder.toString();
	}

}
