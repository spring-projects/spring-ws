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

package org.springframework.ws.mock.support;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

import static org.springframework.ws.mock.support.Assert.assertTrue;
import static org.springframework.ws.mock.support.Assert.fail;

/**
 * Implementation of {@link org.springframework.ws.mock.client.RequestMatcher} based on XMLUnit's {@link Diff}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class DiffMatcher {

    static {
        XMLUnit.setIgnoreWhitespace(true);
    }

    public final void match(WebServiceMessage request) throws IOException, AssertionError {
        try {
            Diff diff = createDiff(request);
            assertTrue("Messages are different, " + diff.toString(), diff.similar());
        }
        catch (IOException ex) {
            throw ex;
        }
        catch (Exception ex) {
            fail("Could not create Diff: " + ex.getMessage());
        }
    }

    /**
     * Creates a {@link org.custommonkey.xmlunit.Diff} for the given request message.
     *
     * @param request the request message
     * @return the diff
     * @throws Exception in case of errors
     */
    protected abstract Diff createDiff(WebServiceMessage request) throws Exception;

}
