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

package org.springframework.ws.test.server;

import java.io.IOException;
import javax.xml.transform.Source;

import org.springframework.core.io.Resource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.support.PayloadDiffMatcher;
import org.springframework.xml.transform.ResourceSource;

/**
 * Factory methods for {@link ResponseMatcher} classes. Typically used to provide input for {@link
 * ResponseActions#andExpect(ResponseMatcher)}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class ResponseMatchers {

    private ResponseMatchers() {
    }

    /**
     * Expects any response.
     *
     * @return the response matcher
     */
    public static ResponseMatcher anything() {
        return new ResponseMatcher() {
            public void match(WebServiceMessage response) {
            }
        };
    }

    /**
     * Expects the given {@link Source} XML payload.
     *
     * @param payload the XML payload
     * @return the response matcher
     */
    public static ResponseMatcher payload(Source payload) {
        final PayloadDiffMatcher matcher = new PayloadDiffMatcher(payload);
        return new ResponseMatcher() {
            public void match(WebServiceMessage response) throws IOException {
                matcher.match(response);
            }
        };
    }

    /**
     * Expects the given {@link Resource} XML payload.
     *
     * @param payload the XML payload
     * @return the response matcher
     */
    public ResponseMatcher payload(Resource payload) throws IOException {
        return payload(new ResourceSource(payload));
    }

}
