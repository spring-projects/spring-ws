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
package org.springframework.webflow.executor;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.support.FlowExecutionRedirect;
import org.springframework.webflow.support.FlowRedirect;

/**
 * Immutable value object that provides clients with information about a
 * response to issue.
 * <p>
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ResponseInstruction implements Serializable {

	/**
	 * The persistent identifier of the flow execution.
	 */
	private String flowExecutionKey;

	/**
	 * The view selection that was made.
	 */
	private ViewSelection viewSelection;

	/**
	 * A state of the flow execution.
	 */
	private transient FlowExecutionContext flowExecutionContext;

	/**
	 * Create a new response instruction for a paused flow execution.
	 * @param flowExecutionKey the persistent identifier of the flow execution
	 * @param flowExecutionContext the current flow execution context
	 * @param viewSelection the selected view
	 */
	public ResponseInstruction(String flowExecutionKey, FlowExecutionContext flowExecutionContext,
			ViewSelection viewSelection) {
		Assert.notNull(flowExecutionKey, "The flow execution key is required");
		this.flowExecutionKey = flowExecutionKey;
		init(flowExecutionContext, viewSelection);
	}

	/**
	 * Create a new response instruction for an ended flow execution.
	 * @param flowExecutionContext the current flow execution context
	 * @param viewSelection the selected view
	 */
	public ResponseInstruction(FlowExecutionContext flowExecutionContext, ViewSelection viewSelection) {
		init(flowExecutionContext, viewSelection);
	}

	private void init(FlowExecutionContext flowExecutionContext, ViewSelection viewSelection) {
		Assert.notNull(flowExecutionContext, "The flow execution context is required");
		Assert.notNull(viewSelection, "The view selection is required");
		this.flowExecutionContext = flowExecutionContext;
		this.viewSelection = viewSelection;
	}

	/**
	 * Returns the persistent identifier of the flow execution.
	 */
	public String getFlowExecutionKey() {
		return flowExecutionKey;
	}

	/**
	 * Returns the flow execution context representing the current state of the
	 * execution.
	 */
	public FlowExecutionContext getFlowExecutionContext() {
		return flowExecutionContext;
	}

	/**
	 * Returns the view selection selected by the flow executino.
	 */
	public ViewSelection getViewSelection() {
		return viewSelection;
	}

	/**
	 * Returns true if this is a "null" response instruction.
	 */
	public boolean isNull() {
		return viewSelection == ViewSelection.NULL_VIEW;
	}

	/**
	 * Returns true if this is an "application view" (forward) response
	 * instruction.
	 */
	public boolean isApplicationView() {
		return viewSelection instanceof ApplicationView;
	}

	/**
	 * Returns true if this is an instruction to render an application view for
	 * a "active" (in progress) flow execution.
	 */
	public boolean isActiveView() {
		return isApplicationView() && flowExecutionContext.isActive();
	}

	/**
	 * Returns true if this is an instruction to render a confirmation view for
	 * a "ended" (inactive) flow execution.
	 */
	public boolean isConfirmationView() {
		return isApplicationView() && !flowExecutionContext.isActive();
	}

	/**
	 * Returns true if this is an instruction to perform a redirect to the
	 * current flow execution.
	 */
	public boolean isFlowExecutionRedirect() {
		return viewSelection instanceof FlowExecutionRedirect;
	}

	/**
	 * Returns true if this an instruction to perform a redirect to an external
	 * URL.
	 */
	public boolean isExternalRedirect() {
		return viewSelection instanceof ExternalRedirect;
	}

	/**
	 * Returns true if this is an instruction to launch an entirely new
	 * (independent) flow execution.
	 */
	public boolean isFlowRedirect() {
		return viewSelection instanceof FlowRedirect;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ResponseInstruction)) {
			return false;
		}
		ResponseInstruction other = (ResponseInstruction)o;
		if (getFlowExecutionKey() != null) {
			return getFlowExecutionKey().equals(other.getFlowExecutionKey())
					&& viewSelection.equals(other.viewSelection);
		}
		else {
			return other.getFlowExecutionKey() == null && viewSelection.equals(other.viewSelection);
		}
	}

	public int hashCode() {
		int hashCode = viewSelection.hashCode();
		if (getFlowExecutionKey() != null) {
			hashCode += getFlowExecutionKey().hashCode();
		}
		return hashCode;
	}

	public String toString() {
		return new ToStringCreator(this).append("flowExecutionKey", flowExecutionKey).append("viewSelection",
				viewSelection).append("flowExecutionContext", flowExecutionContext).toString();
	}
}