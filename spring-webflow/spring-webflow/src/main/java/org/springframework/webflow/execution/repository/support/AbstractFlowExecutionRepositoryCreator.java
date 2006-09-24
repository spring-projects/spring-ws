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

import org.springframework.util.Assert;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;

/**
 * A convenient base for flow execution repository creational strategies. Simply
 * exposes a configuration interface for configuring the services needed by
 * repositories created by this creator.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionRepositoryCreator implements FlowExecutionRepositoryCreator {

	/**
	 * The holder for common services needed by all repositories created by this
	 * creator.
	 */
	private FlowExecutionRepositoryServices repositoryServices;

	/**
	 * Creates a new flow execution repository creator
	 * @param repositoryServices the holder for common services needed by all
	 * repositories created by this creator.
	 */
	public AbstractFlowExecutionRepositoryCreator(FlowExecutionRepositoryServices repositoryServices) {
		Assert.notNull(repositoryServices, "The repository services instance is required");
		this.repositoryServices = repositoryServices;
	}

	/**
	 * Returns the holder for common services needed by all repositories created
	 * by this creator.
	 */
	protected FlowExecutionRepositoryServices getRepositoryServices() {
		return repositoryServices;
	}

	public abstract FlowExecutionRepository createRepository();

	public abstract FlowExecutionRepository rehydrateRepository(FlowExecutionRepository repository);
}