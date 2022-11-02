/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.axiom;

import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.soap12.AbstractSoap12MessageFactoryTestCase;

public class AxiomSoap12MessageFactoryTest extends AbstractSoap12MessageFactoryTestCase {

	@Override
	protected WebServiceMessageFactory createMessageFactory() throws Exception {

		AxiomSoapMessageFactory factory = new AxiomSoapMessageFactory();
		factory.setSoapVersion(SoapVersion.SOAP_12);
		factory.afterPropertiesSet();

		return factory;
	}

	@Override
	public void doTestCreateSoapMessageIllFormedXml() {

		// Axiom parses the contents of XML lazily, so it will not throw an InvalidXmlException when a message is parsed
		throw new InvalidXmlException(null, null);
	}
}
