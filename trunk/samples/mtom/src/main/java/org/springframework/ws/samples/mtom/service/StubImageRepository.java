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

package org.springframework.ws.samples.mtom.service;

import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** @author Arjen Poutsma */
public class StubImageRepository implements ImageRepository {

    private static final Log logger = LogFactory.getLog(StubImageRepository.class);

    private Map<String, Image> images = new HashMap<String, Image>();

    public Image readImage(String name) throws IOException {
        logger.info("Streaming image " + name);
        return images.get(name);
    }

    public void storeImage(String name, Image image) throws IOException {
        logger.info("Storing image " + name + " with [" + image.getHeight(null) + "," + image.getWidth(null) + "]");
        images.put(name, image);
    }
}
