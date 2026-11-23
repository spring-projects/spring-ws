/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.config;

import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;

public class DummyMarshaller implements Marshaller, Unmarshaller {

	@Override
	public void marshal(Object graph, Result result) throws XmlMappingException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean supports(Class<?> clazz) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object unmarshal(Source source) throws XmlMappingException, IOException {
		throw new UnsupportedOperationException();
	}

}
