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
package org.springframework.webflow;

/**
 * A generically typed flow execution statistics interface for use by management
 * clients. These stats would typically be exported for management via JMX.
 * References to strongly-typed web flow classes (e.g Flow, State) should not go
 * here -- put them in the FlowExecutionContext subinterface.
 * 
 * @see org.springframework.webflow.FlowExecutionContext
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionStatistics {

	/**
	 * Returns a display string suitable for logging/printing in a console
	 * containing info about this executing flow.
	 * @return the flow execution caption
	 */
	public String getCaption();

	/**
	 * Is the flow execution active?
	 * @return true if active, false if flow execution has terminated
	 */
	public boolean isActive();

}