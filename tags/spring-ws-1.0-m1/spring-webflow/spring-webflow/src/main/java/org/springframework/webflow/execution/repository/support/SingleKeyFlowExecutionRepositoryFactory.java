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

import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.FlowExecutionKey;

/**
 * Convenient implementation that encapsulates the assembly of a default flow
 * execution repository factory that creates repositories that generate one
 * {@link FlowExecutionKey} per persistent FlowExecution. The assigned key
 * remains the same through the life of the FlowExecution.
 * 
 * Useful to support so called "conversation-redirects", where after each POST a
 * redirect occurs to a flow execution URL. Because the flow execution maintains
 * the same key throughout its life the URL never changes for the duration of
 * the interaction.
 * 
 * @author Keith Donald
 */
public class SingleKeyFlowExecutionRepositoryFactory extends DefaultFlowExecutionRepositoryFactory {

	/**
	 * Creates a new simple flow execution repository factory.
	 * @param flowLocator the locator for loading flow definitions for which
	 * flow executions are created from
	 */
	public SingleKeyFlowExecutionRepositoryFactory(FlowLocator flowLocator) {
		super(flowLocator);
		setAlwaysGenerateNewNextKey(false);
	}
}