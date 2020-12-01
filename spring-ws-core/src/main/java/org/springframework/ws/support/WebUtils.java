/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ws.support;

/**
 * Miscellaneous utilities for web applications. Used by various framework classes. NOTE: These are the parts of
 * org.springframework.web.util.WebUtils deprecated in Spring Framework 5.
 *
 * @author Greg Turnquist
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 2.4.0
 */
public abstract class WebUtils {

	/**
	 * Extract the URL filename from the given request URL path. Correctly resolves nested paths such as
	 * "/products/view.html" as well.
	 * 
	 * @param urlPath the request URL path (e.g. "/index.html")
	 * @return the extracted URI filename (e.g. "index")
	 */
	public static String extractFilenameFromUrlPath(String urlPath) {
		String filename = extractFullFilenameFromUrlPath(urlPath);
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex != -1) {
			filename = filename.substring(0, dotIndex);
		}
		return filename;
	}

	/**
	 * Extract the full URL filename (including file extension) from the given request URL path. Correctly resolve nested
	 * paths such as "/products/view.html" and remove any path and or query parameters.
	 * 
	 * @param urlPath the request URL path (e.g. "/products/index.html")
	 * @return the extracted URI filename (e.g. "index.html")
	 */
	public static String extractFullFilenameFromUrlPath(String urlPath) {
		int end = urlPath.indexOf('?');
		if (end == -1) {
			end = urlPath.indexOf('#');
			if (end == -1) {
				end = urlPath.length();
			}
		}
		int begin = urlPath.lastIndexOf('/', end) + 1;
		int paramIndex = urlPath.indexOf(';', begin);
		end = (paramIndex != -1 && paramIndex < end ? paramIndex : end);
		return urlPath.substring(begin, end);
	}

}
