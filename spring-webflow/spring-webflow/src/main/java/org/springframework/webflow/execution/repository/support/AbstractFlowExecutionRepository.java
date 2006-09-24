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
package org.springframework.webflow.execution.repository.support;

import java.io.Serializable;

import org.springframework.util.Assert;
import org.springframework.webflow.Flow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;

/**
 * A convenient base for flow execution repository implementations.
 * <p>
 * Exposes a configuration interface for setting the set of services common to
 * most repository implementations. Also provides some basic implementation
 * assistance.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionRepository implements FlowExecutionRepository, Serializable {

	/**
	 * A holder for the services needed by this repository.
	 */
	private transient FlowExecutionRepositoryServices repositoryServices;

	/**
	 * Flag to indicate whether or not a new flow execution key should always be
	 * generated before each put call.  Default is true.
	 */
	private boolean alwaysGenerateNewNextKey = true;

	/**
	 * No-arg constructor to satisfy use with subclass implementations are that
	 * serializable.
	 */
	protected AbstractFlowExecutionRepository() {

	}

	/**
	 * Creates a new flow execution repository
	 * @param repositoryServices the common services needed by this repository
	 * to function.
	 */
	public AbstractFlowExecutionRepository(FlowExecutionRepositoryServices repositoryServices) {
		setRepositoryServices(repositoryServices);
	}

	/**
	 * Returns the holder for accessing common services needed by this
	 * repository.
	 */
	protected FlowExecutionRepositoryServices getRepositoryServices() {
		return repositoryServices;
	}

	/**
	 * Sets the holder for accessing common services needed by this repository.
	 */
	public void setRepositoryServices(FlowExecutionRepositoryServices repositoryServices) {
		Assert.notNull(repositoryServices, "The repository services instance is required");
		this.repositoryServices = repositoryServices;
	}

	/**
	 * Returns the configured generate new next key flag.
	 */
	protected boolean isAlwaysGenerateNewNextKey() {
		return alwaysGenerateNewNextKey;
	}

	/**
	 * Sets a flag indicating if a new {@link FlowExecutionKey} should always be
	 * generated before each put call. By setting this to false a FlowExecution
	 * can remain identified by the same key throughout its life.
	 * @param alwaysGenerateNewNextKey the generate flag
	 */
	public void setAlwaysGenerateNewNextKey(boolean alwaysGenerateNewNextKey) {
		this.alwaysGenerateNewNextKey = alwaysGenerateNewNextKey;
	}

	public FlowExecution createFlowExecution(String flowId) {
		Flow flow = repositoryServices.getFlowLocator().getFlow(flowId);
		return new FlowExecutionImpl(flow, repositoryServices.getListenerLoader().getListeners(flow));
	}

	public abstract FlowExecutionKey generateKey(FlowExecution flowExecution) throws FlowExecutionRepositoryException;

	public abstract FlowExecutionKey parseFlowExecutionKey(String encodedKey);

	public abstract FlowExecution getFlowExecution(FlowExecutionKey key) throws FlowExecutionRepositoryException;

	public abstract FlowExecutionLock getLock(FlowExecutionKey key) throws FlowExecutionRepositoryException;

	public abstract FlowExecutionKey getNextKey(FlowExecution flowExecution, FlowExecutionKey previousKey) throws FlowExecutionRepositoryException;

	public abstract void putFlowExecution(FlowExecutionKey key, FlowExecution flowExecution) throws FlowExecutionRepositoryException;

	public abstract void removeFlowExecution(FlowExecutionKey key) throws FlowExecutionRepositoryException;

	protected FlowExecution rehydrate(FlowExecution flowExecution, FlowExecutionKey key) {
		FlowExecutionImpl impl = asImpl(flowExecution);
		impl.rehydrate(repositoryServices.getFlowLocator(), repositoryServices.getListenerLoader());
		return flowExecution;
	}

	protected FlowExecutionImpl asImpl(FlowExecution flowExecution) {
		return (FlowExecutionImpl)flowExecution;
	}
}