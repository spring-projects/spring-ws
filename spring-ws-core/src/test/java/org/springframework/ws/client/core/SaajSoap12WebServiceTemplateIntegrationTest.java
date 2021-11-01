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

package org.springframework.ws.client.core;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;

import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

/**
 * @author Arjen Poutsma
 */
public class SaajSoap12WebServiceTemplateIntegrationTest extends AbstractSoap12WebServiceTemplateIntegrationTestCase {

	@Override
	public SoapMessageFactory createMessageFactory() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		return new SaajSoapMessageFactory(messageFactory);
	}
}
