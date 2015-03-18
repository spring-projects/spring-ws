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

package org.springframework.ws.soap;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.ws.mime.AbstractMimeMessage;

/**
 * Abstract implementation of the {@link SoapMessage} interface. Contains convenient default implementations.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractSoapMessage extends AbstractMimeMessage implements SoapMessage {

	private SoapVersion version;

	/** Returns {@code getEnvelope().getBody()}. */
	@Override
	public final SoapBody getSoapBody() {
		return getEnvelope().getBody();
	}

	/** Returns {@code getEnvelope().getHeader()}. */
	@Override
	public final SoapHeader getSoapHeader() {
		return getEnvelope().getHeader();
	}

	/** Returns {@code getSoapBody().getPayloadSource()}. */
	@Override
	public final Source getPayloadSource() {
		return getSoapBody().getPayloadSource();
	}

	/** Returns {@code getSoapBody().getPayloadResult()}. */
	@Override
	public final Result getPayloadResult() {
		return getSoapBody().getPayloadResult();
	}

	/** Returns {@code getSoapBody().hasFault()}. */
	@Override
	public final boolean hasFault() {
		return getSoapBody().hasFault();
	}

	/** Returns {@code getSoapBody().getFault().getFaultCode()}. */
	@Override
	public final QName getFaultCode() {
		if (hasFault()) {
			return getSoapBody().getFault().getFaultCode();
		} else {
			return null;
		}
	}

	/** Returns {@code getSoapBody().getFault().getFaultStringOrReason()}. */
	@Override
	public final String getFaultReason() {
		if (hasFault()) {
			return getSoapBody().getFault().getFaultStringOrReason();
		}
		else {
			return null;
		}
	}

	@Override
	public SoapVersion getVersion() {
		if (version == null) {
			String envelopeNamespace = getEnvelope().getName().getNamespaceURI();
			if (SoapVersion.SOAP_11.getEnvelopeNamespaceUri().equals(envelopeNamespace)) {
				version = SoapVersion.SOAP_11;
			}
			else if (SoapVersion.SOAP_12.getEnvelopeNamespaceUri().equals(envelopeNamespace)) {
				version = SoapVersion.SOAP_12;
			}
			else {
				throw new IllegalStateException(
						"Unknown Envelope namespace uri '" + envelopeNamespace + "'. " + "Cannot deduce SoapVersion.");
			}
		}
		return version;
	}
}
