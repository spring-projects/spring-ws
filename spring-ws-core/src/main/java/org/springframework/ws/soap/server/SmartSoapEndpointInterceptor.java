/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.server;

import org.springframework.ws.server.SmartEndpointInterceptor;

/**
 * SOAP-specific extension of the {@link org.springframework.ws.server.SmartEndpointInterceptor} interface. Allows for
 * handling of SOAP faults, which are considered different from regular responses.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface SmartSoapEndpointInterceptor extends SmartEndpointInterceptor, SoapEndpointInterceptor {

}
