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

package org.springframework.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.StringSource;

/**
 * Mock implementation of the <code>WebServiceMessage</code> interface.
 *
 * @author Arjen Poutsma
 */
public class MockWebServiceMessage implements WebServiceMessage {

    private final StringBuffer content;

    private boolean fault = false;

    private String faultReason;

    public MockWebServiceMessage() {
        content = new StringBuffer();
    }

    public MockWebServiceMessage(Source source) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        content = new StringBuffer();
        transformer.transform(source, getPayloadResult());
    }

    public MockWebServiceMessage(Resource resource) throws IOException, TransformerException {
        this(new SAXSource(SaxUtils.createInputSource(resource)));
    }

    public MockWebServiceMessage(StringBuffer content) {
        this.content = content;
    }

    public MockWebServiceMessage(String content) {
        this.content = new StringBuffer(content);
    }

    public String getPayloadAsString() {
        return content.toString();
    }

    public void setPayload(InputStreamSource inputStreamSource) throws IOException {
        InputStream is = null;
        try {
            is = inputStreamSource.getInputStream();
            Reader reader = new InputStreamReader(is, "UTF-8");
            content.replace(0, content.length(), FileCopyUtils.copyToString(reader));
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public void setPayload(String content) {
        this.content.replace(0, this.content.length(), content);
    }

    public Result getPayloadResult() {
        content.setLength(0);
        return new StreamResult(new StringBufferWriter());
    }

    public Source getPayloadSource() {
        return new StringSource(content.toString());
    }

    public boolean hasFault() {
        return fault;
    }

    public void setFault(boolean fault) {
        this.fault = fault;
    }

    public String getFaultReason() {
        return faultReason;
    }

    public void setFaultReason(String faultReason) {
        this.faultReason = faultReason;
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        PrintWriter writer = new PrintWriter(outputStream);
        writer.write(content.toString());
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("MockWebServiceMessage {");
        buffer.append(content);
        buffer.append('}');
        return buffer.toString();
    }

    private class StringBufferWriter extends Writer {

        private StringBufferWriter() {
            super(content);
        }

        public void write(String str) {
            content.append(str);
        }

        public void write(int c) {
            content.append((char) c);
        }

        public void write(String str, int off, int len) {
            content.append(str.substring(off, off + len));
        }

        public void close() throws IOException {
        }

        public void flush() {
        }

        public void write(char cbuf[], int off, int len) {
            if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0) {
                throw new IndexOutOfBoundsException();
            }
            else if (len == 0) {
                return;
            }
            content.append(cbuf, off, len);
        }
    }
}
