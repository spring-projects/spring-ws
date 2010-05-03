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

package org.springframework.ws.server.endpoint.adapter.method;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.MethodParameter;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.xml.transform.StringSource;

/** @author Arjen Poutsma */
public class SourcePayloadMethodProcessorTest extends AbstractPayloadMethodProcessorTestCase {

    @Override
    protected AbstractPayloadSourceMethodProcessor createProcessor() {
        return new SourcePayloadMethodProcessor();
    }

    @Override
    protected MethodParameter[] createSupportedParameters() throws NoSuchMethodException {
        return new MethodParameter[] {
         new MethodParameter(getClass().getMethod("source", Source.class), 0),
         new MethodParameter(getClass().getMethod("dom", DOMSource.class), 0),
         new MethodParameter(getClass().getMethod("sax", SAXSource.class), 0),
        new MethodParameter(getClass().getMethod("stream", StreamSource.class), 0)};
    }

    @Override
    protected MethodParameter[] createSupportedReturnTypes() throws NoSuchMethodException {
        return new MethodParameter[] {
         new MethodParameter(getClass().getMethod("source", Source.class), -1),
         new MethodParameter(getClass().getMethod("dom", DOMSource.class), -1),
         new MethodParameter(getClass().getMethod("sax", SAXSource.class), -1),
        new MethodParameter(getClass().getMethod("stream", StreamSource.class), -1)};
    }

    @Override
    protected Object getReturnValue(MethodParameter returnType) throws Exception {
        return new StringSource(XML);
    }

    @ResponsePayload
    public Source source(@RequestPayload Source source) {
        return source;
    }

    @ResponsePayload
    public DOMSource dom(@RequestPayload DOMSource source) {
        return source;
    }

    @ResponsePayload
    public SAXSource sax(@RequestPayload SAXSource source) {
        return source;
    }

    @ResponsePayload
    public StreamSource stream(@RequestPayload StreamSource source) {
        return source;
    }
}
