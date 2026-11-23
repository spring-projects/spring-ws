/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xpath;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * An interface used by {@link XPathOperations} implementations for mapping {@link Node}
 * objects on a per-node basis. Implementations of this interface perform the actual work
 * of mapping each node to a result object, but don't need to worry about exception
 * handling.
 *
 * @param <T> the type of the result object
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see XPathOperations#evaluate(String,javax.xml.transform.Source,NodeMapper)
 * @see XPathOperations#evaluateAsObject(String,javax.xml.transform.Source,NodeMapper)
 * @see XPathExpression#evaluate(org.w3c.dom.Node,NodeMapper)
 * @see XPathExpression#evaluateAsObject(org.w3c.dom.Node,NodeMapper)
 */
public interface NodeMapper<T> {

	/**
	 * Maps a single node to an arbitrary object.
	 * @param node the node to map
	 * @param nodeNum the number of the current node
	 * @return object for the current node
	 * @throws DOMException in case of DOM errors
	 */
	@Nullable T mapNode(Node node, int nodeNum) throws DOMException;

}
