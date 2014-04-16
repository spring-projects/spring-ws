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

package org.springframework.xml.transform;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;

import org.springframework.util.Assert;

/**
 * Convenient subclass of {@code StreamSource} that reads from a {@code StringReader}. The string to be read
 * can be set via the constructor.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class StringSource extends StreamSource {

    private final String content;

    /**
     * Initializes a new instance of the {@code StringSource} with the given string content.
     *
     * @param content the content
     */
    public StringSource(String content) {
        Assert.notNull(content, "'content' must not be null");
        this.content = content;
    }

    @Override
    public Reader getReader() {
        return new StringReader(content);
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setInputStream(InputStream inputStream) {
        throw new UnsupportedOperationException("setInputStream is not supported");
    }

    /**
     * Returns {@code null}.
     *
     * @return {@code null}
     */
    @Override
    public InputStream getInputStream() {
        return null;
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setReader(Reader reader) {
        throw new UnsupportedOperationException("setReader is not supported");
    }

    @Override
    public String toString() {
        return content;
    }
}
