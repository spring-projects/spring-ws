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

import org.springframework.webflow.execution.repository.FlowExecutionRepository;

/**
 * A creational strategy that encapsulates the construction, configuration, and
 * rehydration of a flow execution repository implementation. An explicit
 * factory is used as a repository implementation can be a feature-rich object
 * with many tweakable settings, and may also be serialized out between
 * requests.
 * @author Keith Donald
 */
public interface FlowExecutionRepositoryCreator {

	/**
	 * Creates a new flow execution repository. The instance returned is always
	 * a prototype, a new instance is expeted to be created on each invocation.
	 * @return the fully constructed flow execution repository
	 */
	public FlowExecutionRepository createRepository();

	/**
	 * Rehydrate this flow execution repository, restoring any transient
	 * references that may be null as a result of the repository being
	 * deserialized. May not apply to all repository implementations.
	 * @param repository the potentially deserialized repository
	 * @return the rehydrated repository
	 */
	public FlowExecutionRepository rehydrateRepository(FlowExecutionRepository repository);
}