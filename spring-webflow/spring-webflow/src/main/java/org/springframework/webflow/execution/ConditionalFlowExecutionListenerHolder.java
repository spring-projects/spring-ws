package org.springframework.webflow.execution;

import java.util.Iterator;
import java.util.Set;

import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;
import org.springframework.webflow.Flow;

/**
 * A holder that holds a listener plus a set of criteria defining the flows
 * in which that listener applies.
 * @author Keith Donald
 */
class ConditionalFlowExecutionListenerHolder {

	/**
	 * The held listener
	 */
	private FlowExecutionListener listener;

	/**
	 * The listener criteria set.
	 */
	private Set criteriaSet = CollectionFactory.createLinkedSetIfPossible(3);

	public ConditionalFlowExecutionListenerHolder(FlowExecutionListener listener) {
		Assert.notNull(listener, "The listener is required");
		this.listener = listener;
	}

	public FlowExecutionListener getListener() {
		return listener;
	}

	public void add(FlowExecutionListenerCriteria criteria) {
		criteriaSet.add(criteria);
	}

	public void remove(FlowExecutionListenerCriteria criteria) {
		criteriaSet.remove(criteria);
	}

	public boolean isCriteriaSetEmpty() {
		return criteriaSet.isEmpty();
	}

	public boolean equals(Object o) {
		if (!(o instanceof ConditionalFlowExecutionListenerHolder)) {
			return false;
		}
		return listener.equals(((ConditionalFlowExecutionListenerHolder)o).listener);
	}

	public int hashCode() {
		return listener.hashCode();
	}

	/**
	 * Determines if the listener held by this holder applies to the
	 * specified flow definition.
	 * @param flow the flow
	 * @return true if yes, false otherwise.
	 */
	public boolean listenerAppliesTo(Flow flow) {
		Iterator it = criteriaSet.iterator();
		while (it.hasNext()) {
			FlowExecutionListenerCriteria criteria = (FlowExecutionListenerCriteria)it.next();
			if (criteria.appliesTo(flow)) {
				return true;
			}
		}
		return false;
	}
}