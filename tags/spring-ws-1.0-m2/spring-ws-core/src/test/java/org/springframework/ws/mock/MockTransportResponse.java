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

package org.springframework.ws.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.springframework.ws.transport.TransportResponse;

/**
 * Mock implementation of the <code>TransportResponse</code> interface.
 *
 * @author Arjen Poutsma
 */
public class MockTransportResponse implements TransportResponse {

    private Properties headers = new Properties();

    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public void addHeader(String name, String value) {
        String currentValue = headers.getProperty(name);
        if (currentValue != null) {
            value = currentValue + "," + value;
        }
        headers.setProperty(name, value);
    }

    public Properties getHeaders() {
        return headers;
    }

    public String getContents() {
        try {
            return new String(outputStream.toByteArray(), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public OutputStream getOutputStream() throws IOException {
        return outputStream;
    }
}
