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

package org.springframework.xml.xsd.commons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.xml.sax.InputSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;

/**
 * Implementation of the {@link XsdSchemaCollection} that uses Apache WS-Commons XML Schema.
 *
 * <p>Setting the {@link #setInline(boolean) inline} flag to {@code true} will result in all referenced schemas
 * (included and imported) being merged into the referred schema. When including the schemas into a WSDL, this greatly
 * simplifies the deployment of the schemas.
 *
 * @author Arjen Poutsma
 * @see <a href="http://ws.apache.org/commons/XmlSchema/">Commons XML Schema</a>
 * @since 1.5.0
 */
public class CommonsXsdSchemaCollection implements XsdSchemaCollection, InitializingBean, ResourceLoaderAware {

	private static final Log logger = LogFactory.getLog(CommonsXsdSchemaCollection.class);

	private final XmlSchemaCollection schemaCollection = new XmlSchemaCollection();

	private final List<XmlSchema> xmlSchemas = new ArrayList<XmlSchema>();

	private Resource[] xsdResources;

	private boolean inline = false;

	private URIResolver uriResolver = new ClasspathUriResolver();

	private ResourceLoader resourceLoader;

	/**
	 * Constructs a new, empty instance of the {@code CommonsXsdSchemaCollection}.
	 *
	 * <p>A subsequent call to the {@link #setXsds(Resource[])} is required.
	 */
	public CommonsXsdSchemaCollection() {
	}

	/**
	 * Constructs a new instance of the {@code CommonsXsdSchemaCollection} based on the given resources.
	 *
	 * @param resources the schema resources to load
	 */
	public CommonsXsdSchemaCollection(Resource[] resources) {
		this.xsdResources = resources;
	}

	/**
	 * Sets the schema resources to be loaded.
	 *
	 * @param xsdResources the schema resources to be loaded
	 */
	public void setXsds(Resource[] xsdResources) {
		this.xsdResources = xsdResources;
	}

	/**
	 * Defines whether included schemas should be inlined into the including schema.
	 *
	 * <p>Defaults to {@code false}.
	 */
	public void setInline(boolean inline) {
		this.inline = inline;
	}

	/**
	 * Sets the WS-Commons uri resolver to use when resolving (relative) schemas.
	 *
	 * <p>Default is an internal subclass of {@link DefaultURIResolver} which correctly handles schemas on the classpath.
	 */
	public void setUriResolver(URIResolver uriResolver) {
		Assert.notNull(uriResolver, "'uriResolver' must not be null");
		this.uriResolver = uriResolver;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void afterPropertiesSet() throws IOException {
		Assert.notEmpty(xsdResources, "'xsds' must not be empty");

		schemaCollection.setSchemaResolver(uriResolver);

		Set<XmlSchema> processedIncludes = new HashSet<XmlSchema>();
		Set<XmlSchema> processedImports = new HashSet<XmlSchema>();

		for (Resource xsdResource : xsdResources) {
			Assert.isTrue(xsdResource.exists(), xsdResource + " does not exit");
			try {
				XmlSchema xmlSchema =
						schemaCollection.read(SaxUtils.createInputSource(xsdResource));
				xmlSchemas.add(xmlSchema);

				if (inline) {
					inlineIncludes(xmlSchema, processedIncludes, processedImports);
					findImports(xmlSchema, processedImports, processedIncludes);
				}
			}
			catch (Exception ex) {
				throw new CommonsXsdSchemaException("Schema [" + xsdResource + "] could not be loaded", ex);
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("Loaded " + StringUtils.arrayToCommaDelimitedString(xsdResources));
		}

	}

	@Override
	public XsdSchema[] getXsdSchemas() {
		XsdSchema[] result = new XsdSchema[xmlSchemas.size()];
		for (int i = 0; i < xmlSchemas.size(); i++) {
			XmlSchema xmlSchema = xmlSchemas.get(i);
			result[i] = new CommonsXsdSchema(xmlSchema, schemaCollection);
		}
		return result;
	}

	@Override
	public XmlValidator createValidator() {
		try {
			Resource[] resources = new Resource[xmlSchemas.size()];
			for (int i = xmlSchemas.size() - 1; i >= 0; i--) {
				XmlSchema xmlSchema = xmlSchemas.get(i);
				String sourceUri = xmlSchema.getSourceURI();
				if (StringUtils.hasLength(sourceUri)) {
					resources[i] = new UrlResource(sourceUri);
				}
			}
			return XmlValidatorFactory
					.createValidator(resources, XmlValidatorFactory.SCHEMA_W3C_XML);
		} catch (IOException ex) {
			throw new CommonsXsdSchemaException(ex.getMessage(), ex);
		}
	}

	private void inlineIncludes(XmlSchema schema, Set<XmlSchema> processedIncludes, Set<XmlSchema> processedImports) {
		processedIncludes.add(schema);

		List<XmlSchemaObject> schemaItems = schema.getItems();
		for (int i = 0; i < schemaItems.size(); i++) {
			XmlSchemaObject schemaObject = schemaItems.get(i);
			if (schemaObject instanceof XmlSchemaInclude) {
				XmlSchema includedSchema = ((XmlSchemaInclude) schemaObject).getSchema();
				if (!processedIncludes.contains(includedSchema)) {
					inlineIncludes(includedSchema, processedIncludes, processedImports);
					findImports(includedSchema, processedImports, processedIncludes);
					List<XmlSchemaObject> includeItems = includedSchema.getItems();
					for (XmlSchemaObject includedItem : includeItems) {
						schemaItems.add(includedItem);
					}
				}
				// remove the <include/>
				schemaItems.remove(i);
				i--;
			}
		}
	}

	private void findImports(XmlSchema schema, Set<XmlSchema> processedImports, Set<XmlSchema> processedIncludes) {
		processedImports.add(schema);
		List<XmlSchemaExternal> externals = schema.getExternals();
		for (XmlSchemaExternal external : externals) {
			if (external instanceof XmlSchemaImport) {
				XmlSchemaImport schemaImport = (XmlSchemaImport) external;
				XmlSchema importedSchema = schemaImport.getSchema();
				if (!"http://www.w3.org/XML/1998/namespace".equals(schemaImport.getNamespace()) &&
						importedSchema != null && !processedImports.contains(importedSchema)) {
					inlineIncludes(importedSchema, processedIncludes, processedImports);
					findImports(importedSchema, processedImports, processedIncludes);
					xmlSchemas.add(importedSchema);
				}
				// remove the schemaLocation
				external.setSchemaLocation(null);
			}
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("CommonsXsdSchemaCollection");
		builder.append('{');
		for (int i = 0; i < xmlSchemas.size(); i++) {
			XmlSchema schema = xmlSchemas.get(i);
			builder.append(schema.getTargetNamespace());
			if (i < xmlSchemas.size() - 1) {
				builder.append(',');
			}
		}
		builder.append('}');
		return builder.toString();
	}

	private class ClasspathUriResolver extends DefaultURIResolver {

		@Override
		public InputSource resolveEntity(String namespace, String schemaLocation, String baseUri) {
			if (resourceLoader != null) {
				Resource resource = resourceLoader.getResource(schemaLocation);
				if (resource.exists()) {
					return createInputSource(resource);
				}
				else if (StringUtils.hasLength(baseUri)) {
					// let's try and find it relative to the baseUri, see SWS-413
					try {
						Resource baseUriResource = new UrlResource(baseUri);
						resource = baseUriResource.createRelative(schemaLocation);
						if (resource.exists()) {
							return createInputSource(resource);
						}
					}
					catch (IOException e) {
						// fall through
					}
				}
				// let's try and find it on the classpath, see SWS-362
				String classpathLocation = ResourceLoader.CLASSPATH_URL_PREFIX + "/" + schemaLocation;
				resource = resourceLoader.getResource(classpathLocation);
				if (resource.exists()) {
					return createInputSource(resource);
				}
			}
			return super.resolveEntity(namespace, schemaLocation, baseUri);
		}

		private InputSource createInputSource(Resource resource) {
			try {
				return SaxUtils.createInputSource(resource);
			}
			catch (IOException ex) {
				throw new CommonsXsdSchemaException("Could not resolve location", ex);
			}
		}
	}

}
