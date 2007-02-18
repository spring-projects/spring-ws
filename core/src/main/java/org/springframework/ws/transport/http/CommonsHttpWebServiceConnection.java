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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of the {@link WebServiceConnection} that uses Jakarta Commons HttpClient.
 *
 * @author Arjen Poutsma
 */
public class CommonsHttpWebServiceConnection implements WebServiceConnection {

    private final HttpClient httpClient;

    private final PostMethod postMethod;

    public CommonsHttpWebServiceConnection(HttpClient httpClient, PostMethod postMethod) {
        Assert.notNull(httpClient, "httpClient must not be null");
        Assert.notNull(postMethod, "postMethod must not be null");
        this.httpClient = httpClient;
        this.postMethod = postMethod;
    }

    public void close() throws IOException {
        postMethod.releaseConnection();
    }

    public void sendAndReceive(MessageContext messageContext) throws IOException {
        writeRequestMessage(messageContext.getRequest());
        executePostMethod();
        validateResponse(postMethod);
        readResponse(postMethod, messageContext);
    }

    private void writeRequestMessage(WebServiceMessage message) throws IOException {
        CommonsHttpTransportOutputStream tos = new CommonsHttpTransportOutputStream(postMethod);
        message.writeTo(tos);
        tos.flush();
        // calling close() causes the RequestEntity to be set on the PostMethod
        tos.close();
    }

    private void executePostMethod() throws IOException {
        httpClient.executeMethod(postMethod);
    }

    private void validateResponse(PostMethod postMethod) throws IOException {
        int statusCode = postMethod.getStatusCode();
        if (statusCode != HttpStatus.SC_INTERNAL_SERVER_ERROR && statusCode / 100 != 2) {
            throw new HttpTransportException("Did not receive successful HTTP response: status code = " + statusCode +
                    ", status message = [" + postMethod.getStatusText() + "]");
        }

    }

    private void readResponse(PostMethod postMethod, MessageContext messageContext) throws IOException {
        if (postMethod.getStatusCode() == HttpStatus.SC_NO_CONTENT || postMethod.getResponseContentLength() == 0) {
            return;
        }
        messageContext.readResponse(new CommonsHttpTransportInputStream(postMethod));
    }

}
