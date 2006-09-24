/*
 * Copyright 2006 the original author or authors.
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

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.xml.JaxpVersion;

/**
 * Factory for compiled <code>XPathExpression</code>s, being aware of JAXP 1.3+ XPath functionality, Jaxen, and Xalan.
 * Mainly for internal use of the framework.
 * <p/>
 * The goal of this class is to avoid runtime dependencies a specific XPath engine, simply using the best XPath
 * implementation that is available. Prefers JAXP 1.3+ XPath implementations to Jaxen, and falls back to Xalan.
 *
 * @author Arjen Poutsma
 * @see XPathExpression
 */
public abstract class XPathExpressionFactory {

    private static final Log logger = LogFactory.getLog(XPathExpressionFactory.class);

    private static final String XALAN_XPATH_CLASS_NAME = "org.apache.xpath.XPath";

    private static boolean xalanXPathAvailable;

    private static final String JAXEN_CLASS_NAME = "org.jaxen.XPath";

    private static boolean jaxenAvailable;

    static {
        // Check whether JAXP 1.3, Resin, Jaxen, or Xalan are available
        if (JaxpVersion.getJaxpVersion() >= JaxpVersion.JAXP_13) {
            logger.info("JAXP 1.3 available");
        }
        try {
            Class.forName(JAXEN_CLASS_NAME);
            jaxenAvailable = true;
            logger.info("Jaxen available");
        }
        catch (ClassNotFoundException ex) {
            jaxenAvailable = false;
        }
        try {
            Class.forName(XALAN_XPATH_CLASS_NAME);
            xalanXPathAvailable = true;
            logger.info("Xalan available");
        }
        catch (ClassNotFoundException ex) {
            xalanXPathAvailable = false;
        }
    }

    /**
     * Create a compiled XPath expression using the given string.
     *
     * @param expression the XPath expression
     * @return the compiled XPath expression
     * @throws IllegalStateException if neither JAXP 1.3+, Jaxen, or Xalan are available
     * @throws XPathParseException   if the given expression cannot be parsed
     */
    public static XPathExpression createXPathExpression(String expression)
            throws IllegalStateException, XPathParseException {
        return createXPathExpression(expression, Collections.EMPTY_MAP);
    }

    /**
     * Create a compiled XPath expression using the given string and namespaces. The namespace map should consist of
     * string prefixes mapped to string namespaces.
     *
     * @param expression the XPath expression
     * @param namespaces a map that binds string prefixes to string namespaces
     * @return the compiled XPath expression
     * @throws IllegalStateException if neither JAXP 1.3+, Jaxen, or Xalan are available
     * @throws XPathParseException   if the given expression cannot be parsed
     */
    public static XPathExpression createXPathExpression(String expression, Map namespaces)
            throws IllegalStateException, XPathParseException {
        Assert.hasLength(expression, "expression is empty");
        if (JaxpVersion.getJaxpVersion() >= JaxpVersion.JAXP_13) {
            logger.debug("Creating [javax.xml.xpath.XPathExpression]");
            return Jaxp13XPathExpressionFactory.createXPathExpression(expression, namespaces);
        }
        else if (jaxenAvailable) {
            logger.debug("Creating [org.jaxen.XPath]");
            return JaxenXPathExpressionFactory.createXPathExpression(expression, namespaces);
        }
        else if (xalanXPathAvailable) {
            logger.debug("Creating [org.apache.xpath.XPath]");
            return XalanXPathExpressionFactory.createXPathExpression(expression, namespaces);
        }
        else {
            throw new IllegalStateException(
                    "Could not create XPathExpression: could not locate JAXP 1.3, Resin, Xalan on the class path");
        }
    }


}
