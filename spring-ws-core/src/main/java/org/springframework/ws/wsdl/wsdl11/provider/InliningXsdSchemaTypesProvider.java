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

package org.springframework.ws.wsdl.wsdl11.provider;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.springframework.util.Assert;
import org.springframework.ws.wsdl.WsdlDefinitionException;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;

/**
 * Implementation of {@link TypesProvider} that inlines a {@link XsdSchema} or {@link XsdSchemaCollection} into the
 * WSDL.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class InliningXsdSchemaTypesProvider extends TransformerObjectSupport implements TypesProvider {

	private static final Log logger = LogFactory.getLog(InliningXsdSchemaTypesProvider.class);

	/** The prefix used to register the schema namespace in the WSDL. */
	public static final String SCHEMA_PREFIX = "sch";

	private XsdSchemaCollection schemaCollection;

	/**
	 * Sets the single XSD schema to inline. Either this property, or {@link #setSchemaCollection(XsdSchemaCollection)
	 * schemaCollection} must be set.
	 */
	public void setSchema(final XsdSchema schema) {
		this.schemaCollection = new XsdSchemaCollection() {

			public XsdSchema[] getXsdSchemas() {
				return new XsdSchema[]{schema};
			}

			public XmlValidator createValidator() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/** Returns the XSD schema collection to inline. */
	public XsdSchemaCollection getSchemaCollection() {
		return schemaCollection;
	}

	/**
	 * Sets the XSD schema collection to inline. Either this property, or {@link #setSchema(XsdSchema) schema} must be
	 * set.
	 */
	public void setSchemaCollection(XsdSchemaCollection schemaCollection) {
		this.schemaCollection = schemaCollection;
	}

	@Override
	public void addTypes(Definition definition) throws WSDLException {
		Assert.notNull(getSchemaCollection(), "setting 'schema' or 'schemaCollection' is required");
		Types types = definition.createTypes();
		XsdSchema[] schemas = schemaCollection.getXsdSchemas();
		for (int i = 0; i < schemas.length; i++) {
			if (logger.isDebugEnabled()) {
				logger.debug("Inlining " + schemas[i]);
			}
			if (schemas.length == 1) {
				definition.addNamespace(SCHEMA_PREFIX, schemas[i].getTargetNamespace());
			}
			else {
				String prefix = SCHEMA_PREFIX + i;
				definition.addNamespace(prefix, schemas[i].getTargetNamespace());
			}
			Element schemaElement = getSchemaElement(schemas[i]);
			Schema schema = (Schema) definition.getExtensionRegistry()
					.createExtension(Types.class, new QName("http://www.w3.org/2001/XMLSchema", "schema"));
			types.addExtensibilityElement(schema);
			schema.setElement(schemaElement);
		}
		definition.setTypes(types);
	}

	private Element getSchemaElement(XsdSchema schema) {
		try {
			DOMResult result = new DOMResult();
			transform(schema.getSource(), result);
			Document schemaDocument = (Document) result.getNode();
			return schemaDocument.getDocumentElement();
		}
		catch (TransformerException e) {
			throw new WsdlDefinitionException("Could not transform schema source to Document");
		}
	}

}
