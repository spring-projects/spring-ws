package org.springframework.webflow.action;

/**
 * An object that is the originator of state that can be captured in a memento
 * token.
 * <p>
 * Provides an operation for {@link #createMemento() creating a token} capturing
 * state, as well as an operation for
 * {@link #setMemento(Memento) setting a token} to update originator state.
 * 
 * @author Keith Donald
 */
public interface MementoOriginator {

	/**
	 * Create a memento holding the state of this originator.
	 * @return the memento
	 */
	public Memento createMemento();

	/**
	 * Update the state of the originator based on the memento.
	 * @param memento the memento
	 */
	public void setMemento(Memento memento);
}