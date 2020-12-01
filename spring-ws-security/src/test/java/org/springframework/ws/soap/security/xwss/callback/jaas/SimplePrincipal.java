/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.security.xwss.callback.jaas;

import java.security.Principal;

public final class SimplePrincipal implements Principal {

	private String name;

	public SimplePrincipal() {
		name = "";
	}

	public SimplePrincipal(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public boolean equals(Object o) {

		if (!(o instanceof SimplePrincipal)) {
			return false;
		}
		return name.equals(((SimplePrincipal) o).name);
	}

	public String toString() {
		return name;
	}
}
