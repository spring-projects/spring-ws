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

package org.springframework.ws.test.server;

import java.io.IOException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Implementation of {@link org.springframework.ws.test.server.RequestCreator} that creates a request based on a {@link
 * javax.xml.transform.Source}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class PayloadRequestCreator extends AbstractRequestCreator {

    private final Source payload;

    private TransformerHelper transformerHelper = new TransformerHelper();

    PayloadRequestCreator(Source payload) {
        this.payload = payload;
    }

    @Override
    protected void doWithRequest(WebServiceMessage request) throws IOException {
        try {
            transformerHelper.transform(payload, request.getPayloadResult());
        }
        catch (TransformerException ex) {
            throw new AssertionError("Could not transform request payload to message: " + ex.getMessage());
        }
    }
}
