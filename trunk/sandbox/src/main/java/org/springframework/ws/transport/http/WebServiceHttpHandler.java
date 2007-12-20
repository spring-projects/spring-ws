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

package org.springframework.ws.transport.http;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.SimpleWebServiceMessageReceiverObjectSupport;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class WebServiceHttpHandler extends SimpleWebServiceMessageReceiverObjectSupport implements HttpHandler {

    public void handle(HttpExchange httpExchange) throws IOException {
        if ("POST".equals(httpExchange.getRequestMethod())) {
            WebServiceConnection connection = new HttpExchangeConnection(httpExchange);
            try {
                handleConnection(connection);
            }
            catch (Exception ex) {
                logger.error(ex);
            }
        }
        else {
            httpExchange.sendResponseHeaders(HttpTransportConstants.STATUS_METHOD_NOT_ALLOWED, -1);
            httpExchange.close();
        }
    }
}
