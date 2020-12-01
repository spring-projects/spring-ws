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

package org.springframework.ws.soap.addressing.version;

import java.net.URI;

import javax.xml.namespace.QName;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.core.EndpointReference;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;

/**
 * Implements WS-Addressing 1.0 (May 2006). This version of the specification is used by Microsoft's Windows
 * Communication Foundation (WCF), and supported by Axis 1 and 2.
 *
 * @author Arjen Poutsma
 * @see <a href="http://www.w3.org/TR/2006/REC-ws-addr-core-20060509">Web Services Addressing, August 2004</a>
 * @since 1.5.0
 */

public class Addressing10 extends AbstractAddressingVersion {

	private static final String NAMESPACE_URI = "http://www.w3.org/2005/08/addressing";

	@Override
	public void addAddressingHeaders(SoapMessage message, MessageAddressingProperties map) {
		Assert.notNull(map.getAction(), "'Action' is required");
		super.addAddressingHeaders(message, map);
	}

	@Override
	public boolean hasRequiredProperties(MessageAddressingProperties map) {
		if (map.getAction() == null) {
			return false;
		}
		if (map.getReplyTo() != null || map.getFaultTo() != null) {
			return map.getMessageId() != null;
		}
		return true;

	}

	@Override
	protected String getNamespaceUri() {
		return NAMESPACE_URI;
	}

	@Override
	protected QName getReferencePropertiesName() {
		return null;
	}

	@Override
	protected URI getDefaultTo() {
		return getAnonymous();
	}

	@Override
	protected EndpointReference getDefaultReplyTo(EndpointReference from) {
		return new EndpointReference(getAnonymous());
	}

	@Override
	protected final URI getAnonymous() {
		return URI.create(NAMESPACE_URI + "/anonymous");
	}

	@Override
	protected final URI getNone() {
		return URI.create(NAMESPACE_URI + "/none");
	}

	@Override
	protected final QName getMessageAddressingHeaderRequiredFaultSubcode() {
		return new QName(NAMESPACE_URI, "MessageAddressingHeaderRequired", getNamespacePrefix());
	}

	@Override
	protected final String getMessageAddressingHeaderRequiredFaultReason() {
		return "A required header representing a Message Addressing Property is not present";
	}

	@Override
	protected QName getInvalidAddressingHeaderFaultSubcode() {
		return new QName(NAMESPACE_URI, "InvalidAddressingHeader", getNamespacePrefix());
	}

	@Override
	protected String getInvalidAddressingHeaderFaultReason() {
		return "A header representing a Message Addressing Property is not valid and the message cannot be processed";
	}

	public String toString() {
		return "WS-Addressing 1.0";
	}
}
