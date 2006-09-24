package org.springframework.webflow.execution.repository.support;

import java.io.Serializable;

import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;

/**
 * Thrown when no flow execution continuation exists within a continuation
 * group. with the provided id This might occur if the continuation was expired
 * or was explictly invalidated but a client's browser page cache still
 * references it.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class InvalidContinuationIdException extends FlowExecutionRepositoryException {

	/**
	 * The unique continuation identifier that was invalid.
	 */
	private Serializable continuationId;

	/**
	 * Creates a continuation not found exception.
	 * @param continuationId the invalid continuation id
	 */
	public InvalidContinuationIdException(Serializable continuationId) {
		super("The continuation id '" + continuationId + "' is invalid.  Access to flow execution denied.");
	}

	/**
	 * Returns the continuation id.
	 */
	public Serializable getContinuationId() {
		return continuationId;
	}
}