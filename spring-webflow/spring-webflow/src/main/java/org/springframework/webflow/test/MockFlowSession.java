/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.test;

import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.FlowSessionStatus;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.State;
import org.springframework.webflow.ViewState;

/**
 * Mock implementation of the <code>FlowSession</code> interface.
 * 
 * @author Erwin Vervaet
 */
public class MockFlowSession implements FlowSession {

	private Flow flow;

	private State state;

	private FlowSessionStatus status = FlowSessionStatus.CREATED;

	private AttributeMap scope = new AttributeMap();

	private FlowSession parent;

	/**
	 * Creates a new mock flow session that sets a flow with id "mockFlow" as
	 * the 'active flow' in state "mockState". This session marks itself active.
	 */
	public MockFlowSession() {
		setFlow(new Flow("mockFlow"));
		State state = new ViewState(flow, "mockState");
		setStatus(FlowSessionStatus.ACTIVE);
		setState(state);
	}

	/**
	 * Creates a new mock session in a created state for the specified flow
	 * definition.
	 */
	public MockFlowSession(Flow flow) {
		setFlow(flow);
	}

	/**
	 * Creates a new mock session in a created state for the specified flow
	 * definition.
	 */
	public MockFlowSession(Flow flow, AttributeMap input) {
		setFlow(flow);
		scope.putAll(input);
	}

	public Flow getFlow() {
		return flow;
	}

	public State getState() {
		return state;
	}

	public AttributeMap getScope() {
		return scope;
	}

	public FlowSessionStatus getStatus() {
		return status;
	}

	public FlowSession getParent() {
		return parent;
	}

	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * Set the flow associated with this flow session.
	 */
	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	/**
	 * Set the currently active state.
	 */
	public void setState(State state) {
		this.state = state;
	}

	/**
	 * Set the status of this flow session.
	 */
	public void setStatus(FlowSessionStatus status) {
		this.status = status;
	}

	/**
	 * Set the scope data maintained by this flow session. This will be the flow
	 * scope data of the ongoing flow execution. As such, the given scope should
	 * be of type {@link ScopeType#FLOW}.
	 */
	public void setScope(AttributeMap scope) {
		this.scope = scope;
	}

	/**
	 * Set the parent flow session of this flow session in the ongoing flow
	 * execution.
	 */
	public void setParent(FlowSession parent) {
		this.parent = parent;
	}
}