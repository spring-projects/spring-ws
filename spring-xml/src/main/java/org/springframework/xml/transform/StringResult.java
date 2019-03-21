/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.xml.transform;

import java.io.StringWriter;
import javax.xml.transform.stream.StreamResult;

/**
 * Convenient subclass of {@code StreamResult} that writes to a {@code StringWriter}. The resulting string can
 * be retrieved via {@code toString()}.
 *
 * @author Arjen Poutsma
 * @see #toString()
 * @since 1.0.0
 */
public class StringResult extends StreamResult {

	public StringResult() {
		super(new StringWriter());
	}

	/** Returns the written XML as a string. */
	public String toString() {
		return getWriter().toString();
	}

}
