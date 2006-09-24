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

package org.springframework.ws.soap;

/**
 * Represents the <code>Envelope</code> element in a SOAP message. The header contains the optional
 * <code>SoapHeader</code> and <code>SoapBody</code>.
 *
 * @author Arjen Poutsma
 */
public interface SoapEnvelope extends SoapElement {

    /**
     * Returns the <code>SoapHeader</code>. Returns <code>null</code> if no header is present.
     *
     * @return the <code>SoapHeader</code>, or <code>null</code>
     */
    SoapHeader getHeader();

    /**
     * Returns the <code>SoapBody</code>.
     *
     * @return the <code>SoapBody</code>
     */
    SoapBody getBody();

}
