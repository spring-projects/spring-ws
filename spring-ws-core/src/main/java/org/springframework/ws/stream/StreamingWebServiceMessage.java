/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.stream;

import org.springframework.ws.WebServiceMessage;

/**
 * Sub-interface of {@link WebServiceMessage} that allows for setting a streaming payload.
 *
 * @author Arjen Poutsma
 * @since 2.0
 * @see StreamingPayload
 */
public interface StreamingWebServiceMessage extends WebServiceMessage {

	/**
	 * Sets the streaming payload for this message.
	 * @param payload the streaming payload
	 */
	void setStreamingPayload(StreamingPayload payload);

}
