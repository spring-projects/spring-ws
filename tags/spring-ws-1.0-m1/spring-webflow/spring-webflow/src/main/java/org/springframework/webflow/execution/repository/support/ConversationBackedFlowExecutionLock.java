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

import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.conversation.Conversation;
import org.springframework.webflow.execution.repository.conversation.ConversationService;
import org.springframework.webflow.execution.repository.conversation.NoSuchConversationException;

/**
 * A flow execution lock that locks a conversation managed by a
 * {@link ConversationService}.
 * 
 * This implementation ensures multiple threads cannot manipulate the same
 * conversation at the same time. The locked conversation is the sole gateway to
 * a flow execution, and a lock on it prevents access to any associated
 * execution.
 * 
 * @author Keith Donald
 */
class ConversationBackedFlowExecutionLock implements FlowExecutionLock {

	/**
	 * The conversation associated to lock, associated with the
	 * FlowExecutionKey.
	 */
	private Conversation conversation;

	/**
	 * Creates a new conversation-backed execution lock.
	 * @param conversation the conversation
	 */
	public ConversationBackedFlowExecutionLock(Conversation conversation) {
		this.conversation = conversation;
	}

	public void lock() {
		conversation.lock();
	}

	public void unlock() {
		try {
			conversation.unlock();
		}
		catch (NoSuchConversationException e) {
			// ignore
		}
	}
}