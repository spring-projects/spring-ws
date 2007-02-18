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
import java.net.URLConnection;

import org.springframework.ws.transport.WebServiceConnection;

/**
 * <code>WebServiceMessageSender</code> implementation that uses standard J2SE facilities to execute POST requests,
 * without support for HTTP authentication or advanced configuration options.
 * <p/>
 * Designed for easy subclassing, customizing specific template methods. However, consider {@link
 * org.springframework.ws.transport.http.CommonsHttpMessageSender} for more sophisticated needs: the J2SE
 * <code>HttpURLConnection</code> is rather limited in its capabilities.
 *
 * @author Arjen Poutsma
 * @see java.net.HttpURLConnection
 */
public class HttpUrlConnectionMessageSender extends AbstractHttpWebServiceMessageSender {

    public WebServiceConnection createConnection() throws IOException {
        URLConnection con = getUrl().openConnection();
        if (!(con instanceof HttpURLConnection)) {
            throw new HttpTransportException("URL [" + getUrl() + "] is not an HTTP URL");
        }
        return new HttpUrlConnectionWebServiceConnection((HttpURLConnection) con);
    }

}
