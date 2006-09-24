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

import java.util.Iterator;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.springframework.web.util.WebUtils;
import org.springframework.webflow.CollectionUtils;
import org.springframework.webflow.SharedMap;
import org.springframework.webflow.context.StringKeyedMapAdapter;

/**
 * Map backed by the Portlet session, for accessing session scoped attributes in
 * a Portlet environment.
 * 
 * Supports setting 
 * 
 * @author Keith Donald
 */
public class PortletSessionMap extends StringKeyedMapAdapter implements SharedMap {

	/**
	 * The wrapped portlet request, providing access to the session.
	 */
	private PortletRequest request;

	/**
	 * The scope to access in the session, either APPLICATION (global) or PORTLET. 
	 */
	private int scope;
	
	/**
	 * Create a new map wrapping the session associated with given request.
	 */
	public PortletSessionMap(PortletRequest request, int scope) {
		this.request = request;
		this.scope = scope;
	}

	/**
	 * Return the portlet session associated with the wrapped request, or null
	 * if no such session exits.
	 */
	private PortletSession getSession() {
		return request.getPortletSession(false);
	}

	protected Object getAttribute(String key) {
		PortletSession session = getSession();
		return (session == null) ? null : session.getAttribute(key, scope);
	}

	protected void setAttribute(String key, Object value) {
		request.getPortletSession(true).setAttribute(key, value, scope);
	}

	protected void removeAttribute(String key) {
		PortletSession session = getSession();
		if (session != null) {
			session.removeAttribute(key, scope);
		}
	}

	protected Iterator getAttributeNames() {
		PortletSession session = getSession();
		return session == null ? CollectionUtils.EMPTY_ITERATOR : CollectionUtils.iterator(session.getAttributeNames(scope)); 
	}

	public Object getMutex() {
		PortletSession session = request.getPortletSession(true);
		Object mutex = session.getAttribute(WebUtils.SESSION_MUTEX_ATTRIBUTE);
		return mutex != null ? mutex : session;
	}
}