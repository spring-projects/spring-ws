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

package org.springframework.xml.validation;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.xml.transform.ResourceSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Convenient utility methods for loading of {@link Schema} objects, performing standard handling of input streams.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class SchemaLoaderUtils {

	/**
	 * Load schema from the given resource.
	 *
	 * @param resource the resource to load from
	 * @param schemaLanguage the language of the schema. Can be {@code XMLConstants.W3C_XML_SCHEMA_NS_URI} or
	 *          {@code XMLConstants.RELAXNG_NS_URI}.
	 * @throws IOException if loading failed
	 * @throws SAXException if loading failed
	 * @see javax.xml.XMLConstants#W3C_XML_SCHEMA_NS_URI
	 * @see javax.xml.XMLConstants#RELAXNG_NS_URI
	 */
	public static Schema loadSchema(Resource resource, String schemaLanguage) throws IOException, SAXException {
		return loadSchema(new Resource[] { resource }, schemaLanguage);
	}

	/**
	 * Load schema from the given resource.
	 *
	 * @param resources the resources to load from
	 * @param schemaLanguage the language of the schema. Can be {@code XMLConstants.W3C_XML_SCHEMA_NS_URI} or
	 *          {@code XMLConstants.RELAXNG_NS_URI}.
	 * @throws IOException if loading failed
	 * @throws SAXException if loading failed
	 * @see javax.xml.XMLConstants#W3C_XML_SCHEMA_NS_URI
	 * @see javax.xml.XMLConstants#RELAXNG_NS_URI
	 */
	public static Schema loadSchema(Resource[] resources, String schemaLanguage) throws IOException, SAXException {
		Assert.notEmpty(resources, "No resources given");
		Assert.hasLength(schemaLanguage, "No schema language provided");
		Source[] schemaSources = new Source[resources.length];
		XMLReader xmlReader = XMLReaderFactoryUtils.createXMLReader();
		xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
		for (int i = 0; i < resources.length; i++) {
			Assert.notNull(resources[i], "Resource is null");
			Assert.isTrue(resources[i].exists(), "Resource " + resources[i] + " does not exist");
			schemaSources[i] = new ResourceSource(xmlReader, resources[i]);
		}
		SchemaFactory schemaFactory = SchemaFactoryUtils.newInstance(schemaLanguage);
		return schemaFactory.newSchema(schemaSources);
	}

	/** Retrieves the URL from the given resource as System ID. Returns {@code null} if it cannot be opened. */
	public static String getSystemId(Resource resource) {
		try {
			return resource.getURL().toString();
		} catch (IOException e) {
			return null;
		}
	}

}
