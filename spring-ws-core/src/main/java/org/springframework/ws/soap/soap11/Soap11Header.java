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

package org.springframework.ws.soap.soap11;

import java.util.Iterator;

import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;

/**
 * Subinterface of {@code SoapHeader} that exposes SOAP 1.1 functionality.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface Soap11Header extends SoapHeader {

	/**
	 * Returns an {@code Iterator} over all the {@link SoapHeaderElement header elements} that should be processed for the
	 * given actors. Headers target to the "next" actor or role will always be included.
	 *
	 * @param actors an array of actors to search for
	 * @return an iterator over all the header elements that contain the specified actors
	 * @throws SoapHeaderException if the headers cannot be returned
	 * @see SoapHeaderElement
	 */
	Iterator<SoapHeaderElement> examineHeaderElementsToProcess(String[] actors) throws SoapHeaderException;

}
