/**
 * 
 */
package org.springframework.webflow.builder;

public class SimpleFlowBuilder extends AbstractFlowBuilder {
	public void buildStates() {
		addEndState("end");
	}
}