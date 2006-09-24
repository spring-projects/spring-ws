/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.samples.phonebook.webflow;

import java.io.File;
import java.util.Map;

import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Flow;
import org.springframework.webflow.builder.FlowServiceLocator;
import org.springframework.webflow.registry.ExternalizedFlowDefinition;
import org.springframework.webflow.samples.phonebook.domain.ArrayListPhoneBook;
import org.springframework.webflow.samples.phonebook.domain.PhoneBook;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.test.AbstractXmlFlowExecutionTests;
import org.springframework.webflow.test.MockFlowServiceLocator;
import org.springframework.webflow.test.MockParameterMap;

public class SearchFlowExecutionTests extends AbstractXmlFlowExecutionTests {

	public void testStartFlow() {
		ApplicationView view = applicationView(startFlow());
		assertCurrentStateEquals("enterCriteria");
		assertViewNameEquals("searchCriteria", view);
		assertModelAttributeNotNull("searchCriteria", view);
	}

	public void testCriteriaSubmitSuccess() {
		startFlow();
		MockParameterMap parameters = new MockParameterMap();
		parameters.put("firstName", "Keith");
		parameters.put("lastName", "Donald");
		ApplicationView view = applicationView(signalEvent("search", parameters));
		assertCurrentStateEquals("displayResults");
		assertViewNameEquals("searchResults", view);
		assertModelAttributeCollectionSize(1, "results", view);
	}

	public void testCriteriaSubmitError() {
		startFlow();
		signalEvent("search");
		assertCurrentStateEquals("enterCriteria");
	}

	public void testNewSearch() {
		testCriteriaSubmitSuccess();
		ApplicationView view = applicationView(signalEvent("newSearch"));
		assertCurrentStateEquals("enterCriteria");
		assertViewNameEquals("searchCriteria", view);
	}

	public void testSelectValidResult() {
		testCriteriaSubmitSuccess();
		MockParameterMap parameters = new MockParameterMap();
		parameters.put("id", "1");
		ApplicationView view = applicationView(signalEvent("select", parameters));
		assertCurrentStateEquals("displayResults");
		assertViewNameEquals("searchResults", view);
		assertModelAttributeCollectionSize(1, "results", view);
	}

	/**
	 * A stub for testing.
	 */
	private PhoneBook phonebook = new ArrayListPhoneBook();

	protected ExternalizedFlowDefinition getFlowDefinition() {
		File flowDir = new File("src/main/webapp/WEB-INF/flows");
		Resource resource = new FileSystemResource(new File(flowDir, "search-flow.xml"));
		return new ExternalizedFlowDefinition(resource);
	}

	protected FlowServiceLocator createFlowServiceLocator() {
		MockFlowServiceLocator flowServiceLocator = new MockFlowServiceLocator();

		Flow detailFlow = new Flow("detail-flow");
		// test responding to finish result
		new EndState(detailFlow, "finish");
		detailFlow.setInputMapper(new AttributeMapper() {
			public void map(Object source, Object target, Map context) {
				assertEquals("id of value 1 not provided as input by calling search flow", new Long(1), ((AttributeMap)source).get("id"));
			}
		});
		flowServiceLocator.registerSubflow(detailFlow);
		flowServiceLocator.registerBean("phonebook", phonebook);
		return flowServiceLocator;
	}
}