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
package org.springframework.webflow.execution.repository.continuation;

import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.conversation.ConversationService;
import org.springframework.webflow.execution.repository.support.DelegatingFlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.support.FlowExecutionRepositoryServices;
import org.springframework.webflow.execution.repository.support.SingletonFlowExecutionRepositoryFactory;

/**
 * A convenient implementation that encapsulates the assembly of a "client" flow
 * execution repository factory and delegates to it at runtme. The delegate
 * factory creates repositories that persist flow executions client-side,
 * requiring no server-side state.
 * <p>
 * Internally, sets a {@link SingletonFlowExecutionRepositoryFactory} configured
 * with a single, stateless
 * {@link ClientContinuationFlowExecutionRepository client continuation-based flow execution repository}
 * implementation.
 * <p>
 * This class inherits from {@link FlowExecutionRepositoryServices} to allow for
 * direct configuration of services needed by the repositories created by this
 * factory.
 * 
 * @see ClientContinuationFlowExecutionRepository
 * 
 * @author Keith Donald
 */
public class ClientContinuationFlowExecutionRepositoryFactory extends DelegatingFlowExecutionRepositoryFactory {

	/**
	 * Creates a new client flow execution repository factory.
	 * @param flowLocator the locator for loading flow definitions for which
	 * flow executions are created from
	 */
	public ClientContinuationFlowExecutionRepositoryFactory(FlowLocator flowLocator) {
		super(flowLocator);
		setRepositoryFactory(new SingletonFlowExecutionRepositoryFactory(new ClientContinuationFlowExecutionRepository(
				this)));
	}

	/**
	 * Sets the conversation service reference.
	 * @param conversationService the conversation service, may not be null.
	 */
	public void setConversationService(ConversationService conversationService) {
		getRepository().setConversationService(conversationService);
	}

	/**
	 * Sets the continuation factory that encapsulates the construction of
	 * continuations stored in this repository.
	 */
	public void setContinuationFactory(FlowExecutionContinuationFactory continuationFactory) {
		getRepository().setContinuationFactory(continuationFactory);
	}

	private ClientContinuationFlowExecutionRepository getRepository() {
		return (ClientContinuationFlowExecutionRepository)((SingletonFlowExecutionRepositoryFactory)getRepositoryFactory())
				.getRepository();
	}
}