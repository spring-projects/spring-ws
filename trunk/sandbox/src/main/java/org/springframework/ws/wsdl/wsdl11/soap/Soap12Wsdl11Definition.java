/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.wsdl.wsdl11.soap;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class Soap12Wsdl11Definition extends AbstractSoapWsdl11Definition {

    public static final String SOAP_12_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/soap12/";

    public static final String SOAP_12_NAMESPACE_PREFIX = "soap12";

    protected String getSoapNamespaceUri() {
        return SOAP_12_NAMESPACE_URI;
    }

    protected String getSoapNamespacePrefix() {
        return SOAP_12_NAMESPACE_PREFIX;
    }

    protected String getBindingSuffix() {
        return "Soap12";
    }
}