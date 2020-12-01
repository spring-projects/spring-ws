/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.xml.xpath;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * An interface used by {@link XPathOperations} implementations for processing {@link Node} objects on a per-node basis.
 * Implementations of this interface perform the actual work of processing nodes, but don't need to worry about
 * exception handling.
 * <p>
 * Consider using a {@link NodeMapper} instead if you need to map exactly result object per node, assembling them in a
 * List.
 *
 * @author Arjen Poutsma
 * @see XPathOperations#evaluate(String,javax.xml.transform.Source,NodeCallbackHandler)
 * @since 1.0.0
 */
public interface NodeCallbackHandler {

	/**
	 * Processed a single node.
	 *
	 * @param node the node to map
	 * @throws DOMException in case of DOM errors
	 */
	void processNode(Node node) throws DOMException;

}
