/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.wsdl.wsdl11.provider;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

/**
 * Strategy for adding {@link javax.wsdl.PortType}s to a {@link javax.wsdl.Definition}.
 * <p>
 * Used by {@link org.springframework.ws.wsdl.wsdl11.ProviderBasedWsdl4jDefinition}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public interface PortTypesProvider {

	void addPortTypes(Definition definition) throws WSDLException;

}
