/*
 * Copyright 2006 the original author or authors.
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
import java.net.URL;
import java.net.URLConnection;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.MessageSender;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * <code>MessageSender</code> implementation that uses standard J2SE facilities to execute POST requests, without
 * support for HTTP authentication or advanced configuration options.
 *
 * @author Arjen Poutsma
 */
public class HttpUrlConnectionMessageSender implements MessageSender {

    private URL url;

    private static final String HTTP_METHOD_POST = "POST";

    /**
     * Returns the url used by this message sender.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets the url used by this message sender.
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    public void send(MessageContext messageContext) throws IOException {
        HttpURLConnection connection = openConnection();
        prepareConnection(connection);
        writeRequestMessage(connection, messageContext.getRequest());
        validateResponse(connection);
        readResponse(connection, messageContext);
        connection.disconnect();
    }

    protected HttpURLConnection openConnection() throws IOException {
        URLConnection con = getUrl().openConnection();
        if (!(con instanceof HttpURLConnection)) {
            throw new IOException("URL [" + getUrl() + "] is not an HTTP URL");
        }
        return (HttpURLConnection) con;
    }

    protected void prepareConnection(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod(HTTP_METHOD_POST);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
    }

    protected void writeRequestMessage(HttpURLConnection connection, WebServiceMessage message) throws IOException {
        TransportOutputStream tos = null;
        try {
            tos = new HttpUrlConnectionTransportOutputStream(connection);
            message.writeTo(tos);
            tos.flush();
        }
        finally {
            if (tos != null) {
                tos.close();
            }
        }
    }

    protected void validateResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_INTERNAL_ERROR && responseCode / 100 != 2) {
            throw new IOException("Did not receive successful HTTP response: status code = " + responseCode +
                    ", status message = [" + connection.getResponseMessage() + "]");
        }
    }

    protected void readResponse(HttpURLConnection connection, MessageContext messageContext) throws IOException {
        if (connection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT || connection.getContentLength() == 0) {
            return;
        }
        TransportInputStream tis = null;
        try {
            tis = new HttpUrlConnectionTransportInputStream(connection);
            messageContext.readResponse(tis);
        }
        finally {
            if (tis != null) {
                tis.close();
            }
        }
    }
}
