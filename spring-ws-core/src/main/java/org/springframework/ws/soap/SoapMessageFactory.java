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

import java.io.IOException;
import java.io.InputStream;

import org.springframework.ws.WebServiceMessageFactory;

/**
 * Sub-interface of {@link WebServiceMessageFactory} which contains SOAP-specific properties and methods.
 *
 * <p>The {@code soapVersion} property can be used to indicate the SOAP version of the factory. By default, the
 * version is {@link SoapVersion#SOAP_11}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface SoapMessageFactory extends WebServiceMessageFactory {

	/**
	 * Sets the SOAP Version used by this factory.
	 *
	 * @param version the version constant
	 * @see SoapVersion#SOAP_11
	 * @see SoapVersion#SOAP_12
	 */
	void setSoapVersion(SoapVersion version);

	/**
	 * Creates a new, empty {@code SoapMessage}.
	 *
	 * @return the empty message
	 */
	@Override
	SoapMessage createWebServiceMessage();

	/**
	 * Reads a {@link SoapMessage} from the given input stream.
	 *
	 * <p>If the given stream is an instance of {@link org.springframework.ws.transport.TransportInputStream
	 * TransportInputStream}, the headers will be read from the request.
	 *
	 * @param inputStream the input stream to read the message from
	 * @return the created message
	 * @throws java.io.IOException if an I/O exception occurs
	 */
	@Override
	SoapMessage createWebServiceMessage(InputStream inputStream) throws IOException;

}
