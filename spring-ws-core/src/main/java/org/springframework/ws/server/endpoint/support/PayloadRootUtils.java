/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.support;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.jspecify.annotations.Nullable;

import org.springframework.xml.transform.TransformerHelper;

/**
 * Helper class for determining the root qualified name of a Web Service payload.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @deprecated since 5.1.0 in favor of
 * {@link org.springframework.ws.support.PayloadRootUtils}, as the payload root is also
 * relevant on the client side.
 */
@Deprecated(since = "5.1.0", forRemoval = true)
public abstract class PayloadRootUtils {

	private PayloadRootUtils() {
	}

	/**
	 * Return the root qualified name of the given source, or {@code null} if it could not
	 * be determined.
	 * @param source the source to inspect
	 * @param transformerFactory the transformer factory to use, if necessary
	 * @return the root qualified name, or {@code null}
	 * @throws TransformerException if the source could not be read
	 */
	public static @Nullable QName getPayloadRootQName(@Nullable Source source, TransformerFactory transformerFactory)
			throws TransformerException {
		return org.springframework.ws.support.PayloadRootUtils.getPayloadRootQName(source, transformerFactory);
	}

	/**
	 * Return the root qualified name of the given source, or {@code null} if it could not
	 * be determined.
	 * @param source the source to inspect
	 * @param transformerHelper the transformer helper to use, if necessary
	 * @return the root qualified name, or {@code null}
	 * @throws TransformerException if the source could not be read
	 */
	public static @Nullable QName getPayloadRootQName(@Nullable Source source, TransformerHelper transformerHelper)
			throws TransformerException {
		return org.springframework.ws.support.PayloadRootUtils.getPayloadRootQName(source, transformerHelper);
	}

}
