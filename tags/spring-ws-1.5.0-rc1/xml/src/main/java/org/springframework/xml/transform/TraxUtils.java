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

package org.springframework.xml.transform;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;

import org.springframework.util.Assert;
import org.springframework.xml.JaxpVersion;

/**
 * Convenient utility methods for dealing with TrAX.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class TraxUtils {

    /**
     * Indicates whether the given {@link Source} is a StAX Source.
     *
     * @return <code>true</code> if <code>source</code> is a Spring-WS {@link StaxSource} or JAXP 1.4 {@link
     *         StAXSource}; <code>false</code> otherwise.
     */
    public static boolean isStaxSource(Source source) {
        if (source instanceof StaxSource) {
            return true;
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.isStaxSource(source);
        }
        else {
            return false;
        }
    }

    /**
     * Indicates whether the given {@link Result} is a StAX Result.
     *
     * @return <code>true</code> if <code>result</code> is a Spring-WS {@link StaxResult} or JAXP 1.4 {@link
     *         StAXResult}; <code>false</code> otherwise.
     */
    public static boolean isStaxResult(Result result) {
        if (result instanceof StaxResult) {
            return true;
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.isStaxResult(result);
        }
        else {
            return false;
        }
    }

    /**
     * Returns the {@link XMLStreamReader} for the given StAX Source.
     *
     * @param source a Spring-WS {@link StaxSource} or {@link StAXSource}
     * @return the {@link XMLStreamReader}
     * @throws IllegalArgumentException if <code>source</code> is neither a Spring-WS {@link StaxSource} or {@link
     *                                  StAXSource}
     */
    public static XMLStreamReader getXMLStreamReader(Source source) {
        if (source instanceof StaxSource) {
            return ((StaxSource) source).getXMLStreamReader();
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.getXMLStreamReader(source);
        }
        else {
            throw new IllegalArgumentException("Source '" + source + "' is neither StaxSource nor StAXSource");
        }
    }

    /**
     * Returns the {@link XMLEventReader} for the given StAX Source.
     *
     * @param source a Spring-WS {@link StaxSource} or {@link StAXSource}
     * @return the {@link XMLEventReader}
     * @throws IllegalArgumentException if <code>source</code> is neither a Spring-WS {@link StaxSource} or {@link
     *                                  StAXSource}
     */
    public static XMLEventReader getXMLEventReader(Source source) {
        if (source instanceof StaxSource) {
            return ((StaxSource) source).getXMLEventReader();
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.getXMLEventReader(source);
        }
        else {
            throw new IllegalArgumentException("Source '" + source + "' is neither StaxSource nor StAXSource");
        }
    }

    /**
     * Returns the {@link XMLStreamWriter} for the given StAX Result.
     *
     * @param result a Spring-WS {@link StaxResult} or {@link StAXResult}
     * @return the {@link XMLStreamReader}
     * @throws IllegalArgumentException if <code>source</code> is neither a Spring-WS {@link StaxResult} or {@link
     *                                  StAXResult}
     */
    public static XMLStreamWriter getXMLStreamWriter(Result result) {
        if (result instanceof StaxResult) {
            return ((StaxResult) result).getXMLStreamWriter();
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.getXMLStreamWriter(result);
        }
        else {
            throw new IllegalArgumentException("Result '" + result + "' is neither StaxResult nor StAXResult");
        }
    }

    /**
     * Returns the {@link XMLEventWriter} for the given StAX Result.
     *
     * @param result a Spring-WS {@link StaxResult} or {@link StAXResult}
     * @return the {@link XMLStreamReader}
     * @throws IllegalArgumentException if <code>source</code> is neither a Spring-WS {@link StaxResult} or {@link
     *                                  StAXResult}
     */
    public static XMLEventWriter getXMLEventWriter(Result result) {
        if (result instanceof StaxResult) {
            return ((StaxResult) result).getXMLEventWriter();
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.getXMLEventWriter(result);
        }
        else {
            throw new IllegalArgumentException("Result '" + result + "' is neither StaxResult nor StAXResult");
        }
    }

    /**
     * Creates a StAX {@link Source} for the given {@link XMLStreamReader}. Returns a {@link StAXSource} under JAXP 1.4
     * or higher, or a {@link StaxSource} otherwise.
     *
     * @param streamReader the StAX stream reader
     * @return a source wrapping <code>streamReader</code>
     */
    public static Source createStaxSource(XMLStreamReader streamReader) {
        if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.createStaxSource(streamReader);
        }
        else {
            return new StaxSource(streamReader);
        }
    }

    /**
     * Creates a StAX {@link Source} for the given {@link XMLEventReader}. Returns a {@link StAXSource} under JAXP 1.4
     * or higher, or a {@link StaxSource} otherwise.
     *
     * @param eventReader the StAX event reader
     * @return a source wrapping <code>streamReader</code>
     * @throws XMLStreamException in case of StAX errors
     */
    public static Source createStaxSource(XMLEventReader eventReader) throws XMLStreamException {
        if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.createStaxSource(eventReader);
        }
        else {
            return new StaxSource(eventReader);
        }
    }

    /** Inner class to avoid a static JAXP 1.4 dependency. */
    private static class Jaxp14StaxHandler {

        private static Source createStaxSource(XMLStreamReader streamReader) {
            return new StAXSource(streamReader);
        }

        private static Source createStaxSource(XMLEventReader eventReader) throws XMLStreamException {
            return new StAXSource(eventReader);
        }

        private static boolean isStaxSource(Source source) {
            return source instanceof StAXSource;
        }

        private static boolean isStaxResult(Result result) {
            return result instanceof StAXResult;
        }

        private static XMLStreamReader getXMLStreamReader(Source source) {
            Assert.isInstanceOf(StAXSource.class, source);
            return ((StAXSource) source).getXMLStreamReader();
        }

        private static XMLEventReader getXMLEventReader(Source source) {
            Assert.isInstanceOf(StAXSource.class, source);
            return ((StAXSource) source).getXMLEventReader();
        }

        private static XMLStreamWriter getXMLStreamWriter(Result result) {
            Assert.isInstanceOf(StAXResult.class, result);
            return ((StAXResult) result).getXMLStreamWriter();
        }

        private static XMLEventWriter getXMLEventWriter(Result result) {
            Assert.isInstanceOf(StAXResult.class, result);
            return ((StAXResult) result).getXMLEventWriter();
        }
    }


}
