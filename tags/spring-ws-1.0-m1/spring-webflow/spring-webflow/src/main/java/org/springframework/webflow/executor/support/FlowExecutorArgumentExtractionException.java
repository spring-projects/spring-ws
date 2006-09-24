package org.springframework.webflow.executor.support;

import org.springframework.webflow.FlowException;

/**
 * A runtime exception thrown by a flow executor argument extractor when a
 * argument could not be extracted.
 * @author Keith Donald
 */
public class FlowExecutorArgumentExtractionException extends FlowException {

	/**
	 * Creates a new exception.
	 * @param msg the message
	 */
	public FlowExecutorArgumentExtractionException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new exception.
	 * @param msg the message
	 * @param cause the cause
	 */
	public FlowExecutorArgumentExtractionException(String msg, Throwable cause) {
		super(msg, cause);
	}
}