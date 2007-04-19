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

package org.springframework.ws.server.endpoint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method parameter should be bound to an XPath expression. The annotation value signifies the XPath
 * expression to use. The parameter can be of the following types: <ul> <li><code>boolean</code>, or {@link
 * Boolean}</li> <li><code>double</code>, or {@link Double}</li> <li>{@link String}</li> <li>{@link
 * org.w3c.dom.Node}</li> <li>{@link org.w3c.dom.NodeList}</li> </ul>
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.server.endpoint.adapter.XPathParamAnnotationMethodEndpointAdapter
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface XPathParam {

    String value();
}
