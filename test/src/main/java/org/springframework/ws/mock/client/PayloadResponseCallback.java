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

package org.springframework.ws.mock.client;

import java.io.IOException;
import java.net.URI;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.springframework.ws.WebServiceMessage;

/**
 * Implementation of {@link ResponseCreator} that writes a {@link Source} response.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class PayloadResponseCallback extends AbstractResponseCreator<WebServiceMessage> {

    private final Source payload;

    PayloadResponseCallback(Source payload) {
        this.payload = payload;
    }

    @Override
    protected void doWithResponse(URI uri, WebServiceMessage request, WebServiceMessage response) throws IOException {
        try {
            transform(payload, response.getPayloadResult());
        }
        catch (TransformerException ex) {
            throw new AssertionError("Could not transform response payload to message: " + ex.getMessage());
        }
    }
}
