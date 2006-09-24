/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.support;

import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * Helper class for using <code>javax.xml.namespace.QName</code>.
 *
 * @author Arjen Poutsma
 * @see javax.xml.namespace.QName
 */
public abstract class QNameUtils {

    /**
     * Validates the given String as a QName
     *
     * @param text the qualified name
     * @return <code>true</code> if valid, <code>false</code> otherwise
     */
    public static boolean validateQName(String text) {
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
     *
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

}
