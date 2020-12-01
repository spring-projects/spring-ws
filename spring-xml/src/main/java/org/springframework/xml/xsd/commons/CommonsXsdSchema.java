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

package org.springframework.xml.xsd.commons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaSerializer;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.XsdSchema;
import org.w3c.dom.Document;

/**
 * Implementation of the {@link XsdSchema} interface that uses Apache WS-Commons XML Schema.
 *
 * @author Arjen Poutsma
 * @see <a href="http://ws.apache.org/commons/XmlSchema/">Commons XML Schema</a>
 * @since 1.5.0
 */
public class CommonsXsdSchema implements XsdSchema {

	private final XmlSchema schema;

	private final XmlSchemaCollection collection;

	/**
	 * Create a new instance of the {@code CommonsXsdSchema} class with the specified {@link XmlSchema} reference.
	 *
	 * @param schema the Commons {@code XmlSchema} object; must not be {@code null}
	 * @throws IllegalArgumentException if the supplied {@code schema} is {@code null}
	 */
	protected CommonsXsdSchema(XmlSchema schema) {
		this(schema, null);
	}

	/**
	 * Create a new instance of the {@code CommonsXsdSchema} class with the specified {@link XmlSchema} and
	 * {@link XmlSchemaCollection} reference.
	 *
	 * @param schema the Commons {@code XmlSchema} object; must not be {@code null}
	 * @param collection the Commons {@code XmlSchemaCollection} object; can be {@code null}
	 * @throws IllegalArgumentException if the supplied {@code schema} is {@code null}
	 */
	protected CommonsXsdSchema(XmlSchema schema, XmlSchemaCollection collection) {
		Assert.notNull(schema, "'schema' must not be null");
		this.schema = schema;
		this.collection = collection;
	}

	@Override
	public String getTargetNamespace() {
		return schema.getTargetNamespace();
	}

	public QName[] getElementNames() {
		List<QName> result = new ArrayList<QName>(schema.getElements().keySet());
		return result.toArray(new QName[result.size()]);
	}

	@Override
	public Source getSource() {
		// try to use the package-friendly XmlSchemaSerializer first, fall back to slower stream-based version
		try {
			XmlSchemaSerializer serializer = BeanUtils.instantiateClass(XmlSchemaSerializer.class);
			if (collection != null) {
				serializer.setExtReg(collection.getExtReg());
			}
			Document[] serializedSchemas = serializer.serializeSchema(schema, false);
			return new DOMSource(serializedSchemas[0]);
		} catch (BeanInstantiationException ex) {
			// ignore
		} catch (XmlSchemaSerializer.XmlSchemaSerializerException ex) {
			// ignore
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			schema.write(bos);
		} catch (UnsupportedEncodingException ex) {
			throw new CommonsXsdSchemaException(ex.getMessage(), ex);
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		return new StreamSource(bis);
	}

	@Override
	public XmlValidator createValidator() {
		try {
			Resource resource = new UrlResource(schema.getSourceURI());
			return XmlValidatorFactory.createValidator(resource, XmlValidatorFactory.SCHEMA_W3C_XML);
		} catch (IOException ex) {
			throw new CommonsXsdSchemaException(ex.getMessage(), ex);
		}
	}

	/** Returns the wrapped Commons {@code XmlSchema} object. */
	public XmlSchema getSchema() {
		return schema;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("CommonsXsdSchema");
		builder.append('{');
		builder.append(getTargetNamespace());
		builder.append('}');
		return builder.toString();
	}

}
