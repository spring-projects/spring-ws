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

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.webflow.Flow;
import org.springframework.webflow.builder.FlowAssembler;
import org.springframework.webflow.builder.FlowBuilder;
import org.springframework.webflow.builder.ResourceHolder;

/**
 * A flow definition holder that can detect changes on an underlying flow
 * definition resource and refresh that resource automatically.
 * 
 * This class is threadsafe.
 * 
 * @author Keith Donald
 */
public class RefreshableFlowHolder implements FlowHolder {

	/**
	 * The flow definition assembled by this assembler.
	 */
	private Flow flow;

	/**
	 * The flow assembler.
	 */
	private FlowAssembler assembler;

	/**
	 * A last modified date for the backing flow resource, used to support
	 * automatic reassembly on resource change.
	 */
	private long lastModified;

	/**
	 * A flag indicating whether or not this assembler is in the middle of the
	 * assembly process.
	 */
	private boolean assembling;

	/**
	 * Creates a new refreshable flow holder that uses the configured assembler (GOF director) to
	 * drive flow assembly, on initial use and on any resource change.
	 * @param assembler the flow assembler (director)
	 */
	public RefreshableFlowHolder(FlowAssembler assembler) {
		this.assembler = assembler;
	}

	public String getId() {
		return assembler.getFlowId();
	}

	/**
	 * Returns the flow builder that actually builds the Flow definition.
	 */
	public FlowBuilder getFlowBuilder() {
		return assembler.getFlowBuilder();
	}

	public synchronized Flow getFlow() {
		if (assembling) {
			// must return early assembly result
			return getFlowBuilder().getFlow();
		}
		if (!isAssembled()) {
			lastModified = calculateLastModified();
			assembleFlow();
		}
		else {
			refreshIfChanged();
		}
		return flow;
	}

	public synchronized void refresh() {
		assembleFlow();
	}

	/**
	 * Reassemble the flow if its underlying resource has changed.
	 */
	protected void refreshIfChanged() {
		if (this.lastModified == -1) {
			// just ignore, tracking last modified date not supported
			return;
		}
		if (this.lastModified < calculateLastModified()) {
			assembleFlow();
		}
	}

	/**
	 * Assemble the held flow definition, delegating to the configured
	 * FlowAssembler (director).
	 */
	protected void assembleFlow() {
		try {
			assembling = true;
			assembler.assembleFlow();
			flow = getFlowBuilder().getFlow();
		}
		finally {
			assembling = false;
		}
	}

	/**
	 * Helper that retrieves the last modified date by querying the backing flow
	 * resource.
	 * @return the last modified date, or -1 if it could not be retrieved
	 */
	protected long calculateLastModified() {
		if (getFlowBuilder() instanceof ResourceHolder) {
			Resource resource = ((ResourceHolder)getFlowBuilder()).getResource();
			try {
				return resource.getFile().lastModified();
			}
			catch (IOException e) {
				// ignore, last modified checks not supported
			}
		}
		return -1;
	}
	
	/**
	 * Returns a flag indicating if this holder has performed and completed
	 * Flow assembly.
	 */
	protected boolean isAssembled() {
		return flow != null;
	}

	/**
	 * Returns a flag indicating if this holder is performing assembly.
	 */
	protected boolean isAssembling() {
		return assembling;
	}
	
	/**
	 * Returns the last modifed date of the backed builder resource.
	 * @return the last modified date
	 */
	protected long getLastModified() {
		return lastModified;
	}
}