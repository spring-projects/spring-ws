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

package org.springframework.xml.transform;

import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;

/**
 * Convenient subclass of <code>StreamSource</code> that reads from a <code>StringReader</code>. The string to be read
 * can be set via the constructor.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class StringSource extends StreamSource {

    /**
     * Initializes a new instance of the <code>StringSource</code> with the given string content.
     *
     * @param content the content
     */
    public StringSource(String content) {
        super(new StringReader(content));
    }

    /**
     * Initializes a new instance of the <code>StringSource</code> with the given string content and system id.
     *
     * @param content  the content
     * @param systemId a string that conforms to the URI syntax
     */
    public StringSource(String content, String systemId) {
        super(new StringReader(content), systemId);
    }

}
