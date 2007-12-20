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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.springframework.util.FileCopyUtils;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class DummyHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write("Hello world!");
        writer.flush();

        byte[] buf = os.toByteArray();
        exchange.sendResponseHeaders(HttpTransportConstants.STATUS_OK, buf.length);
        FileCopyUtils.copy(buf, exchange.getResponseBody());
        exchange.close();
    }
}
