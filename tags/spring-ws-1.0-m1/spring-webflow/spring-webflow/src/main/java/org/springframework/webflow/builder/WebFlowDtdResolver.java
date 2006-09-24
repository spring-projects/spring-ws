/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.builder;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * EntityResolver implementation for the Spring Web Flow 1.0 DTD, to load the
 * DTD from the classpath. The implementation is similar to that of the
 * <code>org.springframework.beans.factory.xml.BeansDtdResolver</code>.
 * <p>
 * The doctype of the DTD expected to be resolved:
 * 
 * <pre>
 *     &lt;!DOCTYPE flow PUBLIC &quot;-//SPRING//DTD WEBFLOW 1.0//EN&quot;
 *     &quot;http://www.springframework.org/dtd/spring-webflow-1.0.dtd&quot;&gt;
 * </pre>
 * 
 * @see org.springframework.webflow.builder.XmlFlowBuilder
 * 
 * @author Erwin Vervaet
 */
public class WebFlowDtdResolver implements EntityResolver {

	private static final String WEBFLOW_ELEMENT = "spring-webflow-1.0";

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (systemId != null && systemId.indexOf(WEBFLOW_ELEMENT) > systemId.lastIndexOf("/")) {
			String dtdFilename = systemId.substring(systemId.indexOf(WEBFLOW_ELEMENT));
			try {
				Resource resource = new ClassPathResource(dtdFilename, getClass());
				InputSource source = new InputSource(resource.getInputStream());
				source.setPublicId(publicId);
				source.setSystemId(systemId);
				return source;
			}
			catch (IOException ex) {
				// fall through below
			}
		}
		// let the parser handle it
		return null;
	}
}