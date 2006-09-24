package org.springframework.webflow;

/**
 * Thrown when a state transition could not be executed.
 * @author Keith Donald
 */
public class CannotExecuteTransitionException extends FlowException {
	
	/**
	 * The transition that could not be executed. 
	 */
	private Transition transition;

	/**
	 * Create a new exception.
	 * @param transition the transition
	 */
	public CannotExecuteTransitionException(Transition transition) {
		super("Cannot execute transition " + transition);
		this.transition = transition;
	}

	/**
	 * Returns the transition that could not be executed.
	 * @return the transition
	 */
	public Transition getTransition() {
		return transition;
	}
}