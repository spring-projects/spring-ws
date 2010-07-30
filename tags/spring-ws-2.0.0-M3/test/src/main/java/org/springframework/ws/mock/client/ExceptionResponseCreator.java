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

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Implementation of {@link ResponseCreator} that responds by throwing either an {@link IOException} or a {@link
 * RuntimeException}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class ExceptionResponseCreator implements ResponseCreator<WebServiceMessage> {

    private final Exception exception;

    ExceptionResponseCreator(IOException exception) {
        this.exception = exception;
    }

    ExceptionResponseCreator(RuntimeException exception) {
        this.exception = exception;
    }

    public WebServiceMessage createResponse(URI uri,
                                            WebServiceMessage request,
                                            WebServiceMessageFactory<WebServiceMessage> factory) throws IOException {
        if (exception instanceof IOException) {
            throw (IOException) exception;
        }
        else {
            throw (RuntimeException) exception;
        }
    }
}
