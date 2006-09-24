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
package org.springframework.webflow.builder;

import org.springframework.binding.convert.support.ConversionServiceAwareConverter;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.util.MapAccessor;
import org.springframework.util.StringUtils;
import org.springframework.webflow.NullViewSelector;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.support.ApplicationViewSelector;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.support.ExternalRedirectSelector;
import org.springframework.webflow.support.FlowExecutionRedirect;
import org.springframework.webflow.support.FlowRedirect;
import org.springframework.webflow.support.FlowRedirectSelector;
import org.springframework.webflow.support.RedirectType;

/**
 * Converter that converts an encoded string representation of a view selector
 * into a {@link ViewSelector} object that will make selections at runtime.
 * 
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"viewName" - will result in a {@link ApplicationViewSelector} that
 * returns a ViewSelection with the provided view name expression.</li>
 * <li>"redirect:&lt;viewName&gt;" - will result in a
 * {@link ApplicationViewSelector} that returns a {@link FlowExecutionRedirect}
 * to a flow execution URL.</li>
 * <li>"externalRedirect:&lt;url&gt;" - will result in a
 * {@link ExternalRedirectSelector} that returns a {@link ExternalRedirect} to a
 * URL.</li>
 * <li>"flowRedirect:&lt;url&gt;" - will result in a
 * {@link FlowRedirectSelector} that returns a {@link FlowRedirect} to a flow.</li>
 * <li>"bean:&lt;id&gt;" - will result usage of a custom
 * <code>ViewSelector</code> bean implementation.</li>
 * </ul>
 * 
 * @see org.springframework.webflow.ViewSelection
 * @see org.springframework.webflow.ViewSelector
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToViewSelector extends ConversionServiceAwareConverter {

	/**
	 * The name of the context parameter indicating which state type the
	 * conversion operation applies.
	 */
	public static final String STATE_TYPE_CONTEXT_PARAMETER = "stateType";

	/**
	 * Conversion context parameter value constant indicating the encoded view
	 * selector applies to a view state.
	 */
	public static final String VIEW_STATE_TYPE = "view";

	/**
	 * Conversion context parameter value constant indicating the encoded view
	 * selector applies to an end state.
	 */
	public static final String END_STATE_TYPE = "end";

	/**
	 * Prefix used when the encoded view name wants to specify that a redirect
	 * is required.
	 */
	public static final String REDIRECT_PREFIX = "redirect:";

	/**
	 * Prefix used when the encoded view name wants to specify that a redirect
	 * to an external URL is required.
	 */
	public static final String EXTERNAL_REDIRECT_PREFIX = "externalRedirect:";

	/**
	 * Prefix used when the encoded view name wants to specify that a redirect
	 * to a flow is requred.
	 */
	public static final String FLOW_REDIRECT_PREFIX = "flowRedirect:";

	/**
	 * Prefix used when the user wants to use a ViewSelector implementation
	 * managed by a factory.
	 */
	private static final String BEAN_PREFIX = "bean:";

	/**
	 * Factory to use for loading custom ViewSelector beans.
	 */
	private FlowServiceLocator flowServiceLocator;

	/**
	 * Create a new text to ViewSelector converter.
	 */
	public TextToViewSelector(FlowServiceLocator flowServiceLocator) {
		this.flowServiceLocator = flowServiceLocator;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { ViewSelector.class };
	}

	protected Object doConvert(Object source, Class targetClass, MapAccessor context) throws Exception {
		String encodedView = (String)source;
		if (!StringUtils.hasText(encodedView)) {
			return NullViewSelector.INSTANCE;
		}
		String stateType = context.getRequiredString(STATE_TYPE_CONTEXT_PARAMETER);
		if (stateType.equals(VIEW_STATE_TYPE)) {
			return convertEncodedViewStateSelector(encodedView);
		}
		else if (stateType.equals(END_STATE_TYPE)) {
			return convertEncodedEndStateSelector(encodedView);
		}
		else {
			throw new IllegalArgumentException("No state context provided; unable to convert encoded view selector '"
					+ encodedView + "'");
		}
	}

	protected ViewSelector convertEncodedViewStateSelector(String encodedView) {
		if (encodedView.startsWith(REDIRECT_PREFIX)) {
			String viewName = encodedView.substring(REDIRECT_PREFIX.length());
			Expression viewNameExpr = (Expression)fromStringTo(Expression.class).execute(viewName);
			return new ApplicationViewSelector(viewNameExpr, RedirectType.FLOW_EXECUTION);
		}
		else if (encodedView.startsWith(EXTERNAL_REDIRECT_PREFIX)) {
			String externalUrl = encodedView.substring(EXTERNAL_REDIRECT_PREFIX.length());
			Expression urlExpr = (Expression)fromStringTo(Expression.class).execute(externalUrl);
			return new ExternalRedirectSelector(urlExpr);
		}
		else if (encodedView.startsWith(FLOW_REDIRECT_PREFIX)) {
			String flowRedirect = encodedView.substring(FLOW_REDIRECT_PREFIX.length());
			Expression redirectExpr = (Expression)fromStringTo(Expression.class).execute(flowRedirect);
			return new FlowRedirectSelector(redirectExpr);
		}
		else if (encodedView.startsWith(BEAN_PREFIX)) {
			String id = encodedView.substring(BEAN_PREFIX.length());
			return flowServiceLocator.getViewSelector(id);
		}
		else {
			Expression viewNameExpr = (Expression)fromStringTo(Expression.class).execute(encodedView);
			return new ApplicationViewSelector(viewNameExpr);
		}
	}

	protected ViewSelector convertEncodedEndStateSelector(String encodedView) {
		if (encodedView.startsWith(REDIRECT_PREFIX)) {
			String externalUrl = encodedView.substring(REDIRECT_PREFIX.length());
			Expression urlExpr = (Expression)fromStringTo(Expression.class).execute(externalUrl);
			return new ExternalRedirectSelector(urlExpr);
		}
		else if (encodedView.startsWith(EXTERNAL_REDIRECT_PREFIX)) {
			String externalUrl = encodedView.substring(EXTERNAL_REDIRECT_PREFIX.length());
			Expression urlExpr = (Expression)fromStringTo(Expression.class).execute(externalUrl);
			return new ExternalRedirectSelector(urlExpr);
		}
		else if (encodedView.startsWith(FLOW_REDIRECT_PREFIX)) {
			String flowRedirect = encodedView.substring(FLOW_REDIRECT_PREFIX.length());
			Expression redirectExpr = (Expression)fromStringTo(Expression.class).execute(flowRedirect);
			return new FlowRedirectSelector(redirectExpr);
		}
		else if (encodedView.startsWith(BEAN_PREFIX)) {
			String id = encodedView.substring(BEAN_PREFIX.length());
			return flowServiceLocator.getViewSelector(id);
		}
		else {
			Expression viewNameExpr = (Expression)fromStringTo(Expression.class).execute(encodedView);
			return new ApplicationViewSelector(viewNameExpr);
		}
	}
}