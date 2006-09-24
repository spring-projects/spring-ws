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
package org.springframework.webflow.execution.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.FlowSessionStatus;
import org.springframework.webflow.State;
import org.springframework.webflow.execution.FlowLocator;

/**
 * Implementation of the FlowSession interfaced used internally by the
 * <code>FlowExecutionImpl</code>. This class is closely coupled with
 * <code>FlowExecutionImpl</code> and <code>FlowControlContextImpl</code>.
 * The three classes work together to form a complete flow execution
 * implementation.
 * 
 * @see org.springframework.webflow.execution.impl.FlowExecutionImpl
 * @see org.springframework.webflow.execution.impl.FlowExecutionControlContextImpl
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
class FlowSessionImpl implements FlowSession, Externalizable {

	private static final Log logger = LogFactory.getLog(FlowSessionImpl.class);

	/**
	 * The flow definition (a singleton).
	 */
	private transient Flow flow;

	/**
	 * Set only on deserialization so this object can be fully reconstructed.
	 */
	private String flowId;

	/**
	 * The current state of this flow session.
	 */
	private transient State state;

	/**
	 * Set only on deserialization so this object can be fully reconstructed.
	 */
	private String stateId;

	/**
	 * The session status; may be CREATED, STARTING, ACTIVE, PAUSED, SUSPENDED,
	 * or ENDED.
	 */
	private FlowSessionStatus status = FlowSessionStatus.CREATED;

	/**
	 * The session data model ("flow scope").
	 */
	private AttributeMap scope = new AttributeMap();

	/**
	 * The parent session of this session (may be null if this is a root
	 * session.)
	 */
	private FlowSessionImpl parent;

	/**
	 * Default constructor required for externalizable serialization. Should NOT
	 * be called programmatically.
	 */
	public FlowSessionImpl() {

	}

	/**
	 * Create a new flow session.
	 * @param flow the flow associated with this flow session
	 * @param input the input parameters used to populate the flow session
	 * @param parent the parent flow session of the created flow session in the
	 * owning flow execution
	 */
	public FlowSessionImpl(Flow flow, FlowSessionImpl parent) {
		Assert.notNull(flow, "The flow is required");
		this.flow = flow;
		this.parent = parent;
	}

	public Flow getFlow() {
		return flow;
	}

	public State getState() {
		return state;
	}

	/**
	 * Set the current state of this flow session.
	 * @param state the state that is currently active in this flow session
	 */
	public void setState(State state) {
		Assert.notNull(state, "The state is required");
		Assert.isTrue(flow == state.getFlow(),
				"The state does not belong to the flow associated with this flow session");
		if (logger.isDebugEnabled()) {
			logger.debug("Setting current state of '" + getFlow().getId() + "@"
					+ ObjectUtils.getIdentityHexString(this) + "' to '" + state.getId() + "'");
		}
		this.state = state;
	}

	public FlowSessionStatus getStatus() {
		return status;
	}

	/**
	 * Set the status of this flow session.
	 * @param status the new status to set
	 */
	protected void setStatus(FlowSessionStatus status) {
		Assert.notNull(status);
		this.status = status;
	}

	public AttributeMap getScope() {
		return scope;
	}

	public FlowSession getParent() {
		return parent;
	}

	public boolean isRoot() {
		return parent == null;
	}

	// custom serialization

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		flowId = (String)in.readObject();
		stateId = (String)in.readObject();
		status = (FlowSessionStatus)in.readObject();
		scope = (AttributeMap)in.readObject();
		parent = (FlowSessionImpl)in.readObject();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		if (flow != null) {
			out.writeObject(flow.getId());
		}
		else {
			out.writeObject(flowId);
		}
		if (state != null) {
			out.writeObject(state.getId());
		}
		else {
			out.writeObject(stateId);
		}
		out.writeObject(status);
		out.writeObject(scope);
		out.writeObject(parent);
	}

	/**
	 * Restore this <code>FlowSession</code> for use after deserialization.
	 * @param flowLocator the flow locator
	 */
	protected void rehydrate(FlowLocator flowLocator) {
		// implementation note: we cannot integrate this code into the
		// readObject() method since we need the flow locator!
		Assert.state(flow == null, "The flow is already set -- already restored");
		Assert.state(state == null, "The state is already set -- already restored");
		Assert.notNull(flowId, "The flow id was not set during deserialization: "
				+ "cannot restore; was this flow session deserialized properly?");
		flow = flowLocator.getFlow(flowId);
		flowId = null;
		Assert.notNull(stateId, "The current state id was not set during deserialization: "
				+ "cannot restore; was this flow session deserialized properly?");
		state = flow.getRequiredState(stateId);
		stateId = null;
	}

	public String toString() {
		return new ToStringCreator(this).append("flow", flow.getId()).append("state",
				(state != null ? state.getId() : "[none]")).append("scope", scope).toString();
	}
}