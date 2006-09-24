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
package org.springframework.webflow.builder;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.support.FlowExecutionRedirect;
import org.springframework.webflow.support.FlowRedirect;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Test case for TextToViewDescriptorCreator.
 * 
 * @author Erwin Vervaet
 */
public class TextToViewSelectorTests extends TestCase {

	private TextToViewSelector converter;

	public void setUp() {
		BaseFlowServiceLocator flowArtifactFactory = new BaseFlowServiceLocator();
		converter = new TextToViewSelector(flowArtifactFactory);
		converter.setConversionService(flowArtifactFactory.getConversionService());
	}

	public void testApplicationView() {
		ViewSelector selector = (ViewSelector)viewSelector(TextToViewSelector.VIEW_STATE_TYPE, "myView");
		RequestContext context = getRequestContext();
		ApplicationView view = (ApplicationView)selector.makeSelection(context);
		assertEquals("myView", view.getViewName());
		assertEquals(5, view.getModel().size());
	}

	public void testFlowExecutionRedirect() {
		ViewSelector selector = (ViewSelector)viewSelector(TextToViewSelector.VIEW_STATE_TYPE, "redirect:myView");
		RequestContext context = getRequestContext();
		FlowExecutionRedirect redirect = (FlowExecutionRedirect)selector.makeSelection(context);
		assertSame(redirect, FlowExecutionRedirect.INSTANCE);
		context.getRequestScope().clear();
		ApplicationView view = (ApplicationView)selector.makeRefreshSelection(context);
		assertEquals("myView", view.getViewName());
		assertEquals(3, view.getModel().size());
	}

	public void testFlowRedirect() {
		ViewSelector selector = (ViewSelector)viewSelector(TextToViewSelector.END_STATE_TYPE, "flowRedirect:myFlow");
		RequestContext context = getRequestContext();
		FlowRedirect redirect = (FlowRedirect)selector.makeSelection(context);
		assertEquals("myFlow", redirect.getFlowId());
		assertEquals(0, redirect.getInput().size());
	}

	public void testFlowRedirectWithModel() {
		ViewSelector selector = (ViewSelector)viewSelector(TextToViewSelector.END_STATE_TYPE,
				"flowRedirect:myFlow?foo=${flowScope.foo}&bar=${requestScope.oven}");
		RequestContext context = getRequestContext();
		FlowRedirect redirect = (FlowRedirect)selector.makeSelection(context);
		assertEquals("myFlow", redirect.getFlowId());
		assertEquals(2, redirect.getInput().size());
		assertEquals("bar", redirect.getInput().get("foo"));
		assertEquals("mit", redirect.getInput().get("bar"));
	}

	public void testExternalRedirect() {
		ViewSelector selector = (ViewSelector)viewSelector(TextToViewSelector.END_STATE_TYPE,
				"externalRedirect:myUrl.htm?foo=${flowScope.foo}&bar=${requestScope.oven}");
		RequestContext context = getRequestContext();
		ExternalRedirect view = (ExternalRedirect)selector.makeSelection(context);
		assertEquals("myUrl.htm?foo=bar&bar=mit", view.getUrl());
	}

	private RequestContext getRequestContext() {
		MockRequestContext ctx = new MockRequestContext();
		ctx.getFlowScope().put("foo", "bar");
		ctx.getFlowScope().put("bar", "car");
		ctx.getRequestScope().put("oven", "mit");
		ctx.getRequestScope().put("cat", "woman");
		ctx.getFlowScope().put("boo", new Integer(3));
		ctx.setLastEvent(new Event(this, "sample"));
		return ctx;
	}

	/**
	 * Turn given view name into a corresponding view selector.
	 * @param viewName the view name (might be encoded)
	 * @return the corresponding view selector
	 */
	protected ViewSelector viewSelector(String stateType, String viewName) {
		Map context = new HashMap(1, 1);
		context.put(TextToViewSelector.STATE_TYPE_CONTEXT_PARAMETER, stateType);
		return (ViewSelector)converter.convert(viewName, context);
	}
}