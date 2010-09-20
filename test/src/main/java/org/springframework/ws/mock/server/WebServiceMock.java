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

package org.springframework.ws.mock.server;

import java.io.IOException;
import javax.xml.transform.Source;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.ResourceSource;

/**
 * @author Arjen Poutsma
 */
public abstract class WebServiceMock {


    public static ResponseActions receiveMessage(RequestCreator requestCreator) {
        return null;
    }

    public static RequestCreator withPayload(Source payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadRequestCreator(payload);
    }

    public static RequestCreator withPayload(Resource payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadRequestCreator(createResourceSource(payload));
    }


    public static ResponseMatcher payload(Source payload) {
        return null;
    }

    /**
     * Expects any request.
     *
     * @return the request matcher
     */
    public static ResponseMatcher anything() {
        return new ResponseMatcher() {
            public void match(WebServiceMessage response) throws IOException, AssertionError {
            }
        };
    }



    private static ResourceSource createResourceSource(Resource resource) {
        try {
            return new ResourceSource(resource);
        }
        catch (IOException ex) {
            throw new IllegalArgumentException(resource + " could not be opened", ex);
        }
    }

}
