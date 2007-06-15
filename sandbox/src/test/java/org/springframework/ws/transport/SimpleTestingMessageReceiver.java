/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.transport;

import javax.xml.transform.Transformer;

import org.springframework.ws.context.MessageContext;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.springframework.util.Assert;

public class SimpleTestingMessageReceiver extends TransformerObjectSupport implements WebServiceMessageReceiver {

    public void receive(MessageContext messageContext) throws Exception {
        Assert.notNull(messageContext, "MessageContext is null");
        logger.info("Received message");
        Transformer transformer = createTransformer();
        transformer.transform(messageContext.getRequest().getPayloadSource(),
                messageContext.getResponse().getPayloadResult());
    }
}
