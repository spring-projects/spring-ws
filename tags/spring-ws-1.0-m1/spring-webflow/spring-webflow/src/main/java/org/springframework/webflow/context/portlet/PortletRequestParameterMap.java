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

import org.springframework.webflow.CollectionUtils;
import org.springframework.webflow.context.StringKeyedMapAdapter;

/**
 * Map backed by the Portlet request parameter map, for accessing request local
 * portlet parameters.
 * 
 * @author Keith Donald
 */
public class PortletRequestParameterMap extends StringKeyedMapAdapter {

	/**
	 * The wrapped portlet request.
	 */
	private PortletRequest request;

	/**
	 * Create a new map wrapping the parameters of given portlet request.
	 */
	public PortletRequestParameterMap(PortletRequest request) {
		this.request = request;
	}

	protected Object getAttribute(String key) {
		String[] parameters = request.getParameterValues(key);
		if (parameters == null) {
			return null;
		} else if (parameters.length == 1) {
			return parameters[0];
		} else {
			return parameters;
		}
	}

	protected void setAttribute(String key, Object value) {
		throw new UnsupportedOperationException("PortletRequest parameter maps are immutable");
	}

	protected void removeAttribute(String key) {
		throw new UnsupportedOperationException("PortletRequest parameter maps are immutable");
	}

	protected Iterator getAttributeNames() {
		return CollectionUtils.iterator(request.getParameterNames());
	}
}