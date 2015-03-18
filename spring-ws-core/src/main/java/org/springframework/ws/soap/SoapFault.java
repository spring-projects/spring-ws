/*
 * Copyright 2006-2007 the original author or authors.
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

package org.springframework.ws.soap;

import javax.xml.namespace.QName;

/**
 * Represents the {@code Fault} element in the body of a SOAP message.
 *
 * <p>A fault consists of a {@link #getFaultCode() fault code}, {@link #getFaultActorOrRole fault string/reason}, and
 * {@link #getFaultActorOrRole() role}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface SoapFault extends SoapElement {

	/** Returns the fault code. */
	QName getFaultCode();

	/**
	 * Returns the fault string or reason. For SOAP 1.1, this returns the fault string. For SOAP 1.2, this returns the
	 * fault reason for the default locale.
	 */
	String getFaultStringOrReason();

	/** Returns the fault actor or role. For SOAP 1.1, this returns the actor. For SOAP 1.2, this returns the role. */
	String getFaultActorOrRole();

	/** Sets the fault actor. For SOAP 1.1, this sets the actor. For SOAP 1.2, this sets the role. */
	void setFaultActorOrRole(String faultActor);

	/**
	 * Returns the optional detail element for this {@code SoapFault}.
	 *
	 * @return a fault detail
	 */
	SoapFaultDetail getFaultDetail();

	/**
	 * Creates an optional {@code SoapFaultDetail} object and assigns it to this fault.
	 *
	 * @return the created detail
	 */
	SoapFaultDetail addFaultDetail();
}
