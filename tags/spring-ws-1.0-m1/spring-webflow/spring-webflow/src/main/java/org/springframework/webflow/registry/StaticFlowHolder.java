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
package org.springframework.webflow.registry;

import org.springframework.webflow.Flow;

/**
 * A simple flow holder that just holds a static singleton reference to a flow
 * definition.
 * 
 * @author Keith Donald
 */
public class StaticFlowHolder implements FlowHolder {
	
	/**
	 * The held flow. 
	 */
	private Flow flow;

	/**
	 * Creates the static flow holder
	 * @param flow the flow to hold
	 */
	public StaticFlowHolder(Flow flow) {
		this.flow = flow;
	}

	public Flow getFlow() {
		return flow;
	}

	public String getId() {
		return flow.getId();
	}

	public void refresh() {
		// nothing to do
	}
}