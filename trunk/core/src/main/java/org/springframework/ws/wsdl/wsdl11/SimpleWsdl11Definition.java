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

package org.springframework.ws.wsdl.wsdl11;

import java.io.IOException;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.wsdl.WsdlDefinitionException;
import org.springframework.xml.sax.SaxUtils;

/**
 * Default implementation of the <code>Wsdl11Definition</code> interface. Allows a WSDL to be set by the
 * <code>wsdl</code> property.
 *
 * @author Arjen Poutsma
 * @see #setWsdl(org.springframework.core.io.Resource)
 */
public class SimpleWsdl11Definition implements Wsdl11Definition, InitializingBean {

    private Resource wsdlResource;

    /**
     * Constructs a new <code>SimpleWsdl11Definition</code>. Calling <code>setWsdl</code> is required.
     *
     * @see #setWsdl(org.springframework.core.io.Resource)
     */
    public SimpleWsdl11Definition() {
    }

    /** Constructs a new <code>SimpleWsdl11Definition</code> with the given resource. */
    public SimpleWsdl11Definition(Resource wsdlResource) {
        Assert.notNull(wsdlResource, "wsdlResource must not be null");
        this.wsdlResource = wsdlResource;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(wsdlResource, "wsdlResource is required");
        Assert.isTrue(wsdlResource.exists(), "wsdl \"" + wsdlResource + "\" does not exit");
    }

    public Source getSource() {
        try {
            return new SAXSource(SaxUtils.createInputSource(wsdlResource));
        }
        catch (IOException ex) {
            throw new WsdlDefinitionException("Could not create source from " + wsdlResource, ex);
        }
    }

    public void setWsdl(Resource wsdlResource) {
        this.wsdlResource = wsdlResource;
    }
}