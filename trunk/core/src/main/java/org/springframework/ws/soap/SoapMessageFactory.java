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

package org.springframework.ws.soap;

import org.springframework.ws.WebServiceMessageFactory;

/**
 * Sub-interface of {@link WebServiceMessageFactory} which contains SOAP-specific properties and methods.
 * <p/>
 * The <code>soapVersion</code> property can be used to indicate the SOAP version of the factory. By default, the
 * version is {@link SoapVersion#SOAP_11}.
 *
 * @author Arjen Poutsma
 */
public interface SoapMessageFactory extends WebServiceMessageFactory {

    /**
     * Sets the SOAP Version used by this factory.
     *
     * @param version the version constant
     * @see SoapVersion#SOAP_11
     * @see SoapVersion#SOAP_12
     */
    void setSoapVersion(SoapVersion version);

}
