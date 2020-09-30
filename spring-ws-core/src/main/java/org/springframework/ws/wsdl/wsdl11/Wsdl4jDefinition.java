/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.wsdl.wsdl11;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.wsdl.WsdlDefinitionException;
import org.w3c.dom.Document;

/**
 * Implementation of the {@code Wsdl11Definition} based on WSDL4J. A {@link javax.wsdl.Definition} can be given as as
 * constructor argument, or set using a property.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @see #Wsdl4jDefinition(javax.wsdl.Definition)
 * @see #setDefinition(javax.wsdl.Definition)
 * @since 1.0.0
 */
public class Wsdl4jDefinition implements Wsdl11Definition {

	private Definition definition;

	/** WSDL4J is not thread safe, hence the need for a monitor. */
	private final Object monitor = new Object();

	/**
	 * Constructs a new, empty {@code Wsdl4jDefinition}.
	 *
	 * @see #setDefinition(javax.wsdl.Definition)
	 */
	public Wsdl4jDefinition() {}

	/**
	 * Constructs a new {@code Wsdl4jDefinition} based on the given {@code Definition}.
	 *
	 * @param definition the WSDL4J definition
	 */
	public Wsdl4jDefinition(Definition definition) {
		setDefinition(definition);
	}

	/** Returns the WSDL4J {@code Definition}. */
	public Definition getDefinition() {
		synchronized (monitor) {
			return definition;
		}
	}

	/** Set the WSDL4J {@code Definition}. */
	public void setDefinition(Definition definition) {
		synchronized (monitor) {
			this.definition = definition;
		}
	}

	@Override
	public Source getSource() {
		synchronized (monitor) {
			Assert.notNull(definition, "definition must not be null");
			try {
				WSDLFactory wsdlFactory = WSDLFactory.newInstance();
				WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
				Document document = wsdlWriter.getDocument(definition);
				return new DOMSource(document);
			} catch (WSDLException ex) {
				throw new WsdlDefinitionException(ex.getMessage(), ex);
			}
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("Wsdl4jDefinition");
		if (definition != null && StringUtils.hasLength(definition.getTargetNamespace())) {
			builder.append('{');
			builder.append(definition.getTargetNamespace());
			builder.append('}');
		}
		return builder.toString();
	}
}
