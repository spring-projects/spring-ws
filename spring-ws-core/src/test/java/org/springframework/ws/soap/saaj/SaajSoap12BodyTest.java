/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.saaj;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.soap12.AbstractSoap12BodyTestCase;

public class SaajSoap12BodyTest extends AbstractSoap12BodyTestCase {

	@Override
	protected SoapBody createSoapBody() throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		SOAPMessage saajMessage = messageFactory.createMessage();
		return new SaajSoap12Body(saajMessage.getSOAPBody());
	}

}
