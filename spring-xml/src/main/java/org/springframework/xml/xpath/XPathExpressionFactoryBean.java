/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xpath;

import java.util.Map;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Spring {@link FactoryBean} for {@link XPathExpression} object. Facilitates injection of
 * XPath expressions into endpoint beans.
 * <p>
 * Uses {@link XPathExpressionFactory} underneath, so support is provided for JAXP 1.3,
 * and Jaxen XPaths.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #setExpression(String)
 */
public class XPathExpressionFactoryBean implements FactoryBean<XPathExpression>, InitializingBean {

	@SuppressWarnings("NullAway.Init")
	private Map<String, String> namespaces;

	@SuppressWarnings("NullAway.Init")
	private String expressionString;

	@SuppressWarnings("NullAway.Init")
	private XPathExpression expression;

	/** Sets the XPath expression. Setting this property is required. */
	public void setExpression(String expression) {
		this.expressionString = expression;
	}

	/**
	 * Sets the namespaces for the expressions. The given properties binds string prefixes
	 * to string namespaces.
	 */
	public void setNamespaces(Map<String, String> namespaces) {
		this.namespaces = namespaces;
	}

	@Override
	public void afterPropertiesSet() throws IllegalStateException, XPathParseException {
		Assert.notNull(this.expressionString, "expression is required");
		if (CollectionUtils.isEmpty(this.namespaces)) {
			this.expression = XPathExpressionFactory.createXPathExpression(this.expressionString);
		}
		else {
			this.expression = XPathExpressionFactory.createXPathExpression(this.expressionString, this.namespaces);
		}
	}

	@Override
	public XPathExpression getObject() throws Exception {
		return this.expression;
	}

	@Override
	public Class<? extends XPathExpression> getObjectType() {
		return XPathExpression.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
