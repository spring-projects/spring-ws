/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.support.creator;

import static org.springframework.ws.test.support.AssertionErrors.*;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Implementation of {@link WebServiceMessageCreator} that creates a request based on a {@link Source}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class PayloadMessageCreator extends AbstractMessageCreator {

	private final Source payload;

	private TransformerHelper transformerHelper = new TransformerHelper();

	/**
	 * Creates a new instance of the {@code PayloadMessageCreator} with the given payload source.
	 *
	 * @param payload the payload source
	 */
	public PayloadMessageCreator(Source payload) {
		Assert.notNull(payload, "'payload' must not be null");
		this.payload = payload;
	}

	@Override
	protected void doWithMessage(WebServiceMessage message) throws IOException {
		try {
			transformerHelper.transform(payload, message.getPayloadResult());
		} catch (TransformerException ex) {
			fail("Could not transform request payload to message: " + ex.getMessage());
		}
	}
}
