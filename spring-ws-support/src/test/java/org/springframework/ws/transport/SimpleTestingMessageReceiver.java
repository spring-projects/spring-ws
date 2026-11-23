/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport;

import javax.xml.transform.Transformer;

import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.transform.TransformerObjectSupport;

public class SimpleTestingMessageReceiver extends TransformerObjectSupport implements WebServiceMessageReceiver {

	@Override
	public void receive(MessageContext messageContext) throws Exception {

		Assert.notNull(messageContext, "MessageContext is null");

		this.logger.info("Received " + messageContext.getRequest());

		Transformer transformer = createTransformer();
		transformer.transform(messageContext.getRequest().getPayloadSource(),
				messageContext.getResponse().getPayloadResult());
	}

}
