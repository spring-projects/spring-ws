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
package org.springframework.webflow.context.servlet;

import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.webflow.CollectionUtils;
import org.springframework.webflow.context.StringKeyedMapAdapter;

/**
 * Map backed by the Servlet HTTP request parameter map, for accessing request
 * parameters. Also provides support for multi-part requests, providing
 * transparent access to the request "fileMap" as a request parameter entry.
 * 
 * @author Keith Donald
 */
public class HttpServletRequestParameterMap extends StringKeyedMapAdapter {

	/**
	 * The wrapped http request.
	 */
	private HttpServletRequest request;

	/**
	 * Create a new map wrapping the parameters of given request.
	 */
	public HttpServletRequestParameterMap(HttpServletRequest request) {
		this.request = request;
	}

	protected Object getAttribute(String key) {
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
			Object data = multipartRequest.getFileMap().get(key);
			if (data != null) {
				return data;
			}
		}
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
		throw new UnsupportedOperationException("HttpServletRequest parameter maps are immutable");
	}

	protected void removeAttribute(String key) {
		throw new UnsupportedOperationException("HttpServletRequest parameter maps are immutable");
	}

	protected Iterator getAttributeNames() {
		Enumeration parameterNames = request.getParameterNames();
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
			return new MultiPartIterator(multipartRequest.getFileMap().keySet().iterator(), parameterNames);
		}
		else {
			return CollectionUtils.iterator(parameterNames);
		}
	}

	/**
	 * A enumeration that combines elements in the multipart map with that of
	 * the request parameter map.
	 * @author Keith Donald
	 */
	private static class MultiPartIterator implements Iterator {

		private Iterator fileMapNames;

		private Enumeration parameterNames;

		public MultiPartIterator(Iterator fileMapNames, Enumeration parameterNames) {
			this.fileMapNames = fileMapNames;
			this.parameterNames = parameterNames;
		}

		public boolean hasNext() {
			return fileMapNames.hasNext() || parameterNames.hasMoreElements();
		}

		public Object next() {
			if (fileMapNames.hasNext()) {
				return fileMapNames.next();
			}
			else {
				return parameterNames.nextElement();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException("Remove not supported");
		}		
	}
}