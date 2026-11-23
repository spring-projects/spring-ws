/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.namespace;

import java.beans.PropertyEditorSupport;

import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

/**
 * PropertyEditor for {@code javax.xml.namespace.QName}, to populate a property of type
 * QName from a String value.
 * <p>
 * Expects one of the following syntaxes:
 * <ul>
 * <li>{@code localPart}</li>
 * <li>{@code {namespace}localPart}</li>
 * <li>{@code {namespace}prefix:localPart}</li>
 * </ul>
 *
 * This resembles the {@code toString()} representation of {@code QName} itself, but
 * allows for prefixes to be specified as well.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see javax.xml.namespace.QName
 * @see javax.xml.namespace.QName#toString()
 * @see javax.xml.namespace.QName#valueOf(String)
 */
public class QNameEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		setValue(QNameUtils.parseQNameString(text));
	}

	@Override
	public String getAsText() {
		Object value = getValue();
		if (value instanceof QName qName) {
			String prefix = qName.getPrefix();
			if (StringUtils.hasLength(qName.getNamespaceURI()) && StringUtils.hasLength(prefix)) {
				return "{" + qName.getNamespaceURI() + "}" + prefix + ":" + qName.getLocalPart();
			}
			else if (StringUtils.hasLength(qName.getNamespaceURI())) {
				return "{" + qName.getNamespaceURI() + "}" + qName.getLocalPart();
			}
			else {
				return qName.getLocalPart();
			}
		}
		else {
			return "";
		}
	}

}
