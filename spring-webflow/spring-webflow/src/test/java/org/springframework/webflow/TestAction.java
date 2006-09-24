package org.springframework.webflow;

import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;

public class TestAction extends AbstractAction {
	private Event result = new Event(this, "success");

	private boolean executed;

	private int executionCount;

	public TestAction() {

	}

	public TestAction(String result) {
		if (StringUtils.hasText(result)) {
			this.result = new Event(this, result);
		}
		else {
			this.result = null;
		}
	}

	public boolean isExecuted() {
		return executed;
	}

	public int getExecutionCount() {
		return executionCount;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		executed = true;
		executionCount++;
		return result;
	}
}