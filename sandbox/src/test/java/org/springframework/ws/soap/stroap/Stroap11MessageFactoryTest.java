/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.stroap;

import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageFactoryTestCase;

public class Stroap11MessageFactoryTest extends AbstractSoap11MessageFactoryTestCase {

	@Override
	protected WebServiceMessageFactory createMessageFactory() throws Exception {
		return new StroapMessageFactory();
	}

	@Override
	public void testCreateSoapMessageMtom() throws Exception {
	}

	@Override
	public void testCreateSoapMessageSwA() throws Exception {
	}

	@Override
	public void testCreateSoapMessageMtomWeirdStartInfo() throws Exception {
	}
}