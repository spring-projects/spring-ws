package org.springframework.webflow.execution;

/**
 * A value object describing an event that occured within a flow execution.
 * 
 * @author Keith Donald
 */
public class EventId {
	
	/**
	 * The value of the event identifier.
	 */
	private String value;
	
	/**
	 * Constructs a new event id.
	 * @param value the string id value
	 */
	public EventId(String value) {
		this.value = value;
	}
	
	/**
	 * Returns the raw string event id value.
	 * @return the string id value
	 */
	public String getValue() {
		return value;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof EventId)) {
			return false;
		}
		EventId other = (EventId)o;
		return value.equals(other.value);
	}
	
	public int hashCode() {
		return value.hashCode();
	}
	
	public String toString() {
		return value;
	}
}