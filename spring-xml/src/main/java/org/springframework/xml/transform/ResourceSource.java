/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.transform;

import java.io.IOException;

import javax.xml.transform.sax.SAXSource;

import org.xml.sax.XMLReader;

import org.springframework.core.io.Resource;
import org.springframework.xml.sax.SaxUtils;

/**
 * Convenient subclass of {@link SAXSource} that reads from a Spring {@link Resource}. The
 * resource to be read can be set via the constructor.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class ResourceSource extends SAXSource {

	/**
	 * Initializes a new instance of the {@code ResourceSource} with the given resource.
	 * @param content the content
	 */
	public ResourceSource(Resource content) throws IOException {
		super(SaxUtils.createInputSource(content));
	}

	/**
	 * Initializes a new instance of the {@code ResourceSource} with the given
	 * {@link XMLReader} and resource.
	 * @param content the content
	 */
	public ResourceSource(XMLReader reader, Resource content) throws IOException {
		super(reader, SaxUtils.createInputSource(content));
	}

}
