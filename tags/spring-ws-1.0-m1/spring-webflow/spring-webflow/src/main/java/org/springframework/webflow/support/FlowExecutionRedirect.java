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
package org.springframework.webflow.support;

import java.io.ObjectStreamException;

import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewState;

/**
 * Concrete response type that requests an redirect to an <i>existing</i>,
 * active Spring Web Flow execution at a unique SWF-specific <i>flow execution
 * URL</i>. This enables the triggering of redirect after post semantics from
 * within an <i>active</i> flow execution.
 * <p>
 * Once the redirect response is issued a new request is initiated by the
 * browser targeted at the flow execution URL. The URL is stabally refreshable
 * (and bookmarkable) while the conversation remains active, safely triggering a
 * {@link ViewState#refresh(org.springframework.webflow.RequestContext)} on each
 * access.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public final class FlowExecutionRedirect extends ViewSelection {

	public static final FlowExecutionRedirect INSTANCE = new FlowExecutionRedirect();

	private FlowExecutionRedirect() {
	}

	// resolve the singleton instance
	private Object readResolve() throws ObjectStreamException {
		return INSTANCE;
	}

	public String toString() {
		return "flowExecutionRedirect";
	}
}