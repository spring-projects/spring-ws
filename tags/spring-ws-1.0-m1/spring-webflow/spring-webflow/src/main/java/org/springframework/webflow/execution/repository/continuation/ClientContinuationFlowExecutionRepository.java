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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.springframework.util.Assert;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.conversation.Conversation;
import org.springframework.webflow.execution.repository.conversation.ConversationId;
import org.springframework.webflow.execution.repository.conversation.ConversationParameters;
import org.springframework.webflow.execution.repository.conversation.ConversationService;
import org.springframework.webflow.execution.repository.conversation.ConversationServiceException;
import org.springframework.webflow.execution.repository.conversation.NoSuchConversationException;
import org.springframework.webflow.execution.repository.conversation.impl.SimpleConversationId;
import org.springframework.webflow.execution.repository.support.AbstractConversationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.FlowExecutionRepositoryServices;

/**
 * Stores flow execution state client side, requiring no use of server-side
 * state.
 * <p>
 * More specifically, instead of putting {@link FlowExecution} objects in a
 * server-side store this repository <i>encodes</i> them directly into the
 * <code>continuationId</code> of a generated
 * {@link CompositeFlowExecutionKey}. When asked to load a flow execution by
 * its key, this repository decodes the serialized <code>continuationId</code>,
 * restoring the {@link FlowExecution} object at the state it was when encoded.
 * <p>
 * Note: currently this repository implementation does not by default support
 * <i>conversation invalidation after completion</i>, which enables automatic
 * prevention of duplicate submission after a conversation is completed. Support
 * for this requires tracking active conversations using a conversation service
 * backed by some centralized storage medium like a database table.
 * <p>
 * Warning: storing state (a flow execution continuation) on the client entails
 * a certain security risk. This implementation does not provide a secure way of
 * storing state on the client, so a malicious client could reverse engineer a
 * continuation and get access to possible sensitive data stored in the flow
 * execution. If you need more security and still want to store continuations on
 * the client, subclass this class and override the methods
 * {@link #encode(FlowExecution))} and {@link #decode(String)}, implementing
 * them with a secure encoding/decoding algorithm, e.g. based on public/private
 * key encryption.
 * <p>
 * This class depends on the Jakarta Commons Codec library to do BASE64
 * encoding. Commons code must be available in the classpath when using this
 * implementation.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ClientContinuationFlowExecutionRepository extends AbstractConversationFlowExecutionRepository {

	/**
	 * The continuation factory that will be used to create new continuations to
	 * be added to active conversations.
	 */
	private FlowExecutionContinuationFactory continuationFactory = new SerializedFlowExecutionContinuationFactory();

	/**
	 * Creates a new client flow execution repository.
	 * @param repositoryServices the common services needed by this repository
	 * to function.
	 */
	public ClientContinuationFlowExecutionRepository(FlowExecutionRepositoryServices repositoryServices) {
		super(repositoryServices, new NoOpConversationService());
	}

	/**
	 * Creates a new client flow execution repository.
	 * @param repositoryServices the common services needed by this repository
	 * to function.
	 */
	public ClientContinuationFlowExecutionRepository(FlowExecutionRepositoryServices repositoryServices,
			ConversationService conversationService) {
		super(repositoryServices, conversationService);
	}

	/**
	 * Returns the continuation factory in use by this repository.
	 */
	public FlowExecutionContinuationFactory getContinuationFactory() {
		return continuationFactory;
	}

	/**
	 * Sets the continuation factory used by this repository.
	 */
	public void setContinuationFactory(FlowExecutionContinuationFactory continuationFactory) {
		Assert.notNull(continuationFactory, "The continuation factory is required");
		this.continuationFactory = continuationFactory;
	}

	public FlowExecution getFlowExecution(FlowExecutionKey key) {
		FlowExecutionContinuation continuation = (FlowExecutionContinuation)getContinuationId(key);
		FlowExecution flowExecution = continuation.restore();
		return rehydrate(flowExecution, key);
	}

	public void putFlowExecution(FlowExecutionKey key, FlowExecution flowExecution) {
		putConversationScope(key, asImpl(flowExecution).getConversationScope());
	}

	protected final Serializable generateContinuationId(FlowExecution flowExecution) {
		return encode(flowExecution);
	}

	protected final Serializable parseContinuationId(String encodedId) {
		return decode(encodedId);
	}

	/**
	 * Encode given flow execution object into data that can be stored on the
	 * client.
	 * <p>
	 * Subclasses can override this to change the encoding algorithm. This class
	 * just does a BASE64 encoding of the serialized flow execution.
	 * @param flowExecution the flow execution instance
	 * @return the encoded representation
	 */
	protected Serializable encode(FlowExecution flowExecution) {
		FlowExecutionContinuation continuation = continuationFactory.createContinuation(flowExecution);
		return new String(Base64.encodeBase64(continuation.toByteArray()));
	}

	/**
	 * Decode given data, received from the client, and return the corresponding
	 * flow execution object.
	 * <p>
	 * Subclasses can override this to change the decoding algorithm. This class
	 * just does a BASE64 decoding and then deserializes the flow execution.
	 * @param data the encode flow execution data
	 * @return the decoded flow execution instance
	 */
	protected FlowExecutionContinuation decode(String encodedContinuation) {
		byte[] bytes = Base64.decodeBase64(encodedContinuation.getBytes());
		try {
			return deserializeContinuation(bytes);
		}
		catch (IOException e) {
			throw new FlowExecutionContinuationDeserializationException("This should not happen", e);
		}
		catch (ClassNotFoundException e) {
			throw new FlowExecutionContinuationDeserializationException("This should not happen", e);
		}
	}

	private FlowExecutionContinuation deserializeContinuation(byte[] bytes) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
		try {
			return (FlowExecutionContinuation)ois.readObject();
		}
		finally {
			ois.close();
		}
	}
	
	/**
	 * Conversation service that doesn't do anything - the default.
	 * @author Keith Donald
	 */
	private static class NoOpConversationService implements ConversationService {

		private static final ConversationId conversationId = new SimpleConversationId("1");
		
		private static final NoOpConversation INSTANCE = new NoOpConversation();
		
		public Conversation beginConversation(ConversationParameters conversationParameters) throws ConversationServiceException {
			return INSTANCE;
		}

		public Conversation getConversation(ConversationId id) throws NoSuchConversationException {
			return INSTANCE;
		}

		public ConversationId parseConversationId(String encodedId) throws ConversationServiceException {
			return conversationId;
		}

		private static class NoOpConversation implements Conversation {

			public void end() {
			}

			public Object getAttribute(Object name) {
				return new AttributeMap();
			}

			public ConversationId getId() {
				return conversationId;
			}

			public void lock() {
			}

			public void putAttribute(Object name, Object value) {
			}

			public void removeAttribute(Object name) {
			}

			public void unlock() {
			}	
		}
	}
}