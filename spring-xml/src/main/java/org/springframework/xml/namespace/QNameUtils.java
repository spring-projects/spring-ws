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

package org.springframework.xml.namespace;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Node;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Helper class for using {@link QName}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see javax.xml.namespace.QName
 */
public abstract class QNameUtils {

	/**
	 * Validates the given String as a QName.
	 * @param text the qualified name
	 * @return {@code true} if valid, {@code false} otherwise
	 */
	public static boolean validateQName(@Nullable String text) {
		if (!StringUtils.hasLength(text)) {
			return false;
		}
		if (text.charAt(0) == '{') {
			int i = text.indexOf('}');

			if (i == -1 || i == text.length() - 1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the qualified name of the given DOM Node.
	 * @param node the node
	 * @return the qualified name of the node
	 */
	public static QName getQNameForNode(Node node) {
		if (node.getNamespaceURI() != null && node.getPrefix() != null && node.getLocalName() != null) {
			return new QName(node.getNamespaceURI(), node.getLocalName(), node.getPrefix());
		}
		else if (node.getNamespaceURI() != null && node.getLocalName() != null) {
			return new QName(node.getNamespaceURI(), node.getLocalName());
		}
		else if (node.getLocalName() != null) {
			return new QName(node.getLocalName());
		}
		else {
			// as a last resort, use the node name
			return new QName(node.getNodeName());
		}
	}

	/**
	 * Convert a {@code QName} to a qualified name, as used by DOM and SAX. The returned
	 * string has a format of {@code prefix:localName} if the prefix is set, or just
	 * {@code localName} if not.
	 * @param qName the {@code QName}
	 * @return the qualified name
	 */
	public static String toQualifiedName(QName qName) {
		String prefix = qName.getPrefix();
		if (!StringUtils.hasLength(prefix)) {
			return qName.getLocalPart();
		}
		else {
			return prefix + ":" + qName.getLocalPart();
		}
	}

	/**
	 * Convert a namespace URI and DOM or SAX qualified name to a {@code QName}. The
	 * qualified name can have the form {@code prefix:localname} or {@code localName}.
	 * @param namespaceUri the namespace URI
	 * @param qualifiedName the qualified name
	 * @return a QName
	 */
	public static QName toQName(String namespaceUri, String qualifiedName) {
		int idx = qualifiedName.indexOf(':');
		if (idx == -1) {
			return new QName(namespaceUri, qualifiedName);
		}
		else {
			return new QName(namespaceUri, qualifiedName.substring(idx + 1), qualifiedName.substring(0, idx));
		}
	}

	/**
	 * Parse the given qualified name string into a {@code QName}. Expects the syntax
	 * {@code localPart}, {@code {namespace}localPart}, or
	 * {@code {namespace}prefix:localPart}. This format resembles the {@code toString()}
	 * representation of {@code QName} itself, but allows for prefixes to be specified as
	 * well.
	 * @return a corresponding QName instance
	 * @throws IllegalArgumentException when the given string is {@code null} or empty.
	 */
	public static QName parseQNameString(String qNameString) {
		Assert.hasLength(qNameString, "QName text may not be null or empty");
		if (qNameString.charAt(0) != '{') {
			return new QName(qNameString);
		}
		else {
			int endOfNamespaceURI = qNameString.indexOf('}');
			if (endOfNamespaceURI == -1) {
				throw new IllegalArgumentException(
						"Cannot create QName from \"" + qNameString + "\", missing closing \"}\"");
			}
			int prefixSeperator = qNameString.indexOf(':', endOfNamespaceURI + 1);
			String namespaceURI = qNameString.substring(1, endOfNamespaceURI);
			if (prefixSeperator == -1) {
				return new QName(namespaceURI, qNameString.substring(endOfNamespaceURI + 1));
			}
			else {
				return new QName(namespaceURI, qNameString.substring(prefixSeperator + 1),
						qNameString.substring(endOfNamespaceURI + 1, prefixSeperator));
			}
		}

	}

}
