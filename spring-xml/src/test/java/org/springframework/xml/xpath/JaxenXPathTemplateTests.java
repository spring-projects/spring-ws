/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xpath;

import java.util.HashMap;
import java.util.Map;

class JaxenXPathTemplateTests extends AbstractXPathTemplateTests {

	@Override
	protected XPathOperations createTemplate() {

		JaxenXPathTemplate template = new JaxenXPathTemplate();
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put("prefix1", "namespace1");
		namespaces.put("prefix2", "namespace2");
		template.setNamespaces(namespaces);

		return template;
	}

}
