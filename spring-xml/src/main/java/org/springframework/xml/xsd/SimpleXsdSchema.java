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

package org.springframework.xml.xsd;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;

/**
 * The default {@link XsdSchema} implementation.
 * <p>
 * Allows a XSD to be set by the {@link #setXsd(Resource)}, or directly in the
 * {@link #SimpleXsdSchema(Resource) constructor}.
 *
 * @author Mark LaFond
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.5.0
 */
public class SimpleXsdSchema implements XsdSchema, BeanNameAware, InitializingBean {

	private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();

	private static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

	private static final QName SCHEMA_NAME = new QName(SCHEMA_NAMESPACE, "schema", "xsd");

	private @Nullable String name;

	private @Nullable Resource xsdResource;

	private @Nullable String targetNamespace;

	static {
		documentBuilderFactory.setNamespaceAware(true);
	}

	/**
	 * Create a new instance of the {@link SimpleXsdSchema} class.
	 * <p>
	 * A subsequent call to the {@link #setXsd(Resource)} method is required.
	 */
	public SimpleXsdSchema() {
	}

	/**
	 * Create a new instance of the {@link SimpleXsdSchema} class with the specified
	 * resource.
	 * @param xsdResource the XSD resource; must not be {@code null}
	 * @throws IllegalArgumentException if the supplied {@code xsdResource} is
	 * {@code null}
	 */
	public SimpleXsdSchema(Resource xsdResource) {
		this(xsdResource, null);
	}

	/**
	 * Create a new instance of the {@link SimpleXsdSchema} class with the specified
	 * resource and name.
	 * @param xsdResource the XSD resource; must not be {@code null}
	 * @param name name of the schema
	 * @throws IllegalArgumentException if the supplied {@code xsdResource} is
	 * {@code null}
	 * @since 5.1.0
	 */
	public SimpleXsdSchema(Resource xsdResource, @Nullable String name) {
		Assert.notNull(xsdResource, "xsdResource must not be null");
		this.xsdResource = xsdResource;
		this.name = name;
	}

	@Override
	public @Nullable String getName() {
		return this.name;
	}

	/**
	 * Set the name of this schem.
	 * @param name the name
	 * @since 5.1.0
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the XSD resource to be exposed by calls to this instances' {@link #getSource()}
	 * method.
	 * @param xsdResource the XSD resource
	 */
	public void setXsd(Resource xsdResource) {
		this.xsdResource = xsdResource;
	}

	@Override
	public String getTargetNamespace() {
		Assert.state(this.targetNamespace != null,
				() -> "'targetNamespace' cannot be accessed before afterPropertiesSet() has been called on this instance");
		return this.targetNamespace;
	}

	@Override
	public Source getSource() {
		Assert.state(this.xsdResource != null, () -> "Source cannot be created from a null XSD resource");
		try {
			return new ResourceSource(this.xsdResource);
		}
		catch (IOException ex) {
			throw new XsdSchemaException(ex.getMessage(), ex);
		}
	}

	@Override
	public XmlValidator createValidator() {
		Assert.state(this.xsdResource != null, () -> "XmlValidator cannot be created from a null XSD resource");
		try {
			return XmlValidatorFactory.createValidator(this.xsdResource, XmlValidatorFactory.SCHEMA_W3C_XML);
		}
		catch (IOException ex) {
			throw new XsdSchemaException(ex.getMessage(), ex);
		}
	}

	@Override
	public void setBeanName(String name) {
		if (this.name == null) {
			this.name = name;
		}
	}

	@Override
	public void afterPropertiesSet() throws ParserConfigurationException, IOException, SAXException {
		Assert.notNull(this.xsdResource, "'xsd' is required");
		Assert.isTrue(this.xsdResource.exists(), "xsd '" + this.xsdResource + "' does not exist");
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		this.targetNamespace = loadSchema(documentBuilder, this.xsdResource);
	}

	private static String loadSchema(DocumentBuilder documentBuilder, Resource resource)
			throws SAXException, IOException {
		Document schemaDocument = documentBuilder.parse(SaxUtils.createInputSource(resource));
		Element schemaElement = schemaDocument.getDocumentElement();
		Assert.isTrue(SCHEMA_NAME.getLocalPart().equals(schemaElement.getLocalName()),
				resource + " has invalid root element : [" + schemaElement.getLocalName() + "] instead of [schema]");
		Assert.isTrue(SCHEMA_NAME.getNamespaceURI().equals(schemaElement.getNamespaceURI()),
				resource + " has invalid root element: [" + schemaElement.getNamespaceURI() + "] instead of ["
						+ SCHEMA_NAME.getNamespaceURI() + "]");
		String targetNamespace = schemaElement.getAttribute("targetNamespace");
		Assert.hasText(targetNamespace, resource + " has no targetNamespace");
		return targetNamespace;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("SimpleXsdSchema");
		builder.append('{');
		builder.append(getTargetNamespace());
		builder.append('}');
		return builder.toString();
	}

}
