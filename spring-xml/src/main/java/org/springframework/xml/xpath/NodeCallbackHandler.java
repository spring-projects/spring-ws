/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xpath;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * An interface used by {@link XPathOperations} implementations for processing
 * {@link Node} objects on a per-node basis. Implementations of this interface perform the
 * actual work of processing nodes, but don't need to worry about exception handling.
 * <p>
 * Consider using a {@link NodeMapper} instead if you need to map exactly result object
 * per node, assembling them in a List.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see XPathOperations#evaluate(String,javax.xml.transform.Source,NodeCallbackHandler)
 */
public interface NodeCallbackHandler {

	/**
	 * Processed a single node.
	 * @param node the node to map
	 * @throws DOMException in case of DOM errors
	 */
	void processNode(Node node) throws DOMException;

}
