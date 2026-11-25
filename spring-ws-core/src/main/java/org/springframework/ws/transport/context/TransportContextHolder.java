/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.context;

import org.jspecify.annotations.Nullable;

/**
 * Simple holder class that associates a {@code TransportContext} instance with the
 * current thread. The {@code TransportContext} will be inherited by any child threads
 * spawned by the current thread.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see TransportContext
 */
public abstract class TransportContextHolder {

	private static final ThreadLocal<@Nullable TransportContext> transportContextHolder = new TransportThreadLocal();

	/**
	 * Associate the given {@code TransportContext} with the current thread.
	 * @param transportContext the current transport context, or {@code null} to reset the
	 * thread-bound context
	 */
	public static void setTransportContext(@Nullable TransportContext transportContext) {
		if (transportContext != null) {
			transportContextHolder.set(transportContext);
		}
		else {
			transportContextHolder.remove();
		}
	}

	/**
	 * Return the {@code TransportContext} associated with the current thread, if any.
	 * @return the current transport context, or {@code null} if none
	 */
	public static @Nullable TransportContext getTransportContext() {
		return transportContextHolder.get();
	}

	private static final class TransportThreadLocal extends ThreadLocal<@Nullable TransportContext> {

		public String toString() {
			return "Transport State";
		}

	}

}
