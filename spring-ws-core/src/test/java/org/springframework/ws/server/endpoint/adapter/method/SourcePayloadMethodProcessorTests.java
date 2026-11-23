/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.adapter.method;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.MethodParameter;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.xml.transform.StringSource;

/**
 * @author Arjen Poutsma
 */
class SourcePayloadMethodProcessorTests extends AbstractPayloadMethodProcessorTests {

	@Override
	protected AbstractPayloadSourceMethodProcessor createProcessor() {
		return new SourcePayloadMethodProcessor();
	}

	@Override
	protected MethodParameter[] createSupportedParameters() throws NoSuchMethodException {

		return new MethodParameter[] { new MethodParameter(getClass().getMethod("source", Source.class), 0),
				new MethodParameter(getClass().getMethod("dom", DOMSource.class), 0),
				new MethodParameter(getClass().getMethod("sax", SAXSource.class), 0),
				new MethodParameter(getClass().getMethod("stream", StreamSource.class), 0),
				new MethodParameter(getClass().getMethod("stax", StAXSource.class), 0) };
	}

	@Override
	protected MethodParameter[] createSupportedReturnTypes() throws NoSuchMethodException {

		return new MethodParameter[] { new MethodParameter(getClass().getMethod("source", Source.class), -1),
				new MethodParameter(getClass().getMethod("dom", DOMSource.class), -1),
				new MethodParameter(getClass().getMethod("sax", SAXSource.class), -1),
				new MethodParameter(getClass().getMethod("stream", StreamSource.class), -1),
				new MethodParameter(getClass().getMethod("stax", StAXSource.class), -1) };
	}

	@Override
	protected Object getReturnValue(MethodParameter returnType) {
		return new StringSource(XML);
	}

	@ResponsePayload
	public Source source(@RequestPayload Source source) {
		return source;
	}

	@ResponsePayload
	public DOMSource dom(@RequestPayload DOMSource source) {
		return source;
	}

	@ResponsePayload
	public SAXSource sax(@RequestPayload SAXSource source) {
		return source;
	}

	@ResponsePayload
	public StreamSource stream(@RequestPayload StreamSource source) {
		return source;
	}

	@ResponsePayload
	public StAXSource stax(@RequestPayload StAXSource source) {
		return source;
	}

}
