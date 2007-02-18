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
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of the {@link WebServiceConnection} interface that uses a {@link HttpURLConnection}.
 *
 * @author Arjen Poutsma
 */
public class HttpUrlConnectionWebServiceConnection implements WebServiceConnection {

    private static final String HTTP_METHOD_POST = "POST";

    private final HttpURLConnection connection;

    /**
     * Creates a new instance of the <code>HttpUrlConnectionWebServiceConnection</code> with the given
     * <code>HttpURLConnection</code>.
     */
    public HttpUrlConnectionWebServiceConnection(HttpURLConnection connection) {
        Assert.notNull(connection, "connection must not be null");
        this.connection = connection;
    }

    public void close() {
        connection.disconnect();
    }

    public void sendAndReceive(MessageContext messageContext) throws IOException {
        prepareConnection();
        writeRequestMessage(messageContext.getRequest());
        validateResponse();
        readResponse(messageContext);
    }

    private void prepareConnection() throws ProtocolException {
        connection.setRequestMethod(HTTP_METHOD_POST);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
    }

    private void writeRequestMessage(WebServiceMessage message) throws IOException {
        TransportOutputStream tos = new HttpUrlConnectionTransportOutputStream(connection);
        message.writeTo(tos);
        tos.flush();
    }

    private void validateResponse() throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_INTERNAL_ERROR && responseCode / 100 != 2) {
            throw new HttpTransportException("Did not receive successful HTTP response: status code = " + responseCode +
                    ", status message = [" + connection.getResponseMessage() + "]");
        }
    }

    private void readResponse(MessageContext messageContext) throws IOException {
        if (connection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT || connection.getContentLength() == 0) {
            return;
        }
        messageContext.readResponse(new HttpUrlConnectionTransportInputStream(connection));
    }

}
