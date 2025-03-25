/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ws.soap;

import javax.xml.namespace.QName;

/**
 * Represent the {@code Fault} element in the body of a SOAP message.
 * <p>
 * A fault consists of a {@linkplain #getFaultCode() fault code},
 * {@linkplain #getFaultStringOrReason() fault string/reason}, and
 * {@link #getFaultActorOrRole() role}.
 * <p>
 * A fault also can have a {@link SoapFaultDetail detail}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface SoapFault extends SoapElement {

	/**
	 * Return the type of fault.
	 */
	QName getFaultCode();

	/**
	 * Return a human-readable information about the nature of the fault. For SOAP 1.1,
	 * this returns the fault string. For SOAP 1.2, this returns the fault reason for the
	 * default locale.
	 */
	String getFaultStringOrReason();

	/**
	 * Return the optional fault actor or role. For SOAP 1.1, this returns the URI of the
	 * SOAP node that generated the fault. For SOAP 1.2, this returns the URI that
	 * identifies the role in which the node was operating at the point the fault
	 * occurred.
	 */
	String getFaultActorOrRole();

	/**
	 * Set the fault actor or role. For SOAP 1.1, this sets the actor. For SOAP 1.2, this
	 * sets the role.
	 */
	void setFaultActorOrRole(String faultActor);

	/**
	 * Return the optional {@linkplain SoapFaultDetail detail element} of this fault.
	 * @return a fault detail
	 */
	SoapFaultDetail getFaultDetail();

	/**
	 * Create a {@link SoapFaultDetail} and assign it to this fault.
	 * @return the created detail
	 */
	SoapFaultDetail addFaultDetail();

}
