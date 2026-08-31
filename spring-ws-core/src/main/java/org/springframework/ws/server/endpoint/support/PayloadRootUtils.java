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
