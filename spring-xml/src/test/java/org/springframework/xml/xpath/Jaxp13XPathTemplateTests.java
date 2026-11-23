/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xpath;

import java.util.HashMap;
import java.util.Map;

class Jaxp13XPathTemplateTests extends AbstractXPathTemplateTests {

	@Override
	protected XPathOperations createTemplate() {

		Jaxp13XPathTemplate template = new Jaxp13XPathTemplate();
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put("prefix1", "namespace1");
		namespaces.put("prefix2", "namespace2");
		template.setNamespaces(namespaces);

		return template;
	}

}
