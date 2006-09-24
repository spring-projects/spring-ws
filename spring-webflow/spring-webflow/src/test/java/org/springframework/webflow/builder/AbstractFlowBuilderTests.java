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
package org.springframework.webflow.builder;

import junit.framework.TestCase;

import org.springframework.webflow.Action;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.AnnotatedAction;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.Transition;
import org.springframework.webflow.UnmodifiableAttributeMap;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Test Java based flow builder logic (subclasses of AbstractFlowBuilder).
 * 
 * @see org.springframework.webflow.builder.AbstractFlowBuilder
 * 
 * @author Keith Donald
 * @author Rod Johnson
 * @author Colin Sampaleanu
 */
public class AbstractFlowBuilderTests extends TestCase {

	private String PERSONS_LIST = "person.List";

	private static String PERSON_DETAILS = "person.Detail";

	private AbstractFlowBuilder builder = createBuilder();
	
	protected AbstractFlowBuilder createBuilder() {
		return new AbstractFlowBuilder() {
			public void buildStates() {
				addEndState("finish");
			}
		};
	}
	public void testDependencyLookup() {
		TestMasterFlowBuilderLookupById master = new TestMasterFlowBuilderLookupById();
		master.setFlowServiceLocator(new BaseFlowServiceLocator() {
			public Flow getSubflow(String id) throws FlowArtifactException {
				if (id.equals(PERSON_DETAILS)) {
					BaseFlowBuilder builder = new TestDetailFlowBuilderLookupById();
					builder.setFlowServiceLocator(this);
					FlowAssembler assembler = new FlowAssembler(PERSON_DETAILS, builder);
					assembler.assembleFlow();
					return builder.getFlow();
				}
				else {
					throw new FlowArtifactException(id, Flow.class);
				}
			}

			public Action getAction(String id) throws FlowArtifactException {
				return new NoOpAction();
			}

			public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
				if (id.equals("id.attributeMapper")) {
					return new PersonIdMapper();
				}
				else {
					throw new FlowArtifactException(id, FlowAttributeMapper.class);
				}
			}
		});

		FlowAssembler assembler = new FlowAssembler(PERSONS_LIST, master);
		assembler.assembleFlow();
		Flow flow = master.getFlow();

		assertEquals("person.List", flow.getId());
		assertTrue(flow.getStateCount() == 4);
		assertTrue(flow.containsState("getPersonList"));
		assertTrue(flow.getState("getPersonList") instanceof ActionState);
		assertTrue(flow.containsState("viewPersonList"));
		assertTrue(flow.getState("viewPersonList") instanceof ViewState);
		assertTrue(flow.containsState("person.Detail"));
		assertTrue(flow.getState("person.Detail") instanceof SubflowState);
		assertTrue(flow.containsState("finish"));
		assertTrue(flow.getState("finish") instanceof EndState);
	}

	public void testNoArtifactFactorySet() {
		TestMasterFlowBuilderLookupById master = new TestMasterFlowBuilderLookupById();
		try {
			FlowAssembler assembler = new FlowAssembler(PERSONS_LIST, master);
			assembler.assembleFlow();
			fail("Should have failed, artifact lookup not supported");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}
	}

	public class TestMasterFlowBuilderLookupById extends AbstractFlowBuilder {
		public void buildStates() {
			addActionState("getPersonList", action("noOpAction"), transition(on(success()), to("viewPersonList")));
			addViewState("viewPersonList", "person.list.view", transition(on(submit()), to("person.Detail")));
			addSubflowState(PERSON_DETAILS, flow("person.Detail"), attributeMapper("id.attributeMapper"), transition(
					on("*"), to("getPersonList")));
			addEndState("finish");
		}
	}

	public class TestMasterFlowBuilderDependencyInjection extends AbstractFlowBuilder {
		private NoOpAction noOpAction;

		private Flow subFlow;

		private PersonIdMapper personIdMapper;

		public void setNoOpAction(NoOpAction noOpAction) {
			this.noOpAction = noOpAction;
		}

		public void setPersonIdMapper(PersonIdMapper personIdMapper) {
			this.personIdMapper = personIdMapper;
		}

		public void setSubFlow(Flow subFlow) {
			this.subFlow = subFlow;
		}

		public void buildStates() {
			addActionState("getPersonList", noOpAction, transition(on(success()), to("viewPersonList")));
			addViewState("viewPersonList", "person.list.view", transition(on(submit()), to("person.Detail")));
			addSubflowState(PERSON_DETAILS, subFlow, personIdMapper, transition(on("*"), to("getPersonList")));
			addEndState("finish");
		}
	}

	public static class PersonIdMapper implements FlowAttributeMapper {
		public AttributeMap createSubflowInput(RequestContext context) {
			AttributeMap inputMap = new AttributeMap();
			inputMap.put("personId", context.getFlowScope().get("personId"));
			return inputMap;
		}

		public void mapSubflowOutput(UnmodifiableAttributeMap subflowOutput, RequestContext context) {
		}
	}

	public static class TestDetailFlowBuilderLookupById extends AbstractFlowBuilder {
		public void buildStates() {
			addActionState("getDetails", action("noOpAction"), transition(on(success()), to("viewDetails")));
			addViewState("viewDetails", "person.Detail.view", transition(on(submit()), to("bindAndValidateDetails")));
			addActionState("bindAndValidateDetails", action("noOpAction"), new Transition[] {
					transition(on(error()), to("viewDetails")), transition(on(success()), to("finish")) });
			addEndState("finish");
		}
	}

	public static class TestDetailFlowBuilderDependencyInjection extends AbstractFlowBuilder {

		private NoOpAction noOpAction;

		public void setNoOpAction(NoOpAction noOpAction) {
			this.noOpAction = noOpAction;
		}

		public void buildStates() {
			addActionState("getDetails", noOpAction, transition(on(success()), to("viewDetails")));
			addViewState("viewDetails", "person.Detail.view", transition(on(submit()), to("bindAndValidateDetails")));
			addActionState("bindAndValidateDetails", noOpAction, new Transition[] {
					transition(on(error()), to("viewDetails")), transition(on(success()), to("finish")) });
			addEndState("finish");
		}
	};

	/**
	 * Action bean stub that does nothing, just returns a "success" result.
	 */
	public static final class NoOpAction implements Action {
		public Event execute(RequestContext context) throws Exception {
			return new Event(this, "success");
		}
	}
	
	public void testConfigureMultiAction() throws Exception {
		MultiAction multiAction = new MultiAction(new MultiActionTarget());
		AnnotatedAction action = builder.invoke("foo", multiAction);
		assertEquals("foo", action.getAttributeMap().get(AnnotatedAction.METHOD_ATTRIBUTE));
		assertEquals("success", action.execute(new MockRequestContext()).getId());
	}
	
	public static class MultiActionTarget {
		public Event foo(RequestContext context) {
			return new Event(this, "success");
		}
	}
}