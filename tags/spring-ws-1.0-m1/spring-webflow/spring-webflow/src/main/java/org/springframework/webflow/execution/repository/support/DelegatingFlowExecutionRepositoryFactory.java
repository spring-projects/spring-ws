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

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;

/**
 * A base for decorators that encapsulate the construction and configuration of
 * a custom flow execution repository factory delegate. The delegate is invoked
 * at runtime in standard decorator fashion.
 * <p>
 * Also exposes a convenient configuration interface for clients to configure
 * common {@link FlowExecutionRepositoryServices repository services} directly,
 * allowing for easy customization over the behavior of repositories created by
 * the delegate factory.
 * 
 * @author Keith Donald
 */
public abstract class DelegatingFlowExecutionRepositoryFactory extends FlowExecutionRepositoryServices implements
		FlowExecutionRepositoryFactory {

	/**
	 * The repository to delegate to.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory;

	/**
	 * Creates a new delegating flow execution repository factory.
	 * @param flowLocator the low locator service to be used by repositories
	 * created by this factory
	 */
	protected DelegatingFlowExecutionRepositoryFactory(FlowLocator flowLocator) {
		super(flowLocator);
	}

	/**
	 * Returns the wrapped repository factory delegate.
	 */
	protected FlowExecutionRepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	/**
	 * Called by superclasses to set the configured repository factory delegate
	 * after construction.
	 */
	protected void setRepositoryFactory(FlowExecutionRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

	/*
	 * Simply delegates to the wrapped repository factory.
	 * @see org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory#getRepository(org.springframework.webflow.ExternalContext)
	 */
	public FlowExecutionRepository getRepository(ExternalContext context) {
		return repositoryFactory.getRepository(context);
	}
}