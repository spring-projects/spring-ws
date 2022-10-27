/*
 * Copyright 2005-2022 the original author or authors.
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
package org.springframework.ws.soap.axiom;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPModelBuilder;

/**
 * Utilities for building SOAP messages using Axiom 1.4.
 *
 * @author Greg Turnquist
 * @since 3.1
 */
final class Axiom14Utils {

	private Axiom14Utils() {
		throw new RuntimeException("Utility class not meant to be instantiated.");
	}

	static SOAPMessage getSOAPMessage(XMLInputFactory inputFactory, InputStream inputStream, String charSetEncoding,
			SOAPFactory soapFactory) throws XMLStreamException {
		XMLStreamReader reader = inputFactory.createXMLStreamReader(inputStream, charSetEncoding);
		SOAPModelBuilder builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(soapFactory.getMetaFactory(), reader);
		return builder.getSOAPMessage();
	}

	static SOAPMessage getSOAPMessage(Attachments attachments, SOAPFactory soapFactory, XMLStreamReader reader) {
		SOAPModelBuilder builder;
		if (MTOMConstants.SWA_TYPE.equals(attachments.getAttachmentSpecType())
				|| MTOMConstants.SWA_TYPE_12.equals(attachments.getAttachmentSpecType())) {
			builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(soapFactory.getMetaFactory(), reader);
		} else if (MTOMConstants.MTOM_TYPE.equals(attachments.getAttachmentSpecType())) {
			builder = OMXMLBuilderFactory.createSOAPModelBuilder(soapFactory.getMetaFactory(), attachments);
		} else {
			throw new RuntimeException("Unknown attachment type: [" + attachments.getAttachmentSpecType() + "]");
		}
		return builder.getSOAPMessage();
	}
}
