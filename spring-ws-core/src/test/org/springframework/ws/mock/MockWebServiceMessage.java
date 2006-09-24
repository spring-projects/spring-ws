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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;

import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.StringSource;

/**
 * Mock implementation of the <code>WebServiceMessage</code> interface.
 *
 * @author Arjen Poutsma
 */
public class MockWebServiceMessage implements WebServiceMessage {

    private final StringBuffer content;

    public MockWebServiceMessage() {
        this.content = new StringBuffer();
    }

    public MockWebServiceMessage(String content) {
        this.content = new StringBuffer(content);
    }

    public MockWebServiceMessage(StringBuffer content) {
        this.content = content;
    }

    public Source getPayloadSource() {
        return new StringSource(content.toString());
    }

    public Result getPayloadResult() {
        return new StreamResult(new StringBufferWriter());
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        PrintWriter writer = new PrintWriter(outputStream);
        writer.append(content);
    }

    public String getPayloadAsString() {
        return content.toString();
    }

    public void setPayload(String content) {
        this.content.replace(0, this.content.length(), content);
    }

    private class StringBufferWriter extends Writer {

        private StringBufferWriter() {
            super(content);
        }

        public void write(int c) {
            content.append((char) c);
        }

        public void write(char cbuf[], int off, int len) {
            if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            }
            else if (len == 0) {
                return;
            }
            content.append(cbuf, off, len);
        }

        public void write(String str) {
            content.append(str);
        }

        public void write(String str, int off, int len) {
            content.append(str.substring(off, off + len));
        }

        public void flush() {
        }

        public void close() throws IOException {
        }

    }
}
