/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.xml.namespace;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.springframework.util.Assert;

/**
 * Simple {@code javax.xml.namespace.NamespaceContext} implementation. Follows the standard
 * {@code NamespaceContext} contract, and is loadable via a {@code java.util.Map} or
 * {@code java.util.Properties} object
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class SimpleNamespaceContext implements NamespaceContext {

    private Map<String, String> prefixToNamespaceUri = new LinkedHashMap<String, String>();

    private Map<String, Set<String>> namespaceUriToPrefixes = new LinkedHashMap<String, Set<String>>();

    @Override
    public String getNamespaceURI(String prefix) {
        Assert.notNull(prefix, "prefix is null");
        if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            return XMLConstants.XML_NS_URI;
        }
        else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        else if (prefixToNamespaceUri.containsKey(prefix)) {
            return prefixToNamespaceUri.get(prefix);
        }
        return XMLConstants.NULL_NS_URI;
    }

    @Override
    public String getPrefix(String namespaceUri) {
        Iterator<String> iterator = getPrefixes(namespaceUri);
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceUri) {
        Set<String> prefixes = getPrefixesInternal(namespaceUri);
        prefixes = Collections.unmodifiableSet(prefixes);
        return prefixes.iterator();
    }

    /**
     * Sets the bindings for this namespace context. The supplied map must consist of string key value pairs.
     *
     * @param bindings the bindings
     */
    public void setBindings(Map<String, String> bindings) {
        for (Map.Entry<String, String> entry : bindings.entrySet()) {
            bindNamespaceUri(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Binds the given namespace as default namespace.
     *
     * @param namespaceUri the namespace uri
     */
    public void bindDefaultNamespaceUri(String namespaceUri) {
        bindNamespaceUri(XMLConstants.DEFAULT_NS_PREFIX, namespaceUri);
    }

    /**
     * Binds the given prefix to the given namespace.
     *
     * @param prefix       the namespace prefix
     * @param namespaceUri the namespace uri
     */
    public void bindNamespaceUri(String prefix, String namespaceUri) {
        Assert.notNull(prefix, "No prefix given");
        Assert.notNull(namespaceUri, "No namespaceUri given");
        if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            Assert.isTrue(XMLConstants.XML_NS_URI.equals(namespaceUri), "Prefix \"" + prefix +
                    "\" bound to namespace \"" + namespaceUri + "\" (should be \"" + XMLConstants.XML_NS_URI + "\")");
        } else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            Assert.isTrue(XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceUri), "Prefix \"" + prefix +
                    "\" bound to namespace \"" + namespaceUri + "\" (should be \"" +
                    XMLConstants.XMLNS_ATTRIBUTE_NS_URI + "\")");
        }
        else {
            prefixToNamespaceUri.put(prefix, namespaceUri);
            getPrefixesInternal(namespaceUri).add(prefix);
        }
    }

    /** Removes all declared prefixes. */
    public void clear() {
        prefixToNamespaceUri.clear();
        namespaceUriToPrefixes.clear();
    }

    /**
     * Returns all declared prefixes.
     *
     * @return the declared prefixes
     */
    public Iterator<String> getBoundPrefixes() {
        Set<String> prefixes = new HashSet<String>(prefixToNamespaceUri.keySet());
        prefixes.remove(XMLConstants.DEFAULT_NS_PREFIX);
        prefixes = Collections.unmodifiableSet(prefixes);
        return prefixes.iterator();
    }

    private Set<String> getPrefixesInternal(String namespaceUri) {
        if (XMLConstants.XML_NS_URI.equals(namespaceUri)) {
            return Collections.singleton(XMLConstants.XML_NS_PREFIX);
        }
        else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceUri)) {
            return Collections.singleton(XMLConstants.XMLNS_ATTRIBUTE);
        }
        else {
            Set<String> set = namespaceUriToPrefixes.get(namespaceUri);
            if (set == null) {
                set = new LinkedHashSet<String>();
                namespaceUriToPrefixes.put(namespaceUri, set);
            }
            return set;
        }
    }

    /**
     * Removes the given prefix from this context.
     *
     * @param prefix the prefix to be removed
     */
    public void removeBinding(String prefix) {
        String namespaceUri = prefixToNamespaceUri.remove(prefix);
        if (namespaceUri != null) {
            Set<String> prefixes = getPrefixesInternal(namespaceUri);
            prefixes.remove(prefix);
        }
    }

    public boolean hasBinding(String prefix) {
        return prefixToNamespaceUri.containsKey(prefix);
    }
}
