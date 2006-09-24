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
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.execution.repository.conversation.ConversationService;
import org.springframework.webflow.execution.repository.conversation.impl.LocalConversationService;
import org.springframework.webflow.util.RandomGuidUidGenerator;
import org.springframework.webflow.util.UidGenerator;

/**
 * Stores <i>exactly one</i> flow execution per conversation, where each
 * continuation represents the current state of an active conversation.
 * <p>
 * It is important to note use of this repository <b>does not</b> allow for
 * duplicate submission in conjunction with browser navigational buttons (such
 * as the back button). Specifically, if you attempt to "go back" and resubmit,
 * the continuation id stored on the page in your browser history will <b>not</b>
 * match the continuation id of the {@link FlowExecutionEntry} object and access
 * to the conversation will be disallowed. This is because the
 * <code>continuationId</code> changes on each request to consistently prevent
 * the possibility of duplicate submission.
 * <p>
 * This repository is specifically designed to be 'simple': incurring minimal
 * resources and overhead, as only one {@link FlowExecution} is stored <i>per
 * user conversation</i>. This repository implementation should only be used
 * when you do not have to support browser navigational button use, e.g. you
 * lock down the browser and require that all navigational events to be routed
 * explicitly through Spring Web Flow.
 * 
 * @author Keith Donald
 */
public class DefaultFlowExecutionRepository extends AbstractConversationFlowExecutionRepository implements Serializable {

	/**
	 * The flow execution entry attribute.
	 */
	private static final String FLOW_EXECUTION_ENTRY_ATTRIBUTE = "flowExecutionEntry";

	/**
	 * The uid generation strategy to use.
	 */
	private transient UidGenerator continuationIdGenerator = new RandomGuidUidGenerator();

	/**
	 * Creates a new continuation flow execution repository.
	 * @param repositoryServices the repository services holder
	 */
	public DefaultFlowExecutionRepository(FlowExecutionRepositoryServices repositoryServices) {
		super(repositoryServices, new LocalConversationService());
	}

	/**
	 * Creates a new continuation flow execution repository.
	 * @param repositoryServices the repository services holder
	 */
	public DefaultFlowExecutionRepository(FlowExecutionRepositoryServices repositoryServices,
			ConversationService conversationService) {
		super(repositoryServices, conversationService);
	}

	/**
	 * Returns the uid generation strategy used to generate continuation
	 * identifiers.
	 */
	public UidGenerator getContinuationIdGenerator() {
		return continuationIdGenerator;
	}

	/**
	 * Sets the uid generation strategy used to generate unique continuation
	 * identifiers for {@link FlowExecutionKey flow execution keys}.
	 */
	public void setContinuationIdGenerator(UidGenerator continuationIdGenerator) {
		Assert.notNull(continuationIdGenerator, "The continuation id generator is required");
		this.continuationIdGenerator = continuationIdGenerator;
	}

	public FlowExecution getFlowExecution(FlowExecutionKey key) {
		FlowExecution flowExecution = accessFlowExecution(key);
		return rehydrate(flowExecution, key);
	}

	public void putFlowExecution(FlowExecutionKey key, FlowExecution flowExecution) {
		FlowExecutionEntry entry = new FlowExecutionEntry(getContinuationId(key), flowExecution);
		putEntry(key, entry);
		putConversationScope(key, asImpl(flowExecution).getConversationScope());
	}

	protected Serializable generateContinuationId(FlowExecution flowExecution) {
		return continuationIdGenerator.generateUid();
	}

	protected Serializable parseContinuationId(String encodedId) {
		return continuationIdGenerator.parseUid(encodedId);
	}

	private FlowExecutionEntry getEntry(FlowExecutionKey key) {
		FlowExecutionEntry entry = (FlowExecutionEntry)getConversation(key)
				.getAttribute(FLOW_EXECUTION_ENTRY_ATTRIBUTE);
		if (entry == null) {
			throw new NoSuchFlowExecutionException(key, new IllegalStateException("No '"
					+ FLOW_EXECUTION_ENTRY_ATTRIBUTE + "' attribute present in conversation scope: "
					+ "possible programmer error--do not call get before calling put"));
		}
		return entry;
	}

	private FlowExecution accessFlowExecution(FlowExecutionKey key) {
		try {
			return getEntry(key).access(getContinuationId(key));
		}
		catch (InvalidContinuationIdException e) {
			throw new NoSuchFlowExecutionException(key, e);
		}
	}

	private void putEntry(FlowExecutionKey key, FlowExecutionEntry entry) {
		getConversation(key).putAttribute(FLOW_EXECUTION_ENTRY_ATTRIBUTE, entry);
	}

	/**
	 * Simple holder for a flow execution. In order to access the held flow
	 * execution you must present a valid continuationId.
	 * 
	 * @author Keith Donald
	 */
	private static class FlowExecutionEntry implements Serializable {
		
		/**
		 * The id required to access the execution.
		 */
		private Serializable continuationId;

		/**
		 * The flow execution.
		 */
		private FlowExecution flowExecution;

		/**
		 * Creates a new flow execution entry
		 * @param continuationId the continuation id
		 * @param flowExecution the flow execution
		 */
		public FlowExecutionEntry(Serializable continuationId, FlowExecution flowExecution) {
			this.continuationId = continuationId;
			this.flowExecution = flowExecution;
		}

		public FlowExecution access(Serializable continuationId) throws InvalidContinuationIdException {
			if (!this.continuationId.equals(continuationId)) {
				throw new InvalidContinuationIdException(continuationId);
			}
			return flowExecution;
		}
	}
}