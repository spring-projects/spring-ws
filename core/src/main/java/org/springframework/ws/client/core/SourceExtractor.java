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

package org.springframework.ws.client.core;

import java.io.IOException;
import javax.xml.transform.Source;

/**
 * Callback interface for extracting a result object from a {@link javax.xml.transform.Source} instance.
 * <p/>
 * Used for output object creation in {@link WebServiceTemplate}. Alternatively, output sources can also be returned to
 * client code as-is. In case of a source as execution result, you will almost always want to implement a
 * <code>SourceExtractor</code>, to be able to read the message content in a managed fashion, with the connection still
 * open while reading the message.
 * <p/>
 * Implementations of this interface perform the actual work of extracting results, but don't need to worry about
 * exception handling, or resource handling.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.client.core.WebServiceTemplate
 */
public interface SourceExtractor {

    /**
     * Process the data in the given <code>Source</code>, creating a corresponding result object.
     *
     * @param source the message payload to extract data from
     * @return an arbitrary result object, or <code>null</code> if none (the extractor will typically be stateful in the
     *         latter case)
     * @throws IOException in case of I/O errors
     */
    Object extractData(Source source) throws IOException;

}
