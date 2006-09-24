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
package org.springframework.webflow.execution.repository.conversation.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.execution.repository.conversation.ConversationId;

/**
 * A container that holds the state of a conversation. The primary identifier is
 * a conversationId, which identifies a logical <i>conversation</i> or
 * <i>application transaction</i>. This key is used as an index into a single
 * <i>logical</i> executing conversation, identifying a user interaction that
 * is currently in process and has not yet completed.
 * 
 * @author Ben Hale
 */
class ConversationEntry implements Serializable {

	private ConversationId id;

	private String name;
	
	private String caption;

	private String description;

	private ConversationLock lock = ConversationLockFactory.createLock();

	private Map attributes = new HashMap();

	public ConversationEntry(ConversationId id, String name, String caption, String description) {
		this.id = id;
		this.name = name;
		this.caption = caption;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getCaption() {
		return caption;
	}

	public Map getAttributes() {
		return attributes;
	}

	public String getDescription() {
		return description;
	}

	public ConversationId getId() {
		return id;
	}

	public ConversationLock getLock() {
		return lock;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ConversationEntry)) {
			return false;
		}
		return id.equals(((ConversationEntry)o).id);
	}

	public int hashCode() {
		return id.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("id", id).append("caption", caption).append("lock", lock).toString();
	}
}