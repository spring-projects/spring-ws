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

package org.springframework.ws.mock.client;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.TransformerObjectSupport;

import org.custommonkey.xmlunit.Diff;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.custommonkey.xmlunit.XMLAssert.fail;

/**
 * Implementation of {@link RequestMatcher} based on XMLUnit's {@link Diff}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
abstract class DiffMatcher extends TransformerObjectSupport implements RequestMatcher {

    public final void match(WebServiceMessage request) throws IOException, AssertionError {
        try {
            Diff diff = createDiff(request);
            assertXMLEqual(diff, true);
        }
        catch (IOException ex) {
            throw ex;
        }
        catch (Exception ex) {
            fail("Could not create Diff: " + ex.getMessage());
        }
    }

    /**
     * Creates a {@link Diff} for the given request message.
     *
     * @param request the request message
     * @return the diff
     * @throws Exception in case of errors
     */
    protected abstract Diff createDiff(WebServiceMessage request) throws Exception;

}
