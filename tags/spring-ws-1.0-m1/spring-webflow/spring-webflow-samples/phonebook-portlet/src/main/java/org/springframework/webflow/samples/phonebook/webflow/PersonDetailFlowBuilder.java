/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.samples.phonebook.webflow;

import org.springframework.webflow.Transition;
import org.springframework.webflow.builder.AbstractFlowBuilder;
import org.springframework.webflow.builder.FlowBuilderException;
import org.springframework.webflow.builder.FlowServiceLocator;
import org.springframework.webflow.support.DefaultFlowAttributeMapper;

/**
 * Java-based flow builder that builds the person details flow, exactly like it
 * is defined in the <code>detail.xml</code> XML flow definition.
 * <p>
 * This encapsulates the page flow of viewing a person's details and their
 * collegues in a reusable, self-contained module.
 * 
 * @author Keith Donald
 */
public class PersonDetailFlowBuilder extends AbstractFlowBuilder {

	public PersonDetailFlowBuilder(FlowServiceLocator flowServiceLocator) {
		super(flowServiceLocator);
	}

	public void buildStates() throws FlowBuilderException {
		// get the person given a userid as input
		addActionState("getDetails", action("phonebook", method("getPerson(${flowScope.id})"), result("person")),
				transition(on(success()), to("displayDetails")));

		// view the person details
		addViewState("displayDetails", "details", new Transition[] { transition(on(back()), to("finish")),
				transition(on(select()), to("browseColleagueDetails")) });

		// view details for selected collegue
		DefaultFlowAttributeMapper idMapper = new DefaultFlowAttributeMapper();
		idMapper.addInputMapping(mapping().source("externalContext.requestParameterMap.id").target("id").from(
				String.class).to(Long.class).value());
		addSubflowState("browseColleagueDetails", getFlow(), idMapper, transition(on(finish()), to("getDetails")));

		// end
		addEndState("finish");

		// end error
		addEndState("error");
	}
}