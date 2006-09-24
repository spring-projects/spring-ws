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
package org.springframework.webflow.execution.repository.support;

import org.springframework.util.Assert;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.SharedMap;

/**
 * A {@link SharedMapLocator} that returns the external context session map.
 * @author Keith Donald
 */
class SessionMapLocator implements SharedMapLocator {
	public SharedMap getMap(ExternalContext context) {
		Assert.notNull(context, "The external context is required");
		return context.getSessionMap().getSharedMap();
	}
	
	public boolean requiresRebindOnChange() {
		return true;
	}
}