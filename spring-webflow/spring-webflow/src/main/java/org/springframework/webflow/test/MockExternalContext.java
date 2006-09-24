/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.test;

import java.util.HashMap;

import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.ParameterMap;
import org.springframework.webflow.SharedAttributeMap;
import org.springframework.webflow.context.SharedMapDecorator;

/**
 * Mock implementation of the <code>ExternalContext</code> interface.
 * 
 * @author Keith Donald
 */
public class MockExternalContext implements ExternalContext {

	private String contextPath;
	
	private String dispatcherPath;

	private String requestPathInfo;

	private ParameterMap requestParameterMap = new MockParameterMap();

	private AttributeMap requestMap = new AttributeMap();

	private SharedAttributeMap sessionMap = new SharedAttributeMap(new SharedMapDecorator(new HashMap()));

	private SharedAttributeMap applicationMap = new SharedAttributeMap(new SharedMapDecorator(new HashMap()));

	/**
	 * Creates a mock external context with an empty request parameter map.
	 */
	public MockExternalContext() {

	}

	/**
	 * Creates a mock external context with the specified parameters in the
	 * request parameter map.
	 * @param requestParameterMap the request parameters
	 */
	public MockExternalContext(ParameterMap requestParameterMap) {
		this.requestParameterMap = requestParameterMap;
	}

	// implementing external context
	
	public String getContextPath() {
		return contextPath;
	}
	
	public String getDispatcherPath() {
		return dispatcherPath;
	}

	public String getRequestPathInfo() {
		return requestPathInfo;
	}

	public ParameterMap getRequestParameterMap() {
		return requestParameterMap;
	}

	public AttributeMap getRequestMap() {
		return requestMap;
	}

	public SharedAttributeMap getSessionMap() {
		return sessionMap;
	}

	public SharedAttributeMap getApplicationMap() {
		return applicationMap;
	}

	// helper setters

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	
	public void setDispatcherPath(String dispatcherPath) {
		this.dispatcherPath = dispatcherPath;
	}

	public void setRequestPathInfo(String requestPathInfo) {
		this.requestPathInfo = requestPathInfo;
	}

	public void setRequestParameterMap(ParameterMap requestParameterMap) {
		this.requestParameterMap = requestParameterMap;
	}
	
	public void setRequestMap(AttributeMap requestMap) {
		this.requestMap = requestMap;
	}

	public void setSessionMap(SharedAttributeMap sessionMap) {
		this.sessionMap = sessionMap;
	}

	public void setApplicationMap(SharedAttributeMap applicationMap) {
		this.applicationMap = applicationMap;
	}

	public MockParameterMap getMockRequestParameterMap() {
		return (MockParameterMap)requestParameterMap;
	}
	
	/**
	 * Puts a request parameter into the mock parameter map.
	 * @param parameterName the parameter name
	 * @param parameterValue the parameter value
	 */
	public void putRequestParameter(String parameterName, String parameterValue) {
		getMockRequestParameterMap().put(parameterName, parameterValue);
	}

	/**
	 * Puts a multi-valued request parameter into the mock parameter map.
	 * @param parameterName the parameter name
	 * @param parameterValues the parameter values
	 */
	public void putRequestParameter(String parameterName, String[] parameterValues) {
		getMockRequestParameterMap().put(parameterName, parameterValues);
	}
}