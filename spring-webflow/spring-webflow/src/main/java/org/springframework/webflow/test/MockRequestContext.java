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

import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.Event;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.ParameterMap;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.Transition;
import org.springframework.webflow.UnmodifiableAttributeMap;

/**
 * Mock implementation of the <code>RequestContext</code> interface to
 * facilitate standalone Action unit tests.
 * <p>
 * NOT intended to be used for anything but standalone unit tests. This is a
 * simple state holder, a <i>stub</i> implementation, at least if you follow <a
 * href="http://www.martinfowler.com/articles/mocksArentStubs.html">Martin
 * Fowler's</a> reasoning. This class is called <i>Mock</i>RequestContext to
 * be consistent with the naming convention in the rest of the Spring framework
 * (e.g. MockHttpServletRequest, ...).
 * 
 * @see org.springframework.webflow.RequestContext
 * @see org.springframework.webflow.Action
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MockRequestContext implements RequestContext {

	private FlowExecutionContext flowExecutionContext = new MockFlowExecutionContext();

	private ExternalContext externalContext = new MockExternalContext();

	private AttributeMap requestScope = new AttributeMap();

	private Event lastEvent;

	private Transition lastTransition;

	private AttributeMap attributes = new AttributeMap();

	/**
	 * Creates a new mock request context with the following defaults:
	 * <ul>
	 * <li>A flow execution context with a active session of flow "mockFlow" in
	 * state "mockState".
	 * <li>A mock external context with no request parameters set.
	 * </ul>
	 * To add request parameters to this request, use the
	 * {@link #putRequestParameter(String, String) } method.
	 */
	public MockRequestContext() {

	}

	/**
	 * Creates a new mock request context with the following defaults:
	 * <ul>
	 * <li>A flow execution context with an active session for the specified flow.
	 * <li>A mock external context with no request parameters set.
	 * </ul>
	 * To add request parameters to this request, use the
	 * {@link #putRequestParameter(String, String) } method.
	 */
	public MockRequestContext(Flow flow) {
		flowExecutionContext = new MockFlowExecutionContext(flow);
	}
	
	/**
	 * Creates a new mock request context with the following defaults:
	 * <ul>
	 * <li>A flow execution context with a active session of flow "mockFlow" in
	 * state "mockState".
	 * <li>A mock external context with the provided parameters set.
	 * </ul>
	 */
	public MockRequestContext(ParameterMap requestParameterMap) {
		externalContext = new MockExternalContext(requestParameterMap);
	}

	// implementing RequestContext

	public Flow getActiveFlow() {
		return getFlowExecutionContext().getActiveSession().getFlow();
	}

	public State getCurrentState() {
		return getFlowExecutionContext().getActiveSession().getState();
	}

	public AttributeMap getRequestScope() {
		return requestScope;
	}

	public AttributeMap getFlowScope() {
		return getFlowExecutionContext().getActiveSession().getScope();
	}

	public AttributeMap getConversationScope() {
		return getMockFlowExecutionContext().getConversationScope();
	}

	public ParameterMap getRequestParameters() {
		return externalContext.getRequestParameterMap();
	}

	public ExternalContext getExternalContext() {
		return externalContext;
	}

	public FlowExecutionContext getFlowExecutionContext() {
		return flowExecutionContext;
	}

	public Event getLastEvent() {
		return lastEvent;
	}

	public Transition getLastTransition() {
		return lastTransition;
	}

	public UnmodifiableAttributeMap getAttributes() {
		return attributes.unmodifiable();
	}

	public void setAttributes(AttributeCollection attributes) {
		this.attributes.replaceWith(attributes);
	}

	public UnmodifiableAttributeMap getModel() {
		return getConversationScope().union(getFlowScope()).union(getRequestScope()).unmodifiable();
	}

	/**
	 * Set the last event that occured in this request context.
	 * @param lastEvent the event to set
	 */
	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
	}

	/**
	 * Set the last transition that executed in this request context.
	 * @param lastTransition the last transition to set
	 */
	public void setLastTransition(Transition lastTransition) {
		this.lastTransition = lastTransition;
	}

	/**
	 * Set a request context attribute.
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 */
	public void setAttribute(String attributeName, Object attributeValue) {
		attributes.put(attributeName, attributeValue);
	}

	/**
	 * Remove a request context attribute.
	 * @param attributeName the attribute name
	 */
	public void removeAttribute(String attributeName) {
		attributes.remove(attributeName);
	}
	
	/**
	 * Sets the flow execution context.
	 */
	public void setFlowExecutionContext(FlowExecutionContext flowExecutionContext) {
		this.flowExecutionContext = flowExecutionContext;
	}

	/**
	 * Sets the external context.
	 */
	public void setExternalContext(ExternalContext externalContext) {
		this.externalContext = externalContext;
	}

	/**
	 * Returns the flow execution context as a {@link MockFlowExecutionContext}.
	 */
	public MockFlowExecutionContext getMockFlowExecutionContext() {
		return (MockFlowExecutionContext)flowExecutionContext;
	}

	/**
	 * Returns the external context as a {@link MockExternalContext}.
	 */
	public MockExternalContext getMockExternalContext() {
		return (MockExternalContext)externalContext;
	}

	/**
	 * Sets the active flow session of the executing flow associated with this
	 * request.
	 */
	public void setActiveSession(FlowSession flowSession) {
		getMockFlowExecutionContext().setActiveSession(flowSession);
	}

	/**
	 * Adds a request parameter to the configured external context.
	 * @param parameterName the parameter name
	 * @param parameterValue the parameter value
	 */
	public void putRequestParameter(String parameterName, String parameterValue) {
		getMockExternalContext().putRequestParameter(parameterName, parameterValue);
	}

	/**
	 * Adds a multi-valued request parameter to the configured external context.
	 * @param parameterName the parameter name
	 * @param parameterValues the parameter values
	 */
	public void putRequestParameter(String parameterName, String[] parameterValues) {
		getMockExternalContext().putRequestParameter(parameterName, parameterValues);
	}
	
	/**
	 * Returns the contained mutable context {@link AttributeMap} allowing setting of mock context 
	 * attributes.
	 * @return the attribute map
	 */
	public AttributeMap getAttributeMap() {
		return attributes;
	}
}