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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.soap.SOAPConstants;
import javax.xml.stream.events.StartElement;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.soap11.Soap11Header;

/**
 * @author Arjen Poutsma
 */
class Stroap11Header extends StroapHeader implements Soap11Header {

	Stroap11Header(StroapMessageFactory messageFactory) {
		super(messageFactory);
	}

	Stroap11Header(StartElement startElement, StroapMessageFactory messageFactory) {
		super(startElement, messageFactory);
	}

	public Iterator<SoapHeaderElement> examineHeaderElementsToProcess(String[] actors) {
		List<SoapHeaderElement> result = new LinkedList<SoapHeaderElement>();
		Iterator<SoapHeaderElement> iterator = examineAllHeaderElements();
		while (iterator.hasNext()) {
			SoapHeaderElement headerElement = iterator.next();
			String actor = headerElement.getActorOrRole();
			if (shouldProcess(actor, actors)) {
				result.add(headerElement);
			}
		}
		return result.iterator();
	}

	private boolean shouldProcess(String headerActor, String[] actors) {
		if (!StringUtils.hasLength(headerActor)) {
			return true;
		}
		if (SOAPConstants.URI_SOAP_ACTOR_NEXT.equals(headerActor)) {
			return true;
		}
		if (!ObjectUtils.isEmpty(actors)) {
			for (String actor : actors) {
				if (actor.equals(headerActor)) {
					return true;
				}
			}
		}
		return false;
	}

}
