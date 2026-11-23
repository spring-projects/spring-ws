/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.ws.WebServiceMessageFactory;

/**
 * Sub-interface of {@link WebServiceMessageFactory} which contains SOAP-specific
 * properties and methods.
 * <p>
 * The {@code soapVersion} property can be used to indicate the SOAP version of the
 * factory. By default, the version is {@link SoapVersion#SOAP_11}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface SoapMessageFactory extends WebServiceMessageFactory {

	/**
	 * Sets the SOAP Version used by this factory.
	 * @param version the version constant
	 * @see SoapVersion#SOAP_11
	 * @see SoapVersion#SOAP_12
	 */
	void setSoapVersion(SoapVersion version);

	/**
	 * Creates a new, empty {@code SoapMessage}.
	 * @return the empty message
	 */
	@Override
	SoapMessage createWebServiceMessage();

	/**
	 * Reads a {@link SoapMessage} from the given input stream.
	 * <p>
	 * If the given stream is an instance of
	 * {@link org.springframework.ws.transport.TransportInputStream TransportInputStream},
	 * the headers will be read from the request.
	 * @param inputStream the input stream to read the message from
	 * @return the created message
	 * @throws java.io.IOException if an I/O exception occurs
	 */
	@Override
	SoapMessage createWebServiceMessage(InputStream inputStream) throws IOException;

}
