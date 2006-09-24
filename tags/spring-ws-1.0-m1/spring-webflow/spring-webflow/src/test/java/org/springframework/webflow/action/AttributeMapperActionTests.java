/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.binding.mapping.DefaultAttributeMapper;
import org.springframework.binding.mapping.MappingBuilder;
import org.springframework.webflow.support.WebFlowOgnlExpressionParser;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit test for the AttributeMapperAction.
 * 
 * @see org.springframework.webflow.action.AttributeMapperAction
 * 
 * @author Erwin Vervaet
 */
public class AttributeMapperActionTests extends TestCase {

	public void testMapping() throws Exception {
		DefaultAttributeMapper mapper = new DefaultAttributeMapper();
		mapper.addMapping(new MappingBuilder(new WebFlowOgnlExpressionParser()).source(
				"${externalContext.requestParameterMap.foo}").target("${flowScope.bar}").value());
		AttributeMapperAction action = new AttributeMapperAction(mapper);

		MockRequestContext context = new MockRequestContext();
		context.putRequestParameter("foo", "value");

		assertTrue(context.getFlowScope().size() == 0);

		action.execute(context);

		assertEquals(1, context.getFlowScope().size());
		assertEquals("value", context.getFlowScope().get("bar"));
	}
}
