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
package org.springframework.webflow.executor.jsf;

import org.easymock.AbstractMatcher;

/**
 * Special EasyMock arguments matcher for arguments of type JsfExternalContext,
 * since that class does not have either <code>equals</code> or
 * <code>hashCode</code>.
 * 
 * @author Ulrik Sandberg
 */
public class JsfExternalContextMatcher extends AbstractMatcher {

	/*
	 * @see org.easymock.AbstractMatcher#argumentMatches(java.lang.Object,
	 * java.lang.Object)
	 */
	protected boolean argumentMatches(Object arg0, Object arg1) {
		if (arg0 instanceof JsfExternalContext && arg1 instanceof JsfExternalContext) {
			JsfExternalContext first = (JsfExternalContext)arg0;
			JsfExternalContext second = (JsfExternalContext)arg1;
			return super.argumentMatches(first.getActionId(), second.getActionId())
					&& super.argumentMatches(first.getOutcome(), second.getOutcome())
					&& super.argumentMatches(first.getFacesContext(), second.getFacesContext());
		}
		else {
			return super.argumentMatches(arg0, arg1);
		}
	}
}
