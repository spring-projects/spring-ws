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

package org.springframework.xml.transform;

import java.io.IOException;

import javax.xml.transform.sax.SAXSource;

import org.springframework.core.io.Resource;
import org.springframework.xml.sax.SaxUtils;
import org.xml.sax.XMLReader;

/**
 * Convenient subclass of {@link SAXSource} that reads from a Spring {@link Resource}. The resource to be read can be
 * set via the constructor.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class ResourceSource extends SAXSource {

	/**
	 * Initializes a new instance of the {@code ResourceSource} with the given resource.
	 *
	 * @param content the content
	 */
	public ResourceSource(Resource content) throws IOException {
		super(SaxUtils.createInputSource(content));
	}

	/**
	 * Initializes a new instance of the {@code ResourceSource} with the given {@link XMLReader} and resource.
	 *
	 * @param content the content
	 */
	public ResourceSource(XMLReader reader, Resource content) throws IOException {
		super(reader, SaxUtils.createInputSource(content));
	}
}
