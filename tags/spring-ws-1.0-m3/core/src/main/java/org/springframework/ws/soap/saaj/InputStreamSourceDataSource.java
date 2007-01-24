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

package org.springframework.ws.soap.saaj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;

import org.springframework.core.io.InputStreamSource;

/**
 * Activation framework <code>DataSource</code> that wraps a Spring <code>InputStreamSource</code>.
 *
 * @author Arjen Poutsma
 */
class InputStreamSourceDataSource implements DataSource {

    private final InputStreamSource inputStreamSource;

    private final String contentType;

    public InputStreamSourceDataSource(InputStreamSource inputStreamSource, String contentType) {
        this.inputStreamSource = inputStreamSource;
        this.contentType = contentType;
    }

    public InputStream getInputStream() throws IOException {
        return inputStreamSource.getInputStream();
    }

    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException("Read-only javax.activation.DataSource");
    }

    public String getContentType() {
        return contentType;
    }

    public String getName() {
        throw new UnsupportedOperationException("DataSource name not available");
    }

}
