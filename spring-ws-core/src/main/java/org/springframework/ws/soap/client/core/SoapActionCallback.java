/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.client.core;

import java.io.IOException;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapMessage;

/**
 * {@link WebServiceMessageCallback} implementation that sets the SOAP Action header on
 * the message.
 * <p>
 * A usage example with {@link org.springframework.ws.client.core.WebServiceTemplate}:
 * <pre><code class='java'>
 * WebServiceTemplate template = new WebServiceTemplate(messageFactory);
 * Result result = new DOMResult();
 * template.sendSourceAndReceiveToResult(new StringSource("&lt;content xmlns=\"http://tempuri.org\"/&gt;"),
 * 		new SoapActionCallback("http://tempuri.org/SOAPAction"), result);</code></pre>
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class SoapActionCallback implements WebServiceMessageCallback {

	private final String soapAction;

	/** Create a new {@code SoapActionCallback} with the given string SOAPAction. */
	public SoapActionCallback(String soapAction) {
		if (!StringUtils.hasText(soapAction)) {
			soapAction = "\"\"";
		}
		this.soapAction = soapAction;
	}

	@Override
	public void doWithMessage(WebServiceMessage message) throws IOException {
		Assert.isInstanceOf(SoapMessage.class, message);
		SoapMessage soapMessage = (SoapMessage) message;
		soapMessage.setSoapAction(this.soapAction);
	}

}
