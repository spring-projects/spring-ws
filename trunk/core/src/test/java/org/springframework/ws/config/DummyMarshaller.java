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

package org.springframework.ws.config;

import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;

/**
 * @author Arjen Poutsma
 * @since 1.1.0
 */
public class DummyMarshaller implements Marshaller, Unmarshaller {

    public void marshal(Object graph, Result result) throws XmlMappingException, IOException {
        throw new UnsupportedOperationException();
    }

    public boolean supports(Class clazz) {
        throw new UnsupportedOperationException();
    }

    public Object unmarshal(Source source) throws XmlMappingException, IOException {
        throw new UnsupportedOperationException();
    }
}
