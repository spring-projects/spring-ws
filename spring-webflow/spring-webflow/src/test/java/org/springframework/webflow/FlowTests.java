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
package org.springframework.webflow;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.binding.mapping.DefaultAttributeMapper;
import org.springframework.binding.mapping.MappingBuilder;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.webflow.action.TestMultiAction;
import org.springframework.webflow.builder.MyCustomException;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ApplicationViewSelector;
import org.springframework.webflow.support.BeanFactoryFlowVariable;
import org.springframework.webflow.support.DefaultExpressionParserFactory;
import org.springframework.webflow.support.DefaultTargetStateResolver;
import org.springframework.webflow.support.EventIdTransitionCriteria;
import org.springframework.webflow.support.SimpleFlowVariable;
import org.springframework.webflow.support.TransitionExecutingStateExceptionHandler;
import org.springframework.webflow.test.MockFlowExecutionControlContext;

/**
 * Unit test for the Flow class.
 * 
 * @author Keith Donald
 */
public class FlowTests extends TestCase {

	private Flow flow = createSimpleFlow();

	private Flow createSimpleFlow() {
		flow = new Flow("myFlow");
		ViewState state1 = new ViewState(flow, "myState1");
		state1.setViewSelector(new ApplicationViewSelector(new StaticExpression("myView")));
		state1.getTransitionSet().add(new Transition(on("submit"), to("myState2")));
		EndState state2 = new EndState(flow, "myState2");
		state2.setViewSelector(new ApplicationViewSelector(new StaticExpression("myView2")));
		flow.getGlobalTransitionSet().add(new Transition(on("globalEvent"), to("myState2")));
		return flow;
	}

	public void testAddStates() {
		Flow flow = new Flow("myFlow");
		new EndState(flow, "myState1");
		new EndState(flow, "myState2");
		assertEquals("Wrong start state:", "myState1", flow.getStartState().getId());
		assertEquals("State count wrong:", 2, flow.getStateCount());
		assertEquals("State count wrong:", 2, flow.getStates().length);
		assertTrue(flow.containsState("myState1"));
		assertTrue(flow.containsState("myState2"));
		State state = flow.getRequiredState("myState1");
		assertEquals("Wrong flow:", "myFlow", state.getFlow().getId());
		assertEquals("Wrong state:", "myState1", flow.getRequiredState("myState1").getId());
		assertEquals("Wrong state:", "myState2", flow.getState("myState2").getId());
	}

	public void testAddDuplicateState() {
		Flow flow = new Flow("myFlow");
		new EndState(flow, "myState1");
		try {
			new EndState(flow, "myState1");
			fail("Duplicate state added");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testAddSameStateTwice() {
		Flow flow = new Flow("myFlow");
		EndState state = new EndState(flow, "myState1");
		try {
			flow.add(state);
			fail("Should have failed");
		} catch (IllegalArgumentException e) {
			
		}
		assertEquals("State count wrong:", 1, flow.getStateCount());
	}

	public void testAddStateAlreadyInOtherFlow() {
		Flow otherFlow = new Flow("myOtherFlow");
		State state = new EndState(otherFlow, "myState1");
		Flow flow = new Flow("myFlow");
		try {
			flow.add(state);
			fail("Added state part of another flow");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testGetStateNoStartState() {
		Flow flow = new Flow("myFlow");
		try {
			flow.getStartState();
			fail("Retrieved start state when no such state");
		}
		catch (IllegalStateException e) {
			// expected
		}
	}

	public void testGetStateNoSuchState() {
		assertNull("Not null", flow.getState("myState3"));
		try {
			flow.getRequiredState("myState3");
			fail("Returned a state that doesn't exist");
		}
		catch (NoSuchStateException e) {
			// expected
		}
	}

	public void testGetTransitionableState() {
		assertEquals("Wrong state:", "myState1", flow.getTransitionableState("myState1").getId());
		assertEquals("Wrong state:", "myState1", flow.getRequiredTransitionableState("myState1").getId());
	}

	public void testGetStateNoSuchTransitionableState() {
		try {
			flow.getRequiredTransitionableState("myState2");
			fail("End states aren't transtionable");
		}
		catch (IllegalStateException e) {
			// expected
		}
		try {
			flow.getRequiredTransitionableState("doesNotExist");
		}
		catch (NoSuchStateException e) {
			// expected
		}
	}

	public void testAddActions() {
		flow.getStartActionList().add(new TestMultiAction());
		flow.getStartActionList().add(new TestMultiAction());
		flow.getEndActionList().add(new TestMultiAction());
		assertEquals(2, flow.getStartActionList().size());
		assertEquals(1, flow.getEndActionList().size());
	}

	public void testAddInlineFlow() {
		Flow inline = new Flow("inline");
		flow.addInlineFlow(inline);
		assertSame(inline, flow.getInlineFlow("inline"));
		assertEquals(1, flow.getInlineFlowCount());
		String[] inlined = flow.getInlineFlowIds();
		assertEquals(1, inlined.length);
		assertSame(flow.getInlineFlows()[0], inline);
	}

	public void testAddGlobalTransition() {
		Transition t = new Transition(new DefaultTargetStateResolver("myState2"));
		flow.getGlobalTransitionSet().add(t);
		assertSame(t, flow.getGlobalTransitionSet().toArray()[1]);
	}

	public void testStart() {
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		flow.start(context, new AttributeMap());
		assertEquals("Wrong start state", "myState1", context.getCurrentState().getId());
	}

	public void testStartWithAction() {
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		TestAction action = new TestAction();
		flow.getStartActionList().add(action);
		flow.start(context, new AttributeMap());
		assertEquals("Wrong start state", "myState1", context.getCurrentState().getId());
		assertEquals(1, action.getExecutionCount());
	}

	public void testStartWithVariables() {
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		flow.addVariable(new SimpleFlowVariable("var1", ArrayList.class));
		StaticApplicationContext beanFactory = new StaticApplicationContext();
		beanFactory.registerPrototype("bean", ArrayList.class);
		flow.addVariable(new BeanFactoryFlowVariable("var2", "bean", beanFactory));
		flow.start(context, new AttributeMap());
		assertEquals(2, context.getFlowScope().size());
		context.getFlowScope().getRequired("var1", ArrayList.class);	
		context.getFlowScope().getRequired("var2", ArrayList.class);	
	}

	public void testStartWithMapper() {
		DefaultAttributeMapper attributeMapper = new DefaultAttributeMapper();
		MappingBuilder mapping = new MappingBuilder(new DefaultExpressionParserFactory().getExpressionParser());
		attributeMapper.addMapping(mapping.source("attr").target("flowScope.attr").value());
		flow.setInputMapper(attributeMapper);
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		AttributeMap sessionInput = new AttributeMap();
		sessionInput.put("attr", "foo");
		flow.start(context, sessionInput); 
		assertEquals("foo", context.getFlowScope().get("attr"));
	}

	public void testOnEventNullCurrentState() {
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		Event event = new Event(this, "foo");
		try {
			flow.onEvent(event, context);
		} catch (IllegalStateException e) {
			
		}
	}

	public void testOnEventInvalidCurrentState() {
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.setCurrentState(flow.getState("myState2"));
		Event event = new Event(this, "submit");
		context.setLastEvent(event);
		try {
			flow.onEvent(event, context);
		} catch (IllegalStateException e) {
			
		}
	}

	public void testOnEvent() {
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.setCurrentState(flow.getState("myState1"));
		Event event = new Event(this, "submit");
		context.setLastEvent(event);
		assertTrue(context.getFlowExecutionContext().isActive());
		flow.onEvent(event, context);
		assertTrue(!context.getFlowExecutionContext().isActive());
	}

	public void testOnEventGlobalTransition() {
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.setCurrentState(flow.getState("myState1"));
		Event event = new Event(this, "globalEvent");
		context.setLastEvent(event);
		assertTrue(context.getFlowExecutionContext().isActive());
		flow.onEvent(event, context);
		assertTrue(!context.getFlowExecutionContext().isActive());
	}

	public void testOnEventNoTransition() {
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.setCurrentState(flow.getState("myState1"));
		Event event = new Event(this, "bogus");
		context.setLastEvent(event);
		try {
			flow.onEvent(event, context);
		} catch (NoMatchingTransitionException e) {
			
		}
	}

	public void testEnd() {
		TestAction action = new TestAction();
		flow.getEndActionList().add(action);
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		AttributeMap sessionOutput = new AttributeMap();
		flow.end(context, sessionOutput);
		assertEquals(1, action.getExecutionCount());
	}
	
	public void testEndWithMapper() {
		DefaultAttributeMapper attributeMapper = new DefaultAttributeMapper();
		MappingBuilder mapping = new MappingBuilder(new DefaultExpressionParserFactory().getExpressionParser());
		attributeMapper.addMapping(mapping.source("flowScope.attr").target("attr").value());
		flow.setOutputMapper(attributeMapper);
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.getFlowScope().put("attr", "foo");
		AttributeMap sessionOutput = new AttributeMap();
		flow.end(context, sessionOutput); 
		assertEquals("foo", sessionOutput.get("attr"));
	}

	public void testHandleStateException() {
		flow.getExceptionHandlerSet().add(new TransitionExecutingStateExceptionHandler()
				.add(MyCustomException.class, "myState2"));
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.setCurrentState(flow.getRequiredState("myState1"));
		StateException e = new StateException(flow.getStartState(), "Oops!", new MyCustomException());
		ApplicationView selectedView = (ApplicationView)flow.handleException(e, context);
		assertFalse(context.getFlowExecutionContext().isActive());
		assertNotNull("Should not have been null", selectedView);
		assertEquals("Wrong selected view", "myView2", selectedView.getViewName());
	}

	public void testHandleStateExceptionNoMatch() {
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		StateException e = new StateException(flow.getStartState(), "Oops!", new MyCustomException());
		try {
			flow.handleException(e, context);
		}
		catch (StateException ex) {
			// expected
		}
	}

	public static TransitionCriteria on(String eventId) {
		return new EventIdTransitionCriteria(eventId);
	}
	
	public static TargetStateResolver to(String stateId) {
		return new DefaultTargetStateResolver(stateId);
	}
}