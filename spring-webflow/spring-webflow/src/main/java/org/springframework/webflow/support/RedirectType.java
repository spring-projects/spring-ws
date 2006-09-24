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
package org.springframework.webflow.support;

import org.springframework.core.enums.StaticLabeledEnum;
import org.springframework.webflow.ViewSelection;

/**
 * An enumeration for the different logical "redirect types" supported for
 * redirecting to a {@link ApplicationView} selection made by a paused flow
 * execution.
 * 
 * @author Keith Donald
 */
public abstract class RedirectType extends StaticLabeledEnum {

	/**
	 * The flow execution redirect type. Requests a redirect to a bookmarkable
	 * flow execution URL that refreshes the state of a conversation at a point
	 * in time.
	 */
	public static final RedirectType FLOW_EXECUTION = new RedirectType(0, "Flow Execution") {
		public ViewSelection select() {
			return FlowExecutionRedirect.INSTANCE;
		}
	};

	private RedirectType(int code, String label) {
		super(code, label);
	}

	/**
	 * Factory method that makes the view selection for this redirect type.
	 * @return the corresponding view selection
	 */
	public abstract ViewSelection select();
}