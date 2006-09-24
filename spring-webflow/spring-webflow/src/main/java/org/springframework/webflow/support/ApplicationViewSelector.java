/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.support;

import java.io.Serializable;
import java.util.Collections;

import org.springframework.binding.expression.Expression;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewSelector;

/**
 * Simple view selector that makes an {@link ApplicationView} selection using a
 * view name expression.
 * <p>
 * This factory will treat all attributes returned from calling
 * {@link RequestContext#getModel()} as the application model exposed to the
 * view during rendering. This is typically the union of attributes in request,
 * flow, and conversation scope.
 * <p>
 * This selector also supports setting a <i>redirectType</i> value that can be
 * used to trigger a redirect to the {@link ApplicationView} at a bookmarkable
 * URL.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ApplicationViewSelector implements ViewSelector, Serializable {

	/**
	 * The view name to render.
	 */
	private Expression viewName;

	/**
	 * A value indicating if a redirect to the selected application view should
	 * be requested.
	 * <p>
	 * Setting this to something other than <code>null</code> allows you
	 * to redirect while the flow is in progress to a stable URL that can be
	 * safely refreshed.
	 */
	private RedirectType redirectType;

	/**
	 * Creates a application view selector that will make application view
	 * selections requesting that the specified view be rendered.
	 * @param viewName the view name expression
	 */
	public ApplicationViewSelector(Expression viewName) {
		this(viewName, null);
	}

	/**
	 * Creates a application view selector that will make application view
	 * selections requesting that the specified view be rendered.
	 * @param viewName the view name expression
	 * @param redirectType indicates if a redirect to the view should be
	 * initiated
	 */
	public ApplicationViewSelector(Expression viewName, RedirectType redirectType) {
		Assert.notNull(viewName, "The view name expression is required");
		this.viewName = viewName;
		this.redirectType = redirectType;
	}

	/**
	 * Returns the name of the view that should be rendered.
	 */
	public Expression getViewName() {
		return viewName;
	}

	public ViewSelection makeSelection(RequestContext context) {
		if (redirectType != null) {
			return redirectType.select();
		} else {
			return makeRefreshSelection(context);
		}
	}

	public ViewSelection makeRefreshSelection(RequestContext context) {
		String viewName = resolveViewName(context);
		if (!StringUtils.hasText(viewName)) {
			return ViewSelection.NULL_VIEW;
		}
		return createApplicationView(viewName, context);
	}

	/**
	 * Resolves the application view name from the request context.
	 * @param context the context
	 * @return the view name
	 */
	protected String resolveViewName(RequestContext context) {
		return (String)getViewName().evaluateAgainst(context, Collections.EMPTY_MAP);
	}

	/**
	 * Creates the application view selection.
	 * @param viewName the resolved view name
	 * @param context the context
	 * @return the application view
	 */
	protected ApplicationView createApplicationView(String viewName, RequestContext context) {
		return new ApplicationView(viewName, context.getModel().getMap());
	}
	
	public String toString() {
		return new ToStringCreator(this).append("viewName", viewName).append("redirectType", redirectType).toString();
	}
}