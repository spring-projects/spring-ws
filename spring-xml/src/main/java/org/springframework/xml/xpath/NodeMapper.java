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

package org.springframework.xml.xpath;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * An interface used by {@link XPathOperations} implementations for mapping {@link Node} objects on a per-node basis.
 * Implementations of this interface perform the actual work of mapping each node to a result object, but don't need to
 * worry about exception handling.
 *
 * @author Arjen Poutsma
 * @see XPathOperations#evaluate(String,javax.xml.transform.Source,NodeMapper)
 * @see XPathOperations#evaluateAsObject(String,javax.xml.transform.Source,NodeMapper)
 * @see XPathExpression#evaluate(org.w3c.dom.Node,NodeMapper)
 * @see XPathExpression#evaluateAsObject(org.w3c.dom.Node,NodeMapper)
 * @since 1.0.0
 */
public interface NodeMapper<T> {

	/**
	 * Maps a single node to an arbitrary object.
	 *
	 * @param node	  the node to map
	 * @param nodeNum the number of the current node
	 * @return object for the current node
	 * @throws DOMException in case of DOM errors
	 */
	T mapNode(Node node, int nodeNum) throws DOMException;

}
