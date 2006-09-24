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
package org.springframework.webflow;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * A flow artifact exception is thrown when a artifact (such as a flow, state,
 * action, etc) required by the webflow system cannot be obtained, either at
 * flow configuration time or at runtime. Can be used directly, but you are
 * encouraged to create a specific subclass to describe a specific kind of
 * failure scenario.
 * <p>
 * Flow artifact exceptions indicate unrecoverable problems with the flow
 * definition, e.g. a required state of a flow cannot be found. They're not used
 * to signal problems related to execution of a client request. A StateException
 * is used for that.
 * 
 * @see org.springframework.webflow.StateException
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowArtifactException extends FlowException {

	/**
	 * The id of the artifact that could not be retrieved.
	 */
	private String artifactId;

	/**
	 * The type of artifact that could not be retrieved.
	 */
	private Class artifactType;

	/**
	 * Create a new flow artifact exception.
	 * @param artifactId the id of the artifact
	 * @param artifactType the expected artifact type
	 */
	public FlowArtifactException(String artifactId, Class artifactType) {
		this(artifactId, artifactType, null, null);
	}

	/**
	 * Create a new flow artifact exception.
	 * @param artifactId the id of the artifact
	 * @param artifactType the expected artifact type
	 * @param cause the underlying cause of this exception
	 */
	public FlowArtifactException(String artifactId, Class artifactType, Throwable cause) {
		this(artifactId, artifactType, null, cause);
	}

	/**
	 * Create a new flow artifact exception.
	 * @param artifactId the id of the artifact
	 * @param artifactType the expected artifact type
	 * @param message descriptive message
	 */
	public FlowArtifactException(String artifactId, Class artifactType, String message) {
		this(artifactId, artifactType, message, null);
	}

	/**
	 * Create a new flow artifact lookup exception.
	 * @param artifactId the id of the artifact
	 * @param artifactType the expected artifact type
	 * @param message descriptive message
	 * @param cause the underlying cause of this exception
	 */
	public FlowArtifactException(String artifactId, Class artifactType, String message, Throwable cause) {
		super((StringUtils.hasText(message) ? message : "Unable to obtain a " + ClassUtils.getShortName(artifactType)
				+ " flow artifact with id '" + artifactId + "': make sure there is a valid [" + artifactType
				+ "] exported with this id"), cause);
		this.artifactType = artifactType;
		this.artifactId = artifactId;
	}

	/**
	 * Returns the id of the service that cannot be found.
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * Returns the expected service type.
	 */
	public Class getArtifactType() {
		return artifactType;
	}
}