package org.springframework.webflow.samples.sellitem;

import org.springframework.webflow.execution.ConditionalFlowExecutionListenerLoader;

public class FlowExecutionListenerLoader extends ConditionalFlowExecutionListenerLoader {
	public FlowExecutionListenerLoader() {
		addListener(new SellItemFlowExecutionListener());
	}
}
