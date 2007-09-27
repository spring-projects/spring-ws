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

package org.springframework.ws.mime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Abstract implementation of the {@link MimeMessage} interface. Contains convenient default implementations.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractMimeMessage implements MimeMessage {

    public final Attachment addAttachment(String contentId, File file) throws AttachmentException {
        Assert.hasLength(contentId, "contentId must not be empty");
        Assert.notNull(file, "File must not be null");
        DataHandler dataHandler = new DataHandler(new FileDataSource(file));
        return addAttachment(contentId, dataHandler);
    }

    public final Attachment addAttachment(String contentId, InputStreamSource inputStreamSource, String contentType) {
        Assert.hasLength(contentId, "contentId must not be empty");
        Assert.notNull(inputStreamSource, "InputStreamSource must not be null");
        if (inputStreamSource instanceof Resource && ((Resource) inputStreamSource).isOpen()) {
            throw new IllegalArgumentException("Passed-in Resource contains an open stream: invalid argument. " +
                    "MIME requires an InputStreamSource that creates a fresh stream for every call.");
        }
        DataHandler dataHandler = new DataHandler(new InputStreamSourceDataSource(inputStreamSource, contentType));
        return addAttachment(contentId, dataHandler);
    }

    /**
     * Activation framework <code>DataSource</code> that wraps a Spring <code>InputStreamSource</code>.
     *
     * @author Arjen Poutsma
     * @since 1.0.0
     */
    private static class InputStreamSourceDataSource implements DataSource {

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
            if (inputStreamSource instanceof Resource) {
                Resource resource = (Resource) inputStreamSource;
                return resource.getFilename();
            }
            else {
                throw new UnsupportedOperationException("DataSource name not available");
            }
        }

    }

}
