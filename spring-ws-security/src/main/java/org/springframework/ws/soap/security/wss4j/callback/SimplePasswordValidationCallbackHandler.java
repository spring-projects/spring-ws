/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.security.wss4j.callback;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Simple callback handler that validates passwords against a in-memory {@code Properties} object. Password
 * validation is done on a case-sensitive basis.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @see #setUsers(java.util.Properties)
 * @since 1.5.0
 */
public class SimplePasswordValidationCallbackHandler extends AbstractWsPasswordCallbackHandler
		implements InitializingBean {

	private Map<String, String > users = new HashMap<String, String>();

	/** Sets the users to validate against. Property names are usernames, property values are passwords. */
	public void setUsers(Properties users) {
		for (Map.Entry<Object, Object> entry : users.entrySet()) {
			if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
				this.users.put((String) entry.getKey(), (String) entry.getValue());
			}
		}
	}

	public void setUsersMap(Map<String, String> users) {
		this.users = users;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(users, "users is required");
	}
}