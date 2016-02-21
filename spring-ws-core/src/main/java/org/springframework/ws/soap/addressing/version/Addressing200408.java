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

package org.springframework.ws.soap.addressing.version;

import java.net.URI;
import javax.xml.namespace.QName;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.core.EndpointReference;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;

/**
 * Implements the August 2004 edition of the WS-Addressing specification. This version of the specification is used by
 * Microsoft's Web Services Enhancements (WSE) 3.0, and supported by Axis 1 and 2, and XFire.
 *
 * @author Arjen Poutsma
 * @see <a href="http://www.w3.org/Submission/2004/SUBM-ws-addressing-20040810/">Web Services Addressing, August
 *		2004</a>
 * @since 1.5.0
 */
public class Addressing200408 extends AbstractAddressingVersion {

	private static final String NAMESPACE_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

	@Override
	public void addAddressingHeaders(SoapMessage message, MessageAddressingProperties map) {
		Assert.notNull(map.getAction(), "'Action' is required");
		Assert.notNull(map.getTo(), "'To' is required");
		super.addAddressingHeaders(message, map);
	}

	@Override
	public boolean hasRequiredProperties(MessageAddressingProperties map, Object endpoint){
		if (map.getTo() == null) {
			return false;
		}
		if (map.getAction() == null) {
			return false;
		}
		if (isMessageIdRequired(map, endpoint)) {
			return map.getMessageId() != null;
		}
		return true;
	}

	@Override
	protected final URI getAnonymous() {
		return URI.create(NAMESPACE_URI + "/role/anonymous");
	}

	@Override
	protected final String getInvalidAddressingHeaderFaultReason() {
		return "A message information header is not valid and the message cannot be processed.";
	}

	@Override
	protected final QName getInvalidAddressingHeaderFaultSubcode() {
		return new QName(NAMESPACE_URI, "InvalidMessageInformationHeader",
				getNamespacePrefix());
	}

	@Override
	protected final String getMessageAddressingHeaderRequiredFaultReason() {
		return "A required message information header, To, MessageID, or Action, is not present.";
	}

	@Override
	protected final QName getMessageAddressingHeaderRequiredFaultSubcode() {
		return new QName(NAMESPACE_URI, "MessageInformationHeaderRequired",
				getNamespacePrefix());
	}

	@Override
	protected final String getNamespaceUri() {
		return NAMESPACE_URI;
	}

	@Override
	protected URI getDefaultTo() {
		return null;
	}

	@Override
	protected final EndpointReference getDefaultReplyTo(EndpointReference from) {
		return from;
	}

	@Override
	protected final URI getNone() {
		return null;
	}

	public String toString() {
		return "WS-Addressing August 2004";
	}
}
