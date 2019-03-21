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

package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.soap12.AbstractSoap12HeaderTestCase;

public class AxiomSoap12HeaderTest extends AbstractSoap12HeaderTestCase {

	@Override
	protected SoapHeader createSoapHeader() throws Exception {
		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP12Factory();
		AxiomSoapMessage axiomSoapMessage = new AxiomSoapMessage(axiomFactory);
		return axiomSoapMessage.getSoapHeader();
	}

	@Override
	public void testGetResult() throws Exception {
	}
}
