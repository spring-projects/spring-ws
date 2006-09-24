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
package org.springframework.webflow.execution.repository.conversation;

/**
 * A service for managing conversations. This interface is the entry point into
 * the conversation subsystem.
 * 
 * @author Keith Donald
 */
public interface ConversationService {

	/**
	 * Begin a new conversation.
	 * @param conversationParameters the input needed
	 * @return a service interface allowing access to the conversatio context
	 * @throws ConversationServiceException an exception occured
	 */
	public Conversation beginConversation(ConversationParameters conversationParameters) throws ConversationServiceException;

	/**
	 * Get the conversation with the provided id.
	 * @param id the conversation id
	 * @return the conversation
	 * @throws NoSuchConversationException the id provided was invalid.
	 */
	public Conversation getConversation(ConversationId id) throws NoSuchConversationException;

	/**
	 * Parse the string-encoded conversationId into its object form.
	 * Essentially, the reverse of {@link ConversationId#toString()}.
	 * @param encodedId the encoded id
	 * @return the parsed conversation id
	 * @throws ConversationServiceException an exception occured.
	 */
	public ConversationId parseConversationId(String encodedId) throws ConversationServiceException;
}
