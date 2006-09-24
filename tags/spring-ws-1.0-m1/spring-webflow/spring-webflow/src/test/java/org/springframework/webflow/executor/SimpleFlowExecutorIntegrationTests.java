package org.springframework.webflow.executor;

public class SimpleFlowExecutorIntegrationTests extends FlowExecutorIntegrationTests {
	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/webflow/executor/context-simple.xml" };
	}
}