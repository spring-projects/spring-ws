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

import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.Action;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.support.DefaultFlowAttributeMapper;

/**
 * Test case for XML flow builder, testing pluggability of custom types.
 * 
 * @see org.springframework.webflow.builder.XmlFlowBuilder
 * 
 * @author Erwin Vervaet
 */
public class XmlFlowBuilderCustomTypeTests extends TestCase {

	private Flow flow;

	protected void setUp() throws Exception {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("testFlow3.xml",
				XmlFlowBuilderCustomTypeTests.class), new CustomFlowArtifactFactory());
		FlowAssembler assembler = new FlowAssembler("testFlow3", builder);
		assembler.assembleFlow();
		flow = builder.getFlow();
	}

	public void testBuildResult() {
		assertEquals("testFlow3", flow.getId());
		assertEquals(5, flow.getStateCount());
		assertEquals(1, flow.getExceptionHandlerSet().size());
		assertSame(((ActionState)flow.getState("actionState1")).getActionList().getAnnotated(0).getTargetAction()
				.getClass(), CustomAction.class);
		assertSame(((SubflowState)flow.getState("subFlowState1")).getAttributeMapper().getClass(),
				CustomAttributeMapper.class);
		assertSame(flow.getExceptionHandlerSet().toArray()[0].getClass(), CustomExceptionHandler.class);
	}

	public static class CustomAction extends AbstractAction {
		protected Event doExecute(RequestContext context) throws Exception {
			return success();
		}
	}

	public static class CustomAttributeMapper extends DefaultFlowAttributeMapper {
	}

	public static class CustomExceptionHandler implements StateExceptionHandler {
		public boolean handles(StateException exception) {
			return false;
		}

		public ViewSelection handle(StateException exception, FlowExecutionControlContext context) {
			return null;
		}
	}

	public static class CustomFlowArtifactFactory extends BaseFlowServiceLocator {

		public Action getAction(String id) throws FlowArtifactException {
			return new CustomAction();
		}

		public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
			return new CustomAttributeMapper();
		}

		public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException {
			return new CustomExceptionHandler();
		}

	}

}