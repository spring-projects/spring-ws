/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.webflow.context.portlet;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.ParameterMap;
import org.springframework.webflow.SharedAttributeMap;

/**
 * Provides contextual information about a portlet environment that has
 * interacted with SWF.
 * 
 * @author Keith Donald
 */
public class PortletExternalContext implements ExternalContext {

	/**
	 * The context.
	 */
	private PortletContext context;

	/**
	 * The request.
	 */
	private PortletRequest request;

	/**
	 * The response.
	 */
	private PortletResponse response;

	/**
	 * Create an external context wrapping given portlet request and response.
	 * @param request the portlet request
	 * @param response the portlet response
	 */
	public PortletExternalContext(PortletContext context, PortletRequest request, PortletResponse response) {
		this.context = context;
		this.request = request;
		this.response = response;
	}

	public String getContextPath() {
		return request.getContextPath();
	}

	public String getDispatcherPath() {
		return null;
	}

	public String getRequestPathInfo() {
		return null;
	}

	public ParameterMap getRequestParameterMap() {
		return new ParameterMap(new PortletRequestParameterMap(request));
	}

	public AttributeMap getRequestMap() {
		return new AttributeMap(new PortletRequestMap(request));
	}

	public SharedAttributeMap getSessionMap() {
		return new SharedAttributeMap(new PortletSessionMap(request, PortletSession.PORTLET_SCOPE));
	}

	public SharedAttributeMap getGlobalSessionMap() {
		return new SharedAttributeMap(new PortletSessionMap(request, PortletSession.APPLICATION_SCOPE));
	}

	public SharedAttributeMap getApplicationMap() {
		return new SharedAttributeMap(new PortletContextMap(context));
	}

	/**
	 * Returns the wrapped portlet context.
	 */
	public PortletContext getContext() {
		return context;
	}

	/**
	 * Returns the wrapped portlet request.
	 */
	public PortletRequest getRequest() {
		return request;
	}

	/**
	 * Returns the wrapped portlet response.
	 */
	public PortletResponse getResponse() {
		return response;
	}

	public String toString() {
		return new ToStringCreator(this).append("requestParameterMap", getRequestParameterMap()).toString();
	}
}