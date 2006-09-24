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
package org.springframework.webflow.support;

import junit.framework.TestCase;

import org.springframework.binding.expression.support.OgnlExpressionParser;
import org.springframework.binding.mapping.Mapping;
import org.springframework.binding.mapping.MappingBuilder;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.CollectionUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Test case for ParameterizableFlowAttributeMapper.
 * 
 * @author Erwin Vervaet
 */
public class DefaultFlowAttributeMapperTests extends TestCase {

	private DefaultFlowAttributeMapper mapper;

	private MockRequestContext context;

	private MockFlowSession parentSession;

	private MockFlowSession subflowSession;

	private MappingBuilder mapping;

	protected void setUp() throws Exception {
		mapper = new DefaultFlowAttributeMapper();
		mapping = new MappingBuilder(new OgnlExpressionParser());
		context = new MockRequestContext();
		parentSession = new MockFlowSession();
		subflowSession = new MockFlowSession();
		subflowSession.setParent(parentSession);
	}

	public void testDirectMapping() {
		mapper.addInputMapping(mapping.source("${flowScope.x}").target("${y}").value());
		mapper.addOutputMapping(mapping.source("y").target("flowScope.y").value());

		context.setActiveSession(parentSession);
		context.getFlowScope().put("x", "xValue");
		AttributeMap input = mapper.createSubflowInput(context);
		assertEquals(1, input.size());
		assertEquals("xValue", input.get("y"));

		parentSession.getScope().clear();

		AttributeMap subflowOutput = new AttributeMap();
		subflowOutput.put("y", "xValue");
		mapper.mapSubflowOutput(subflowOutput.unmodifiable(), context);
		assertEquals(1, parentSession.getScope().size());
		assertEquals("xValue", parentSession.getScope().get("y"));
	}

	public void testBeanPropertyMapping() {
		mapper.addInputMappings(new Mapping[] { mapping.source("flowScope.bean.prop").target("attr").value(),
				mapping.source("flowScope.bean").target("otherBean").value(),
				mapping.source("flowScope.otherAttr").target("otherBean.prop ").value() });
		mapper.addOutputMappings(new Mapping[] { mapping.source("bean.prop").target("flowScope.attr").value(),
				mapping.source("bean").target("flowScope.otherBean").value(),
				mapping.source("otherAttr").target("flowScope.otherBean.prop").value() });

		TestBean bean = new TestBean();
		bean.setProp("value");

		context.setActiveSession(parentSession);
		context.getFlowScope().put("bean", bean);
		context.getFlowScope().put("otherAttr", "otherValue");
		AttributeMap input = mapper.createSubflowInput(context);
		assertEquals(2, input.size());
		assertEquals("value", input.get("attr"));
		assertEquals("otherValue", ((TestBean)input.get("otherBean")).getProp());

		parentSession.getScope().clear();
		bean.setProp("value");

		AttributeMap subflowOutput = new AttributeMap();
		subflowOutput.put("bean", bean);
		subflowOutput.put("otherAttr", "otherValue");
		mapper.mapSubflowOutput(subflowOutput.unmodifiable(), context);
		assertEquals(2, parentSession.getScope().size());
		assertEquals("value", parentSession.getScope().get("attr"));
		assertEquals("otherValue", ((TestBean)parentSession.getScope().get("otherBean")).getProp());
	}

	public void testExpressionMapping() {
		mapper.addInputMappings(new Mapping[] { mapping.source("${requestScope.a}").target("b").value(),
				mapping.source("${flowScope.x}").target("y").value() });
		mapper.addOutputMappings(new Mapping[] { mapping.source("b").target("flowScope.c").value(),
				mapping.source("y").target("flowScope.z").value() });

		context.setActiveSession(parentSession);
		context.getRequestScope().put("a", "aValue");
		context.getFlowScope().put("x", "xValue");
		AttributeMap input = mapper.createSubflowInput(context);
		assertEquals(2, input.size());
		assertEquals("aValue", input.get("b"));
		assertEquals("xValue", input.get("y"));

		parentSession.getScope().clear();

		AttributeMap subflowOutput = new AttributeMap();
		subflowOutput.put("b", "aValue");
		subflowOutput.put("y", "xValue");
		mapper.mapSubflowOutput(subflowOutput.unmodifiable(), context);
		assertEquals(2, parentSession.getScope().size());
		assertEquals("aValue", parentSession.getScope().get("c"));
		assertEquals("xValue", parentSession.getScope().get("z"));
	}

	public void testNullMapping() {
		mapper.addInputMappings(new Mapping[] { mapping.source("${flowScope.x}").target("y").value(),
				mapping.source("${flowScope.a}").target("b").value() });
		mapper.addOutputMappings(new Mapping[] { mapping.source("y").target("flowScope.c").value(),
				mapping.source("b").target("flowScope.z").value() });

		parentSession.getScope().put("x", null);

		context.setActiveSession(parentSession);
		AttributeMap input = mapper.createSubflowInput(context);
		assertEquals(2, input.size());
		assertTrue(input.contains("y"));
		assertNull(input.get("y"));
		assertTrue(input.contains("b"));
		assertNull(input.get("b"));

		parentSession.getScope().clear();

		mapper.mapSubflowOutput(CollectionUtils.EMPTY_ATTRIBUTE_MAP, context);
		assertEquals(2, parentSession.getScope().size());
		assertTrue(parentSession.getScope().contains("c"));
		assertNull(parentSession.getScope().get("c"));
		assertTrue(parentSession.getScope().contains("z"));
		assertNull(parentSession.getScope().get("z"));
	}

	public void testFormActionInCombinationWithMapping() throws Exception {
		context.setLastEvent(new Event(this, "start"));

		context.setActiveSession(parentSession);
		assertTrue(context.getFlowScope().size() == 0);

		FormAction action = new FormAction();
		action.setFormObjectName("command");
		action.setFormObjectClass(TestBean.class);
		action.setFormObjectScope(ScopeType.FLOW);
		context.setAttribute("method", "setupForm");

		action.execute(context);

		assertEquals(2, context.getFlowScope().size());
		assertNotNull(context.getFlowScope().get("command"));

		mapper.addInputMapping(mapping.source("${flowScope.command}").target("command").value());
		AttributeMap input = mapper.createSubflowInput(context);

		assertEquals(1, input.size());
		assertSame(parentSession.getScope().get("command"), input.get("command"));
		assertTrue(subflowSession.getScope().size() == 0);
		subflowSession.getScope().replaceWith(input);

		context.setActiveSession(subflowSession);
		assertEquals(1, context.getFlowScope().size());

		action.execute(context);

		assertEquals(2, context.getFlowScope().size());
		assertSame(parentSession.getScope().get("command"), context.getFlowScope().get("command"));
	}

	public static class TestBean {
		private String prop;

		public String getProp() {
			return prop;
		}

		public void setProp(String prop) {
			this.prop = prop;
		}
	}
}