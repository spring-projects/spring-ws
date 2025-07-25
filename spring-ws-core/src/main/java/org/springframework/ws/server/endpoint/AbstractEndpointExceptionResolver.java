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

package org.springframework.ws.server.endpoint;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.core.Ordered;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointExceptionResolver;

/**
 * Abstract base class for {@link EndpointExceptionResolver EndpointExceptionResolvers}.
 * <p>
 * Provides a set of mapped endpoints that the resolver should map.
 *
 * @author Arjen Poutsma
 * @author Tareq Abed Rabbo
 * @since 1.0.0
 */
public abstract class AbstractEndpointExceptionResolver implements EndpointExceptionResolver, Ordered {

	/** Shared {@link Log} for subclasses to use. */
	protected final Log logger = LogFactory.getLog(getClass());

	private int order = Integer.MAX_VALUE; // default: same as non-Ordered

	private @Nullable Set<?> mappedEndpoints;

	private @Nullable Log warnLogger;

	/**
	 * Specify the set of endpoints that this exception resolver should map.
	 * <p>
	 * The exception mappings and the default fault will only apply to the specified
	 * endpoints.
	 * <p>
	 * If no endpoints are set, both the exception mappings and the default fault will
	 * apply to all handlers. This means that a specified default fault will be used as
	 * fallback for all exceptions; any further {@code EndpointExceptionResolvers} in the
	 * chain will be ignored in this case.
	 */
	public void setMappedEndpoints(Set<?> mappedEndpoints) {
		this.mappedEndpoints = mappedEndpoints;
	}

	/**
	 * Set the log category for warn logging. The name will be passed to the underlying
	 * logger implementation through Commons Logging, getting interpreted as log category
	 * according to the logger's configuration.
	 * <p>
	 * Default is no warn logging. Specify this setting to activate warn logging into a
	 * specific category. Alternatively, override the {@link #logException} method for
	 * custom logging.
	 */
	public void setWarnLogCategory(String loggerName) {
		this.warnLogger = LogFactory.getLog(loggerName);
	}

	/**
	 * Specify the order value for this mapping.
	 * <p>
	 * Default value is {@link Integer#MAX_VALUE}, meaning that it's non-ordered.
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public final void setOrder(int order) {
		this.order = order;
	}

	@Override
	public final int getOrder() {
		return this.order;
	}

	/**
	 * Default implementation that checks whether the given {@code endpoint} is in the set
	 * of {@link #setMappedEndpoints mapped endpoints}.
	 * @see #resolveExceptionInternal(MessageContext, Object, Exception)
	 */
	@Override
	public final boolean resolveException(MessageContext messageContext, @Nullable Object endpoint, Exception ex) {
		Object mappedEndpoint = (endpoint instanceof MethodEndpoint methodEndpoint) ? methodEndpoint.getBean()
				: endpoint;
		if (this.mappedEndpoints != null && !this.mappedEndpoints.contains(mappedEndpoint)) {
			return false;
		}
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Resolving exception from endpoint [" + endpoint + "]: " + ex);
		}
		boolean resolved = resolveExceptionInternal(messageContext, endpoint, ex);
		if (resolved) {
			logException(messageContext, ex);
		}
		return resolved;
	}

	/**
	 * Log the given exception at warn level, provided that warn logging has been
	 * activated through the {@link #setWarnLogCategory "warnLogCategory"} property.
	 * <p>
	 * Calls {@link #buildLogMessage} in order to determine the concrete message to log.
	 * Always passes the full exception to the logger.
	 * @param messageContext current message context request
	 * @param ex the exception that got thrown during handler execution
	 * @see #setWarnLogCategory
	 * @see #buildLogMessage
	 * @see org.apache.commons.logging.Log#warn(Object, Throwable)
	 */
	protected void logException(MessageContext messageContext, Exception ex) {
		if (this.warnLogger != null && this.warnLogger.isWarnEnabled()) {
			this.warnLogger.warn(buildLogMessage(messageContext, ex), ex);
		}
	}

	/**
	 * Build a log message for the given exception, occured during processing the given
	 * message context.
	 * @param messageContext the message context
	 * @param ex the exception that got thrown during handler execution
	 * @return the log message to use
	 */
	protected String buildLogMessage(MessageContext messageContext, Exception ex) {
		return "Endpoint execution resulted in exception";
	}

	/**
	 * Template method for resolving exceptions that is called by
	 * {@link EndpointExceptionResolver#resolveException}.
	 * @param messageContext current message context
	 * @param endpoint the executed endpoint, or {@code null} if none chosen at the time
	 * of the exception
	 * @param ex the exception that got thrown during endpoint execution
	 * @return {@code true} if resolved; {@code false} otherwise
	 * @see EndpointExceptionResolver#resolveException(MessageContext, Object, Exception)
	 */
	protected abstract boolean resolveExceptionInternal(MessageContext messageContext, @Nullable Object endpoint,
			Exception ex);

}
