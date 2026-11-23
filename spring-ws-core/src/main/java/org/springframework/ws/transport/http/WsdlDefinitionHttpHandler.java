/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.wsdl.WsdlDefinition;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * {@link HttpHandler} implementation for WSDL documents.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class WsdlDefinitionHttpHandler extends TransformerObjectSupport implements HttpHandler, InitializingBean {

	private static final String CONTENT_TYPE = "text/xml";

	@SuppressWarnings("NullAway.Init")
	private WsdlDefinition definition;

	public WsdlDefinitionHttpHandler() {
	}

	public WsdlDefinitionHttpHandler(WsdlDefinition definition) {
		this.definition = definition;
	}

	public void setDefinition(WsdlDefinition definition) {
		this.definition = definition;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.definition, "'definition' is required");
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try (httpExchange) {
			if (HttpTransportConstants.METHOD_GET.equals(httpExchange.getRequestMethod())) {
				Headers headers = httpExchange.getResponseHeaders();
				headers.set(HttpTransportConstants.HEADER_CONTENT_TYPE, CONTENT_TYPE);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				transform(this.definition.getSource(), new StreamResult(os));
				byte[] buf = os.toByteArray();
				httpExchange.sendResponseHeaders(HttpTransportConstants.STATUS_OK, buf.length);
				FileCopyUtils.copy(buf, httpExchange.getResponseBody());
			}
			else {
				httpExchange.sendResponseHeaders(HttpTransportConstants.STATUS_METHOD_NOT_ALLOWED, -1);
			}
		}
		catch (TransformerException ex) {
			this.logger.error(ex, ex);
		}
	}

}
