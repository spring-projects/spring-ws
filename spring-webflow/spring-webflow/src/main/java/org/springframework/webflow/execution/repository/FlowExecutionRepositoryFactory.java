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
package org.springframework.webflow.execution.repository;

import org.springframework.webflow.ExternalContext;

/**
 * An abstract factory for obtaining a reference to a flow execution repository
 * that may be (or have its contents) managed in an external data structure.
 * 
 * @author Keith Donald
 */
public interface FlowExecutionRepositoryFactory {

	/**
	 * Lookup the repository given the external context.
	 * @param context the external context, which may be used to access the
	 * repository from an externally managed, shared in-memory map
	 * @return the retrived flow execution repository
	 */
	public FlowExecutionRepository getRepository(ExternalContext context);
}