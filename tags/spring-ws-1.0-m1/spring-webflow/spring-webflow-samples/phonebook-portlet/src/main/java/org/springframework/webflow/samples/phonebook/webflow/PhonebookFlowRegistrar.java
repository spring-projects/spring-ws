package org.springframework.webflow.samples.phonebook.webflow;

import org.springframework.webflow.builder.FlowServiceLocator;
import org.springframework.webflow.registry.FlowRegistrarSupport;
import org.springframework.webflow.registry.FlowRegistry;

/**
 * Demonstrates how to register flows programatically.
 * 
 * @author Keith Donald
 */
public class PhonebookFlowRegistrar extends FlowRegistrarSupport {
	public void registerFlows(FlowRegistry registry, FlowServiceLocator flowServiceLocator) {
		registerFlow("search-flow", registry, new SearchPersonFlowBuilder(flowServiceLocator));
		registerFlow("detail-flow", registry, new PersonDetailFlowBuilder(flowServiceLocator));
	}
}