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
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;

/**
 * Returns the same (singleton) instance of a {@link FlowExecutionRepository} on
 * each invocation.
 * <p>
 * Designed to be used with {@link FlowExecutionRepository} implementations that
 * are stateless and therefore safely shareable by all threads.
 * 
 * @author Keith Donald
 */
public class SingletonFlowExecutionRepositoryFactory implements FlowExecutionRepositoryFactory {

	/**
	 * The singleton repository.
	 */
	private FlowExecutionRepository repository;

	/**
	 * Creates a new singleton flow execution repository that simply returns the
	 * repository provided everytime.
	 * @param repository the singleton repository
	 */
	public SingletonFlowExecutionRepositoryFactory(FlowExecutionRepository repository) {
		Assert.notNull(repository, "The singleton repository is required");
		this.repository = repository;
	}


	public FlowExecutionRepository getRepository() {
		return repository;
	}

	public FlowExecutionRepository getRepository(ExternalContext context) {
		return repository;
	}
}