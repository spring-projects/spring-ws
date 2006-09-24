package org.springframework.webflow.action;

import java.io.Serializable;

/**
 * An abstract base class for mementos that encapsulate the state of a
 * {@link MementoOriginator}. Basically, a token storing the state of another
 * object.
 * <p>
 * Mementos are expected to be managed by caretakers (clients) without
 * the clents being aware of their internal structure. Only the originator is aware of
 * the internal structure of a concrete Memento implementation.
 * 
 * @see MementoOriginator
 * 
 * @author Keith Donald
 */
public abstract class Memento implements Serializable {

}
